package com.evansloan.collectionlog;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.ScriptID;
import net.runelite.api.WorldType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import static net.runelite.client.util.Text.sanitize;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

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
	private static final String COLLECTION_LOG_ITEMS_KEY = "items";
	private static final String COLLECTION_LOG_TOTAL_OBTAINED_KEY = "total_obtained";
	private static final String COLLECTION_LOG_TOTAL_ITEMS_KEY = "total_items";
	private static final String COLLECTION_LOG_UNIQUE_OBTAINED_KEY = "unique_obtained";
	private static final String COLLECTION_LOG_UNIQUE_ITEMS_KEY = "unique_items";
	private static final String COLLECTION_LOG_COMMAND_STRING = "!log";

	private static final int ADVENTURE_LOG_COLLECTION_LOG_SELECTED_VARBIT_ID = 12061;
	private static final Pattern ADVENTURE_LOG_TITLE_PATTERN = Pattern.compile("The Exploits of (.+)");

	private CollectionLogPanel collectionLogPanel;
	private NavigationButton navigationButton;

	private boolean isPohOwner = false;

	private Map<Integer, Integer> loadedCollectionLogIcons;

	@Getter
	private JsonObject collectionLogData;

	@Getter
	private JsonObject collectionLogTemplate;

	private String userHash;

	@Inject
	private Client client;

	@Getter
	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ChatCommandManager chatCommandManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Getter
	@Inject
	private CollectionLogConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private CollectionLogApiClient apiClient;

	@Inject
	private Gson gson;

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


		if (config.showCollectionLogSidePanel())
		{
			final BufferedImage toolbarIcon = Icon.COLLECTION_LOG_TOOLBAR.getImage();

			collectionLogPanel = new CollectionLogPanel(this);
			navigationButton = NavigationButton.builder()
				.tooltip("Collection Log")
				.icon(toolbarIcon)
				.panel(collectionLogPanel)
				.priority(10)
				.build();
			clientToolbar.addNavigation(navigationButton);

			if (client.getGameState() == GameState.LOGGED_IN)
			{
				collectionLogPanel.loadLoggedInState();
			}
			else
			{
				collectionLogPanel.loadLoggedOutState();
			}
		}
		loadedCollectionLogIcons = new HashMap<>();
		chatCommandManager.registerCommandAsync(COLLECTION_LOG_COMMAND_STRING, this::collectionLogLookup);
	}

	private void collectionLogLookup(ChatMessage chatMessage, String message)
	{
		JsonObject collectionLogJson;
		try
		{
			collectionLogJson = apiClient.getCollectionLog(sanitize(chatMessage.getName()));
		}
		catch (IOException | NullPointerException e)
		{
			log.debug("username has no collection log data");
			log.error(String.valueOf(e));
			return;
		}
		// Todo this should live in api client
		CollectionLog collectionLog = new GsonBuilder()
				.registerTypeAdapter(CollectionLog.class, new CollectionLogDeserilizer())
				.create()
				.fromJson(collectionLogJson, CollectionLog.class);

		String[] commands = message.split("\\s+", 2);
		if (commands.length != 2)
		{
			log.debug("Missing page argument");
			return;
		}

		String logPageCommand = CollectionLogPage.aliasPageName(commands[1]);

		CollectionLogPage collectionLogPage = collectionLog.searchForPage(logPageCommand);
		if (collectionLogPage == null)
		{
			log.debug("No Collection Log page found");
			return;
		}
		clientThread.invoke(() ->
		{
			loadPageIcons(collectionLogPage.getItems());

			StringBuilder replacementMessageBuilder = new StringBuilder();
			int obtained = 0;
			for (CollectionLogItem item : collectionLogPage.getItems())
			{
				if (!item.isObtained())
				{
					continue;
				}
				obtained++;

				String itemReplace = "<img=" + loadedCollectionLogIcons.get(item.getId()) + ">";
				if (item.getQuantity() > 1)
				{
					itemReplace += "x" + item.getQuantity();
				}
				itemReplace += "  ";
				replacementMessageBuilder.append(itemReplace);
			}
			String replacementMessage = collectionLogPage.getName() + ": " + obtained + "/" + collectionLogPage.getItems().size() + " ";
			replacementMessage += replacementMessageBuilder.toString();
			chatMessage.getMessageNode().setValue(replacementMessage);
			client.runScript(ScriptID.BUILD_CHATBOX);
		});
	}

	private void loadPageIcons(List<CollectionLogItem> collectionLogItems)
	{
		List<CollectionLogItem> itemsToLoad = collectionLogItems
				.stream()
				.filter(item -> !loadedCollectionLogIcons.containsKey(item.getId()))
				.collect(Collectors.toList());

		final IndexedSprite[] modIcons = client.getModIcons();

		final IndexedSprite[] newModIcons = Arrays.copyOf(modIcons, modIcons.length + itemsToLoad.size());
		int modIconIdx = modIcons.length;

		for (int i = 0; i < itemsToLoad.size(); i++)
		{
			final CollectionLogItem item = itemsToLoad.get(i);
			final ItemComposition itemComposition = itemManager.getItemComposition(item.getId());
			final BufferedImage image = ImageUtil.resizeImage(itemManager.getImage(itemComposition.getId()), 18, 16);
			final IndexedSprite sprite = ImageUtil.getImageIndexedSprite(image, client);
			final int spriteIndex = modIconIdx + i;

			newModIcons[spriteIndex] = sprite;
			loadedCollectionLogIcons.put(item.getId(), spriteIndex);
		}

		client.setModIcons(newModIcons);
	}

	@Override
	protected void shutDown()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			setCollectionLogTitle(COLLECTION_LOG_TITLE);
		}

		clientToolbar.removeNavigation(navigationButton);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		if (configChanged.getKey().equals("upload_collection_log"))
		{
			collectionLogPanel.loadLoggedInState();
		}

		if (configChanged.getKey().equals("show_collection_log_panel"))
		{
			if (configChanged.getNewValue().equals("false"))
			{
				clientToolbar.removeNavigation(navigationButton);
			}
			else
			{
				clientToolbar.addNavigation(navigationButton);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (!isValidWorldType())
		{
			return;
		}

		if (gameStateChanged.getGameState() == GameState.LOGGING_IN)
		{
			userHash = getUserHash();
			SwingUtilities.invokeLater(() -> collectionLogPanel.loadLoggedInState());
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN ||
			gameStateChanged.getGameState() == GameState.HOPPING)
		{
			saveCollectionLogData();
			collectionLogData = null;
			SwingUtilities.invokeLater(() -> collectionLogPanel.loadLoggedOutState());
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
		}

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

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == WidgetID.ADVENTURE_LOG_ID)
		{
			Widget adventureLog = client.getWidget(WidgetInfo.ADVENTURE_LOG);
			if (adventureLog != null)
			{
				// Children are rendered  on tick after widget load. Invoke later to prevent null children on adventure log widget
				clientThread.invokeLater(() -> {
					Matcher adventureLogUser = ADVENTURE_LOG_TITLE_PATTERN.matcher(adventureLog.getChild(1).getText());
					if (adventureLogUser.find())
					{
						isPohOwner = adventureLogUser.group(1).equals(client.getLocalPlayer().getName());
					}
				});

			}
		}
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

		String filePath;
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
	public void saveCollectionLogData()
	{
		if (collectionLogData == null || !isValidWorldType())
		{
			return;
		}

		saveCollectionLogDataToFile(false);

		if (config.uploadCollectionLog())
		{
			String username = client.getLocalPlayer().getName();
			String accountType = client.getAccountType().toString();
			JsonObject saveData = collectionLogData; // copy data to prevent sending null on logout

			new Thread(() -> {
				try
				{
					apiClient.createUser(
							username,
							accountType,
							userHash
					);

					if (!collectionLogExists())
					{
						apiClient.createCollectionLog(saveData, userHash);
					}
					else
					{
						apiClient.updateCollectionLog(saveData, userHash);
					}
				}
				catch (IOException e)
				{
					log.warn("Unable to save collection log data to collectionlog.net: " + e.getMessage());
				}
			}).start();
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

		collectionLogEntry.add(COLLECTION_LOG_ITEMS_KEY, items);
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

		boolean openedFromAdventureLog = client.getVarbitValue(ADVENTURE_LOG_COLLECTION_LOG_SELECTED_VARBIT_ID) != 0;
		if (openedFromAdventureLog && !isPohOwner)
		{
			return;
		}

		if (collectionLogData == null)
		{
			loadCollectionLogData();
		}

		SwingUtilities.invokeLater(() -> collectionLogPanel.loadLogOpenedState());

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

		JsonObject collectionLogTabs = collectionLogData.getAsJsonObject(COLLECTION_LOG_TABS_KEY);
		if (collectionLogTabs.has(activeTabName))
		{
			// Can happen when entries are opened in quick succession
			// Item data in the widget doesn't load properly and obtained count shows as 0
			if (prevObtainedItemCount > newObtainedItemCount)
			{
				return;
			}

			JsonObject collectionLogTab = collectionLogTabs.getAsJsonObject(activeTabName);
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
		setCollectionLogTitle();
		highlightEntries();

		SwingUtilities.invokeLater(() -> collectionLogPanel.updateMissingEntriesList());
	}

	private void setCollectionLogTitle()
	{
		String title = buildTitle();
		setCollectionLogTitle(title);
	}

	/**
	 * Updates the collection log title with updated counts
	 *
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
		JsonObject collectionLogTabs = collectionLogData.getAsJsonObject(COLLECTION_LOG_TABS_KEY);
		if (!collectionLogTabs.has(activeTabName))
		{
			return 0;
		}

		JsonObject collectionLogTabData = collectionLogTabs.getAsJsonObject(activeTabName);
		if (!collectionLogTabData.has(entryTitle))
		{
			return 0;
		}

		JsonObject collectionLogEntry = collectionLogTabData.getAsJsonObject(entryTitle);
		return getEntryItemCount(collectionLogEntry);
	}

	private int getEntryItemCount(JsonObject collectionLogEntry)
	{
		JsonArray items = collectionLogEntry.getAsJsonArray(COLLECTION_LOG_ITEMS_KEY);
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
			collectionLogTemplate = apiClient.getCollectionLogTemplate();
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
		JsonObject collectionLogTabs = collectionLogData.getAsJsonObject(COLLECTION_LOG_TABS_KEY);
		for (Map.Entry<String, JsonElement> tab : collectionLogTabs.entrySet())
		{
			for (Map.Entry<String, JsonElement> entry : tab.getValue().getAsJsonObject().entrySet())
			{
				JsonArray items = entry.getValue()
					.getAsJsonObject()
					.getAsJsonArray(COLLECTION_LOG_ITEMS_KEY);
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

	/**
	 * Deletes saved collection log data for the current user
	 */
	public void clearCollectionLogData()
	{
		String fileName = "collectionlog-" + client.getLocalPlayer().getName() + ".json";
		String filePath = COLLECTION_LOG_SAVE_DATA_DIR + File.separator + fileName;

		File savedData = new File(filePath);
		if (!savedData.delete())
		{
			log.error("Unable to delete collection log save file: " + filePath);
			return;
		}

		collectionLogData = null;
	}

	/**
	 * Recalculate obtained and total item counts based on saved
	 * collection log data for the current user
	 */
	public void recalculateTotalCounts()
	{
		int newObtained = 0;
		int newTotal = 0;
		JsonObject collectionLogTabs = collectionLogData.getAsJsonObject(COLLECTION_LOG_TABS_KEY);
		for (Map.Entry<String, JsonElement> tab : collectionLogTabs.entrySet())
		{
			JsonArray tabEntries = collectionLogTemplate.getAsJsonArray(tab.getKey());
			for (Map.Entry<String, JsonElement> entry : tab.getValue().getAsJsonObject().entrySet())
			{
				if (!tabEntries.contains(new JsonPrimitive(entry.getKey())))
				{
					tab.getValue().getAsJsonObject().remove(entry.getKey());
					continue;
				}

				JsonArray items = entry.getValue()
					.getAsJsonObject()
					.getAsJsonArray(COLLECTION_LOG_ITEMS_KEY);

				newObtained += StreamSupport.stream(items.spliterator(), false).filter(item -> {
					return item.getAsJsonObject().get("obtained").getAsBoolean();
				}).toArray().length;
				newTotal += items.size();
			}
		}

		collectionLogData.addProperty(COLLECTION_LOG_TOTAL_OBTAINED_KEY, newObtained);
		collectionLogData.addProperty(COLLECTION_LOG_TOTAL_ITEMS_KEY, newTotal);

		setCollectionLogTitle();
	}

	/**
	 * Find entries not loaded into collectionLogData
	 *
	 * @return List of missing entries
	 */
	public List<String> findMissingEntries()
	{
		List<String> missingEntries = new ArrayList<>();

		JsonObject collectionLogTabs = collectionLogData.getAsJsonObject(COLLECTION_LOG_TABS_KEY);

		for (Map.Entry<String, JsonElement> tab : collectionLogTemplate.entrySet())
		{
			JsonObject existingTab = collectionLogTabs.getAsJsonObject(tab.getKey());
			if (existingTab == null)
			{
				StreamSupport.stream(tab.getValue().getAsJsonArray().spliterator(), false).forEach(entry -> {
					missingEntries.add(entry.getAsString());
				});
				continue;
			}

			JsonArray entries = tab.getValue().getAsJsonArray();
			for (JsonElement entryName : entries)
			{

				JsonObject existingEntry = collectionLogTabs.getAsJsonObject(tab.getKey())
					.getAsJsonObject(entryName.getAsString());

				if (existingEntry == null)
				{
					missingEntries.add(entryName.getAsString());
				}
			}
		}

		return missingEntries;
	}
}
