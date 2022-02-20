package com.evansloan.collectionlog;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Provides;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Collection Log",
	description = "Add items obtained/total items to the top of the collection log",
	tags = {"collection", "log"}
)
public class CollectionLogPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "collectionlog";

	private static final int COLLECTION_LOG_CONTAINER = 1;
	private static final int COLLECTION_LOG_DRAW_LIST_SCRIPT_ID = 2730;
	private static final int COLLECTION_LOG_DEFAULT_HIGHLIGHT = 901389;
	private static final int COLLECTION_LOG_ACTIVE_TAB_SPRITE_ID = 2283;

	private static final String COLLECTION_LOG_TITLE = "Collection Log";
	private static final Pattern COLLECTION_LOG_TITLE_REGEX = Pattern.compile("Collection Log - (?:U: )?(?:(\\d+)/(\\d+)|([\\d\\.%]+))(?: T: \\d+/\\d+)?");
	private static final String COLLECTION_LOG_TARGET = "Collection log";
	private static final String COLLECTION_LOG_EXPORT = "Export";
	private static final File COLLECTION_LOG_SAVE_DATA_DIR = new File(RUNELITE_DIR, "collectionlog");
	private static final File COLLECTION_LOG_EXPORT_DIR = new File(COLLECTION_LOG_SAVE_DATA_DIR, "exports");

	private static final String COLLECTION_LOG_TABS_KEY = "tabs";
	private static final String COLLECTION_LOG_TOTAL_OBTAINED_KEY = "total_obtained";
	private static final String COLLECTION_LOG_TOTAL_ITEMS_KEY = "total_items";
	private static final String COLLECTION_LOG_UNIQUE_OBTAINED_KEY = "unique_obtained";
	private static final String COLLECTION_LOG_UNIQUE_ITEMS_KEY = "unique_items";

	private JsonObject collectionLogData;
	private String userHash;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private CollectionLogConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private CollectionLogApiClient apiClient;

	@Provides
	CollectionLogConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CollectionLogConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			// Remove old configs
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP, "completed_categories");
			configManager.unsetConfiguration(CONFIG_GROUP, "highlight_completed");
			configManager.unsetConfiguration(CONFIG_GROUP, "new_item_chat_message");
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP, "obtained_counts");
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP, "obtained_items");
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP, "kill_counts");
			configManager.unsetConfiguration(CONFIG_GROUP, "total_items");
		}
	}

	@Override
	protected void shutDown()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			setCollectionLogTitle(COLLECTION_LOG_TITLE);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (!isValidWorldType())
		{
			return;
		}

		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			userHash = getUserHash();
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN ||
			gameStateChanged.getGameState() == GameState.HOPPING)
		{
			saveCollectionLogData();
			collectionLogData = null;
			userHash = null;
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == COLLECTION_LOG_DRAW_LIST_SCRIPT_ID)
		{
			clientThread.invokeLater(this::getEntry);
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (event.getMenuEntries().length < 2)
		{
			return;
		}

		MenuEntry entry = event.getMenuEntries()[1];

		String entryTarget = entry.getTarget();
		if (entryTarget.equals(""))
		{
			entryTarget = entry.getOption();
		};

		if (!entryTarget.toLowerCase(Locale.ROOT).endsWith(COLLECTION_LOG_TARGET.toLowerCase(Locale.ROOT)))
		{
			return;
		}

		client.createMenuEntry(1)
			.setOption(COLLECTION_LOG_EXPORT)
			.setTarget(entryTarget)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> saveCollectionLogDataToFile(true));
	}

	/**
	 * Save collection data to a .json file.
	 *
	 * @param export Flags the data as an export to be saved in collectionlog/export
	 */
	private void saveCollectionLogDataToFile(boolean export)
	{
		if (collectionLogData == null)
		{
			return;
		}

		COLLECTION_LOG_SAVE_DATA_DIR.mkdir();

		String filePath = "";
		if (export)
		{
			COLLECTION_LOG_EXPORT_DIR.mkdir();
			String fileName = new SimpleDateFormat("'collectionlog-'yyyyMMdd'T'HHmmss'.json'").format(new Date());
			filePath = COLLECTION_LOG_EXPORT_DIR + File.separator  + fileName;
		}
		else
		{
			String fileName = "collectionlog-" + client.getLocalPlayer().getName() + ".json";
			filePath = COLLECTION_LOG_SAVE_DATA_DIR + File.separator + fileName;
		}

		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(collectionLogData.toString());
			writer.close();

			if (export)
			{
				String message = "Collection log exported to " + filePath;

				if (config.notifyOnExport())
				{
					notifier.notify(message);
				}

				if (config.sendExportChatMessage())
				{
					String chatMessage = new ChatMessageBuilder()
						.append(ChatColorType.HIGHLIGHT)
						.append(message)
						.build();

					chatMessageManager.queue(
						QueuedMessage.builder()
							.type(ChatMessageType.CONSOLE)
							.runeLiteFormattedMessage(chatMessage)
							.build()
					);
				}
			}
		}
		catch (IOException e)
		{
			log.error("Unable to export Collection log items: " + e.getMessage());
			if (config.notifyOnExport())
			{
				notifier.notify("Unable to export collection log: " + e.getMessage());
			}
		}
	}

	/**
	 * Save collection log data to a file or upload to
	 * collectionlog.net
	 */
	private void saveCollectionLogData()
	{
		if (collectionLogData == null || !isValidWorldType())
		{
			return;
		}

		saveCollectionLogDataToFile(false);

		if (config.uploadCollectionLog())
		{
			try
			{
				apiClient.createUser(
					client.getLocalPlayer().getName(),
					client.getAccountType().toString(),
					userHash
				);

				if (!collectionLogExists())
				{
					apiClient.createCollectionLog(collectionLogData, userHash);
				}
				else
				{
					apiClient.updateCollectionLog(collectionLogData, userHash);
				}
			}
			catch (IOException e)
			{
				log.warn("Unable to save collection log data to collectionlog.net");
			}
		}
	}

	/**
	 * Retrieves and updates all items in the given entry
	 *
	 * @param collectionLogEntry Collection log entry to update
	 */
	private void updateEntryItems(JsonObject collectionLogEntry)
	{
		Widget itemsContainer = client.getWidget(WidgetInfo.COLLECTION_LOG_ENTRY_ITEMS);

		if (itemsContainer == null)
		{
			return;
		}

		Widget[] widgetItems = itemsContainer.getDynamicChildren();
		JsonArray items = new JsonArray();
		for (Widget widgetItem : widgetItems)
		{
			JsonObject item = new JsonObject();
			item.addProperty("id", widgetItem.getItemId());
			item.addProperty("name", itemManager.getItemComposition(widgetItem.getItemId()).getName());
			item.addProperty("quantity", widgetItem.getOpacity() == 0 ? widgetItem.getItemQuantity() : 0);
			item.addProperty("obtained", widgetItem.getOpacity() == 0);
			items.add(item);
		}

		collectionLogEntry.add("items", items);
	}

	/**
	 * Retrieves and updates kill counts found in the given collection log entry
	 *
	 * @param categoryHead Widget containing kill count information
	 * @param collectionLogEntry Collection log entry to update
	 */
	private void updateEntryKillCounts(Widget categoryHead, JsonObject collectionLogEntry)
	{
		Widget[] children = categoryHead.getDynamicChildren();
		if (children.length < 3)
		{
			return;
		}

		JsonArray killCounts = new JsonArray();
		Widget[] killCountWidgets = Arrays.copyOfRange(children, 2, children.length);
		for (Widget killCountWidget : killCountWidgets)
		{
			String[] killCountText = killCountWidget.getText().split(": ");
			String killCountName = killCountText[0];
			String killCountAmount = killCountText[1].split(">")[1]
				.split("<")[0]
				.replace(",", "");
			killCounts.add(String.format("%s: %s", killCountName, killCountAmount));
		}
		collectionLogEntry.add("kill_count", killCounts);
	}

	/**
	 * Load the current entry being viewed in the collection log
	 * and get/update relevant information contained in the entry
	 */
	private void getEntry()
	{
		if (!isValidWorldType())
		{
			return;
		}

		if (collectionLogData == null)
		{
			loadCollectionLogData();
		}

		String activeTabName = getActiveTabName();
		if (activeTabName == null)
		{
			return;
		}

		Widget entryHead = client.getWidget(WidgetInfo.COLLECTION_LOG_ENTRY_HEADER);

		if (entryHead == null)
		{
			return;
		}

		String entryTitle = entryHead.getDynamicChildren()[0].getText();

		if (!entryExistsInActiveTab(activeTabName, entryTitle))
		{
			return;
		}

		int prevObtainedItemCount = getEntryItemCount(activeTabName, entryTitle);

		JsonObject collectionLogEntry = new JsonObject();
		updateEntryItems(collectionLogEntry);
		updateEntryKillCounts(entryHead, collectionLogEntry);

		int newObtainedItemCount = getEntryItemCount(collectionLogEntry);

		JsonObject collectionLogTabs = collectionLogData.get(COLLECTION_LOG_TABS_KEY).getAsJsonObject();
		if (collectionLogTabs.has(activeTabName))
		{
			// Can happen when entries are opened in quick succession
			// Item data in the widget doesn't load properly and obtained count shows as 0
			if (prevObtainedItemCount > newObtainedItemCount)
			{
				return;
			}

			JsonObject collectionLogTab = collectionLogTabs.get(activeTabName).getAsJsonObject();
			collectionLogTab.add(entryTitle, collectionLogEntry);
		}
		else
		{
			JsonObject collectionLogTab = new JsonObject();
			collectionLogTab.add(entryTitle, collectionLogEntry);
			collectionLogTabs.add(activeTabName, collectionLogTab);
		}

		if (prevObtainedItemCount == newObtainedItemCount)
		{
			update();
			return;
		}

		int prevTotalObtained = collectionLogData.get(COLLECTION_LOG_TOTAL_OBTAINED_KEY).getAsInt();
		int newTotalObtained = prevTotalObtained + (newObtainedItemCount - prevObtainedItemCount);
		collectionLogData.addProperty(COLLECTION_LOG_TOTAL_OBTAINED_KEY, newTotalObtained);

		update();
	}

	/**
	 * Update completed collection log entries with user specified color
	 */
	private void highlightEntries()
	{
		for (CollectionLogList listType : CollectionLogList.values())
		{
			Widget entryList = client.getWidget(WidgetID.COLLECTION_LOG_ID, listType.getListIndex());

			if (entryList == null)
			{
				continue;
			}

			Widget[] names = entryList.getDynamicChildren();
			for (Widget name : names)
			{
				if (name.getTextColor() == COLLECTION_LOG_DEFAULT_HIGHLIGHT)
				{
					name.setTextColor(config.highlightColor().getRGB());
				}
			}
		}
	}

	/**
	 * Build the new title for the collection log containing unique/total counts
	 * or display counts as a percentage
	 *
	 * @return Collection log title
	 */
	private String buildTitle()
	{
		StringBuilder titleBuilder = new StringBuilder(COLLECTION_LOG_TITLE);
		List<String> titleSections = new ArrayList<>();

		if (config.displayUniqueItems())
		{
			Widget collLogContainer = client.getWidget(WidgetID.COLLECTION_LOG_ID, COLLECTION_LOG_CONTAINER);

			if (collLogContainer == null)
			{
				return COLLECTION_LOG_TITLE;
			}

			String collLogTitle = collLogContainer.getDynamicChildren()[1].getText();
			Matcher m = COLLECTION_LOG_TITLE_REGEX.matcher(collLogTitle);

			if (!m.find())
			{
				return COLLECTION_LOG_TITLE;
			}

			String uniqueTitle = "";

			if (m.group(1) != null && m.group(2) != null)
			{

				int uniqueObtained = Integer.parseInt(m.group(1));
				int uniqueTotal = Integer.parseInt(m.group(2));
				uniqueTitle = String.format("U: %d/%d", uniqueObtained, uniqueTotal);
			}

			if (config.displayAsPercentage())
			{
				if (m.group(3) != null)
				{
					uniqueTitle = String.format("U: %s", m.group(3));
				}
				else
				{
					int uniqueObtained = Integer.parseInt(m.group(1));
					int uniqueTotal = Integer.parseInt(m.group(2));
					uniqueTitle = String.format("U: %.2f%%", ((double) uniqueObtained / uniqueTotal) * 100);
				}
			}
			titleSections.add(uniqueTitle);
		}

		if (config.displayTotalItems())
		{
			int totalItemsObtained = collectionLogData.get(COLLECTION_LOG_TOTAL_OBTAINED_KEY).getAsInt();
			int totalItems = collectionLogData.get(COLLECTION_LOG_TOTAL_ITEMS_KEY).getAsInt();
			String totalTitle = String.format("T: %d/%d", totalItemsObtained, totalItems);

			if (config.displayAsPercentage())
			{
				totalTitle = String.format("T: %.2f%%", ((double) totalItemsObtained / totalItems) * 100);
			}

			titleSections.add(totalTitle);
		}

		if (titleSections.size() > 0)
		{
			titleBuilder.append(" - ");
		}
		titleBuilder.append(String.join(" ", titleSections));

		return titleBuilder.toString();
	}

	/**
	 * Update collection log title and highlight completed entries
	 */
	private void update()
	{
		updateTotalItems();

		String title = buildTitle();
		setCollectionLogTitle(title);

		highlightEntries();
	}

	/**
	 * Updates the collection log title with updated counts
	 * @param title
	 */
	private void setCollectionLogTitle(String title)
	{
		Widget collLogContainer = client.getWidget(WidgetID.COLLECTION_LOG_ID, COLLECTION_LOG_CONTAINER);

		if (collLogContainer == null)
		{
			return;
		}

		Widget collLogTitle = collLogContainer.getDynamicChildren()[1];
		updateUniqueItems(collLogTitle.getText());
		collLogTitle.setText(title);
	}

	/**
	 * Get the number of obtained items in the provided entry
	 *
	 * @param activeTabName Current collection log tab
	 * @param entryTitle Current collection log entry
	 * @return Number of obtained items
	 */
	private int getEntryItemCount(String activeTabName, String entryTitle)
	{
		JsonObject collectionLogTabs = collectionLogData.get(COLLECTION_LOG_TABS_KEY).getAsJsonObject();
		if (!collectionLogTabs.has(activeTabName))
		{
			return 0;
		}

		JsonObject collectionLogTabData = collectionLogTabs.get(activeTabName).getAsJsonObject();
		if (!collectionLogTabData.has(entryTitle))
		{
			return 0;
		}

		JsonObject collectionLogEntry = collectionLogTabData.get(entryTitle).getAsJsonObject();
		return getEntryItemCount(collectionLogEntry);
	}

	private int getEntryItemCount(JsonObject collectionLogEntry)
	{
		JsonArray items = collectionLogEntry.get("items").getAsJsonArray();
		return StreamSupport.stream(items.spliterator(), false).filter(item -> {
			return item.getAsJsonObject().get("obtained").getAsBoolean();
		}).toArray().length;
	}

	/**
	 * Load collection log data from an existing .json file.
	 * If a file is not found, create a new JsonObject
	 */
	private void loadCollectionLogData()
	{
		try
		{
			String fileName = "collectionlog-" + client.getLocalPlayer().getName() + ".json";
			FileReader reader = new FileReader(COLLECTION_LOG_SAVE_DATA_DIR + File.separator + fileName);
			collectionLogData = new JsonParser().parse(reader).getAsJsonObject();
			reader.close();
		}
		catch (IOException e)
		{
			collectionLogData = new JsonObject();
			collectionLogData.addProperty(COLLECTION_LOG_TOTAL_OBTAINED_KEY, 0);
			collectionLogData.addProperty(COLLECTION_LOG_TOTAL_ITEMS_KEY, 0);
			collectionLogData.addProperty(COLLECTION_LOG_UNIQUE_OBTAINED_KEY, 0);
			collectionLogData.addProperty(COLLECTION_LOG_UNIQUE_ITEMS_KEY, 0);
			collectionLogData.add(COLLECTION_LOG_TABS_KEY, new JsonObject());
		}
	}

	/**
	 * Updates the total amount of items in the collection log
	 */
	private void updateTotalItems()
	{
		int newTotal = 0;
		JsonObject collectionLogTabs = collectionLogData.get(COLLECTION_LOG_TABS_KEY).getAsJsonObject();
		for (Map.Entry<String, JsonElement> tab : collectionLogTabs.entrySet())
		{
			for (Map.Entry<String, JsonElement> entry : tab.getValue().getAsJsonObject().entrySet())
			{
				JsonArray items = entry.getValue()
						.getAsJsonObject()
						.get("items")
						.getAsJsonArray();
				newTotal += items.size();
			}
		}

		if (newTotal > collectionLogData.get(COLLECTION_LOG_TOTAL_ITEMS_KEY).getAsInt())
		{
			collectionLogData.addProperty(COLLECTION_LOG_TOTAL_ITEMS_KEY, newTotal);
		}
	}

	/**
	 * Updates the count of unique items in the collection log
	 * Parses item counts from collection log title
	 */
	private void updateUniqueItems(String collectionLogTitle)
	{
		Matcher m = COLLECTION_LOG_TITLE_REGEX.matcher(collectionLogTitle);

		if (!m.find())
		{
			return;
		}

		if (m.group(1) != null && m.group(2) != null)
		{
			int uniqueObtained = Integer.parseInt(m.group(1));
			int uniqueTotal = Integer.parseInt(m.group(2));
			collectionLogData.addProperty(COLLECTION_LOG_UNIQUE_OBTAINED_KEY, uniqueObtained);
			collectionLogData.addProperty(COLLECTION_LOG_UNIQUE_ITEMS_KEY, uniqueTotal);
		}
	}

	/**
	 * Get the title of the current selected collection log tab
	 *
	 * @return Title of collection log tab
	 */
	private String getActiveTabName()
	{
		Widget tabsWidget = client.getWidget(WidgetInfo.COLLECTION_LOG_TABS);
		if (tabsWidget == null)
		{
			return null;
		}

		Widget[] tabs = tabsWidget.getStaticChildren();
		for (Widget tab : tabs)
		{
			Widget subTab = tab.getChild(0);

			if (subTab.getSpriteId() == COLLECTION_LOG_ACTIVE_TAB_SPRITE_ID)
			{
				return tab.getName()
					.split(">")[1]
					.split("<")[0];
			}
		}

		return null;
	}

	/**
	 * Create a hashed unique identifier based on user login to
	 * store alongside collection log data for collectionlog.net.
	 *
	 * @return SHA-256 hashed user login
	 * @throws NoSuchAlgorithmException
	 */
	private String getUserHash()
	{
		String username = client.getUsername();
		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] hash = messageDigest.digest(username.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash).replace('/', '-');
		}
		catch (NoSuchAlgorithmException e)
		{
			log.warn("Error creating userHash");
			return null;
		}
	}

	/**
	 * Check if a collection log exists for the current user on collectionlog.net
	 *
	 * @return collectionlog.net collection log exists
	 */
	private boolean collectionLogExists()
	{
		try
		{
			return apiClient.getCollectionLogExists(userHash);
		}
		catch (IOException e)
		{
			log.warn("Unable to get existing collection log from collectionlog.net");
		}

		return false;
	}

	private boolean isValidWorldType()
	{
		List<WorldType> invalidTypes = ImmutableList.of(
			WorldType.DEADMAN,
			WorldType.NOSAVE_MODE,
			WorldType.SEASONAL,
			WorldType.TOURNAMENT_WORLD
		);

		for (WorldType worldType : invalidTypes)
		{
			if (client.getWorldType().contains(worldType))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Check that the current entry being viewed exists in the current active tab.
	 * Mismatches can occur when a tab and entry are loaded within the same game tick
	 * causing item counts to be thrown off.
	 *
	 * @param tabName Active tab name
	 * @param entryName Active entry name
	 * @return Entry exists in tab
	 */
	private boolean entryExistsInActiveTab(String tabName, String entryName)
	{
		CollectionLogList activeEntryList = CollectionLogList.valueOf(tabName.toUpperCase());
		Widget entryList = client.getWidget(WidgetID.COLLECTION_LOG_ID, activeEntryList.getListIndex());
		if (entryList == null)
		{
			return false;
		}

		Widget[] listEntries = entryList.getDynamicChildren();
		return Arrays.stream(listEntries).anyMatch(listEntry -> {
			return listEntry.getText().equals(entryName);
		});
	}
}
