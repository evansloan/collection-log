package com.evansloan.collectionlog;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Provides;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
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
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Collection Log",
	description = "Add items obtained/total items to the top of the collection log",
	tags = {"collection", "log"}
)
public class CollectionLogPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "collectionlog";
	private static final String CONFIG_SHOW_PANEL = "show_collection_log_panel";
	private static final String CONFIG_API_CONNECTIONS = "upload_collection_log";

	private static final int COLLECTION_LOG_CONTAINER = 1;
	private static final int COLLECTION_LOG_DRAW_LIST_SCRIPT_ID = 2730;
	private static final int COLLECTION_LOG_DEFAULT_HIGHLIGHT = 901389;
	private static final int COLLECTION_LOG_ACTIVE_TAB_SPRITE_ID = 2283;

	private static final String COLLECTION_LOG_TITLE = "Collection Log";
	private static final Pattern COLLECTION_LOG_TITLE_REGEX = Pattern.compile("Collection Log - (\\d+)/(\\d+)");
	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log: (.*)");
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
	private static final List<String> COLLECTION_LOG_COMMAND_FILTERS = ImmutableList.of("missing", "obtained", "dupes");
	private static final Pattern COLLECTION_LOG_COMMAND_PATTERN = Pattern.compile("!log\\s*(" + String.join("|", COLLECTION_LOG_COMMAND_FILTERS) + ")?\\s*([\\w\\s]+)?", Pattern.CASE_INSENSITIVE);

	private static final int ADVENTURE_LOG_COLLECTION_LOG_SELECTED_VARBIT_ID = 12061;
	private static final Pattern ADVENTURE_LOG_TITLE_PATTERN = Pattern.compile("The Exploits of (.+)");

	private CollectionLogPanel collectionLogPanel;
	private NavigationButton navigationButton;

	private boolean isPohOwner = false;
	private String obtainedItemName;
	private Multiset<Integer> inventoryItems;
	private Map<Integer, Integer> loadedCollectionLogIcons;

	@Getter
	private JsonObject collectionLogData;

	@Getter
	private JsonObject collectionLogTemplate;

	@Inject
	private Client client;

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

	@Inject
	private CollectionLogConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private CollectionLogApiClient apiClient;

	@Inject
	private EventBus eventBus;

	@Inject
	private CollectionLogManager collectionLogManager;

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
			isUserLoggedIn = true;
			unsetOldConfigs();
		}

		if (config.showCollectionLogSidePanel())
		{
			initPanel();
		}
		loadedCollectionLogIcons = new HashMap<>();
		chatCommandManager.registerCommandAsync(COLLECTION_LOG_COMMAND_STRING, this::collectionLogLookup);
	}

	@Override
	protected void shutDown()
	{
		destroyPanel();
		chatCommandManager.unregisterCommand(COLLECTION_LOG_COMMAND_STRING);
		loadedCollectionLogIcons.clear();
	}

	private void initPanel()
	{
		collectionLogPanel = new CollectionLogPanel(this, collectionLogManager, clientThread, config);
		collectionLogPanel.create(client.getGameState());

		if (navigationButton == null)
		{
			final BufferedImage navigationButtonIcon = Icon.COLLECTION_LOG_TOOLBAR.getImage();
			navigationButton = NavigationButton.builder()
				.tooltip(COLLECTION_LOG_TITLE)
				.icon(navigationButtonIcon)
				.panel(collectionLogPanel)
				.priority(10)
				.build();
		}

		clientToolbar.addNavigation(navigationButton);
	}

	private void destroyPanel()
	{
		collectionLogPanel = null;
		clientToolbar.removeNavigation(navigationButton);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		if (configChanged.getKey().equals(CONFIG_SHOW_PANEL))
		{
			if (config.showCollectionLogSidePanel())
			{
				initPanel();
			}
			else
			{
				destroyPanel();
			}
		}

		if (collectionLogPanel != null)
		{
			collectionLogPanel.onConfigChanged(configChanged);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (!isValidWorldType())
		{
			return;
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN ||
			gameStateChanged.getGameState() == GameState.HOPPING)
		{
			saveCollectionLogData();
			collectionLogManager.clearCollectionLog();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (userSettingsLoaded)
		{
			return;
		}

		if (!isUserLoggedIn)
		{
			return;
		}

		// TODO: Load userSettings on executor thread
		UserSettings userSettings = collectionLogManager.loadUserSettingsFile();
		if (userSettings == null)
		{
			userSettings = new UserSettings();
		}
		collectionLogManager.setUserSettings(userSettings);
		collectionLogPanel.setUserSettings(userSettings);

		userSettingsLoaded = true;
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
			if (adventureLog == null)
			{
				return;
			}

			// Children are rendered on tick after widget load. Invoke later to prevent null children on adventure log widget
			clientThread.invokeLater(() -> {
				Matcher adventureLogUser = ADVENTURE_LOG_TITLE_PATTERN.matcher(adventureLog.getChild(1).getText());
				if (adventureLogUser.find())
				{
					isPohOwner = adventureLogUser.group(1).equals(client.getLocalPlayer().getName());
				}
			});
		}

		if (widgetLoaded.getGroupId() == WidgetID.COLLECTION_LOG_ID)
		{
			updateUniqueItems();
		}
	}

	@Subscribe
	public void onLootReceived(LootReceived lootReceived)
	{
		if (obtainedItemName == null)
		{
			inventoryItems = null;
			return;
		}

		ItemStack obtainedItem = null;
		Collection<ItemStack> items = lootReceived.getItems();
		for (ItemStack item : items)
		{
			ItemComposition itemComp = itemManager.getItemComposition(item.getId());
			if (itemComp.getName().equals(obtainedItemName))
			{
				obtainedItem = item;
			}
		}

		if (obtainedItem == null)
		{
			obtainedItemName = null;
			inventoryItems = null;
			return;
		}

		updateObtainedItem(obtainedItem);
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		Matcher m = COLLECTION_LOG_ITEM_REGEX.matcher(chatMessage.getMessage());
		if (!m.matches())
		{
			return;
		}

		obtainedItemName = Text.removeTags(m.group(1));

		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			obtainedItemName = null;
			inventoryItems = null;
			return;
		}

		// Get inventory prior to onItemContainerChanged event
		Arrays.stream(inventory.getItems())
			.forEach(item -> inventoryItems.add(item.getId(), item.getQuantity()));

		// Defer to onItemContainerChanged or onLootReceived
	}

	@Subscribe
	private void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		if (itemContainerChanged.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		if (obtainedItemName == null)
		{
			inventoryItems = HashMultiset.create();
			return;
		}

		if (inventoryItems == null)
		{
			inventoryItems = HashMultiset.create();
		}

		// Need to build a diff of inventory items prior to item appearing in inventory and current inventory items
		// Necessary to find item that may have non-unique name (Ancient page, decorative armor) that
		// may already be in inventory
		ItemContainer inventory = itemContainerChanged.getItemContainer();
		Multiset<Integer> currentInventoryItems = HashMultiset.create();
		Arrays.stream(inventory.getItems())
			.forEach(item -> currentInventoryItems.add(item.getId(), item.getQuantity()));
		Multiset<Integer> invDiff = Multisets.difference(currentInventoryItems, inventoryItems);

		ItemStack obtainedItemStack = null;
		for (Multiset.Entry<Integer> item : invDiff.entrySet())
		{
			ItemComposition itemComp = itemManager.getItemComposition(item.getElement());
			if (itemComp.getName().equals(obtainedItemName))
			{
				obtainedItemStack = new ItemStack(
					item.getElement(),
					item.getCount(),
					client.getLocalPlayer().getLocalLocation()
				);

				break;
			}
		}

		if (obtainedItemStack == null)
		{
			// Opening clue casket triggers onItemContainerChanged event before clue items
			// appear in inventory. Fall through to onLootReceived to find obtained item(s)
			if (client.getWidget(WidgetInfo.CLUE_SCROLL_REWARD_ITEM_CONTAINER) != null)
			{
				return;
			}

			obtainedItemName = null;
			inventoryItems = HashMultiset.create();
			return;
		}

		updateObtainedItem(obtainedItemStack);
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
			filePath = COLLECTION_LOG_EXPORT_DIR + File.separator + fileName;
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
		if (!collectionLogManager.isInitialized())
		{
			collectionLogPanel.setStatus(
				"Unable to save collection log, please open the collection log in game and try again.",
				true,
				true
			);
			return;
		}

		if (!isValidWorldType())
		{
			collectionLogPanel.setStatus(
				"Unable to save collection log, please log in to a normal free to play or members world and try again",
				true,
				true
			);
			return;
		}

		if (isCollectionLogDeleted)
		{
			return;
		}

		boolean isSaved = collectionLogManager.saveCollectionLogFile(false);
		isSaved = isSaved && collectionLogManager.saveUserSettingsFile();
		String statusMessage = isSaved ? null : "Unable to save collection log data. Check Runelite logs for full error.";
		collectionLogPanel.setStatus(statusMessage, isSaved, !config.allowApiConnections());

		if (config.allowApiConnections())
		{
			if (client.getAccountHash() == -1)
			{
				return;
			}

			Player localPlayer = client.getLocalPlayer();
			String username = localPlayer.getName();

			String accountHash = String.valueOf(client.getAccountHash());
			String accountType = client.getAccountType().toString();

			// Used to display proper farming outfit on site
			boolean isFemale = localPlayer.getPlayerComposition().getGender() == 1;

			// Copy data to prevent sending null on logout
			JsonObject collectionLogJson = collectionLogManager.getCollectionLogJsonObject();
			JsonObject userSettingsJson = collectionLogManager.getUserSettingsJsonObject();

			new Thread(() -> {
				try
				{
					apiClient.createUser(
						username,
						accountType,
						accountHash,
						isFemale,
						userSettingsJson
					);

					if (!collectionLogExists(accountHash))
					{
						apiClient.createCollectionLog(collectionLogJson, accountHash, new Callback()
						{
							@Override
							public void onFailure(@NonNull Call call, @NonNull IOException e)
							{
								log.error("Unable to create collectionlog.net profile: " + e.getMessage());
								collectionLogPanel.setStatus(
									"Error creating collectionlog.net profile. Check Runelite logs for full error.",
									true,
									true
								);
							}

							@Override
							public void onResponse(@NonNull Call call, @NonNull Response response)
							{
								response.close();
								if (!response.isSuccessful())
								{
									return;
								}
								collectionLogPanel.setStatus(
									"Collection log successfully uploaded to collectionlog.net",
									false,
									true
								);
							}
						});
					}
					else
					{
						apiClient.updateCollectionLog(collectionLogJson, accountHash, new Callback()
						{
							@Override
							public void onFailure(@NonNull Call call, @NonNull IOException e)
							{
								log.error("Unable to upload data to collectionilog.net: " + e.getMessage());
								collectionLogPanel.setStatus(
									"Error uploading data to collectionlog.net. Check Runelite logs for full error.",
									true,
									true
								);
							}

							@Override
							public void onResponse(@NonNull Call call, @NonNull Response response)
							{
								response.close();
								collectionLogPanel.setStatus(
									"Collection log successfully uploaded to collectionlog.net",
									false,
									true
								);
							}
						});
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
	 * @param pageHead Collection log entry to update
	 */
	private void updatePage(Widget pageHead, CollectionLogPage pageToUpdate)
	{
		Widget itemsContainer = client.getWidget(WidgetInfo.COLLECTION_LOG_ENTRY_ITEMS);
		if (itemsContainer == null)
		{
			return;
		}
		Widget[] widgetItems = itemsContainer.getDynamicChildren();

		Map<String, Widget> itemsToUpdate = new HashMap<>();
		for (Widget widgetItem : widgetItems)
		{
			String itemName = itemManager.getItemComposition(widgetItem.getItemId())
				.getMembersName();
			itemsToUpdate.put(itemName, widgetItem);
		}
		pageToUpdate.updateItems(itemsToUpdate);

		Widget[] children = pageHead.getDynamicChildren();
		if (children.length < 3)
		{
			pageToUpdate.setUpdated(true);
			return;
		}

		Widget[] killCountWidgets = Arrays.copyOfRange(children, 2, children.length);
		List<String> killCountStrings = Arrays.stream(killCountWidgets)
			.map(Widget::getText)
			.collect(Collectors.toList());
		pageToUpdate.updateKillCounts(killCountStrings);

		pageToUpdate.setUpdated(true);
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

		if (collectionLogManager.getCollectionLog() == null)
		{
			collectionLogManager.initCollectionLog();
		}

		CollectionLog collectionLog = collectionLogManager.getCollectionLog();

		String activeTabName = getActiveTabName();
		if (activeTabName == null)
		{
			return;
		}

		CollectionLogTab collectionLogTab = collectionLog.getTabs().get(activeTabName);
		if (collectionLogTab == null)
		{
			return;
		}

		Widget pageHead = client.getWidget(WidgetInfo.COLLECTION_LOG_ENTRY_HEADER);
		if (pageHead == null)
		{
			return;
		}

		String pageTitle = pageHead.getDynamicChildren()[0].getText();
		if (!collectionLogTab.containsPage(pageTitle))
		{
			return;
		}

		CollectionLogPage pageToUpdate = collectionLog.searchForPage(pageTitle);
		if (pageToUpdate == null)
		{
			return;
		}

		int prevObtainedItemCount = pageToUpdate.getObtainedItemCount();

		updatePage(pageHead, pageToUpdate);

		int newObtainedItemCount = pageToUpdate.getObtainedItemCount();

		if (collectionLog.getTabs().containsKey(activeTabName))
		{
			// Can happen when entries are opened in quick succession
			// Item data in the widget doesn't load properly and obtained count shows as 0
			if (prevObtainedItemCount > newObtainedItemCount)
			{
				return;
			}
		}

		if (prevObtainedItemCount == newObtainedItemCount)
		{
			update();
			return;
		}

		int prevTotalObtained = collectionLog.getTotalObtained();
		int newTotalObtained = prevTotalObtained + (newObtainedItemCount - prevObtainedItemCount);
		collectionLog.setTotalObtained(newTotalObtained);

		update();
	}

	/**
	 * Update completed collection log entries with user specified color
	 */
	private void highlightPages()
	{
		Widget pageList = getActivePageList();
		Widget[] pageNameWidgets = pageList.getDynamicChildren();
		for (Widget pageNameWidget : pageNameWidgets)
		{
			String pageName = pageNameWidget.getText()
				.replace(" *", "");
			CollectionLogPage collectionLogPage = collectionLogManager.getCollectionLog()
				.searchForPage(pageName);

			if (!collectionLogPage.isUpdated())
			{
				pageNameWidget.setText(pageName + " *");
			}

			Color pageNameColor = getPageNameColor(collectionLogPage);
			pageNameWidget.setTextColor(pageNameColor.getRGB());
		}
	}

	private void updatePageHighlight()
	{
		Widget pageList = getActivePageList();
		int pageIndex = client.getVarbitValue(6906);
		Widget pageNameWidget = pageList.getDynamicChildren()[pageIndex];

		String pageName = pageNameWidget.getText()
			.replace(" *", "");
		CollectionLogPage collectionLogPage = collectionLogManager.getCollectionLog()
			.searchForPage(pageName);

		Color pageNameColor = getPageNameColor(collectionLogPage);
		pageNameWidget.setTextColor(pageNameColor.getRGB());
		pageNameWidget.setText(pageName);
	}

	private Widget getActivePageList()
	{
		Widget tabsWidget = client.getWidget(WidgetID.COLLECTION_LOG_ID, 3);
		if (tabsWidget == null)
		{
			return null;
		}

		int tabIndex = client.getVarbitValue(6905);
		Widget tab = tabsWidget.getStaticChildren()[tabIndex];
		if (tab == null)
		{
			return null;
		}

		String tabName = Text.removeTags(tab.getName());
		int listIndex = CollectionLogList.valueOf(tabName.toUpperCase()).getListIndex();
		return client.getWidget(WidgetID.COLLECTION_LOG_ID, listIndex);
	}

	private Color getPageNameColor(CollectionLogPage collectionLogPage)
	{
		Color pageNameColor = config.emptyHighlightColor();
		int obtainedItemCount = collectionLogPage.getObtainedItemCount();
		if (obtainedItemCount != 0 && obtainedItemCount < collectionLogPage.getItems().size())
		{
			pageNameColor = config.inProgressHighlightColor();
		}
		if (obtainedItemCount == collectionLogPage.getItems().size())
		{
			pageNameColor = config.highlightColor();
		}

		return pageNameColor;
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

		boolean displayUnique = config.displayUniqueItems();
		boolean displayTotal = config.displayTotalItems();

		CollectionLog collectionLog = collectionLogManager.getCollectionLog();
		if (collectionLog == null)
		{
			return "";
		}

		if (displayUnique)
		{
			int uniqueObtained = collectionLog.getUniqueObtained();
			int uniqueTotal = collectionLog.getUniqueItems();

			String prefix = displayTotal ? "U: " : "";
			String uniqueTitle = String.format("%s%d/%d", prefix, uniqueObtained, uniqueTotal);

			if (config.displayAsPercentage())
			{
				uniqueTitle = String.format("%s%.2f%%", prefix, ((double) uniqueObtained / uniqueTotal) * 100);
			}
			titleSections.add(uniqueTitle);
		}

		if (config.displayTotalItems())
		{
			int totalItemsObtained = collectionLog.getTotalObtained();
			int totalItems =collectionLog.getTotalItems();

			String prefix = displayUnique ? "T: " : "";
			String totalTitle = String.format("%s%d/%d", prefix, totalItemsObtained, totalItems);

			if (config.displayAsPercentage())
			{
				totalTitle = String.format("%s%.2f%%", prefix, ((double) totalItemsObtained / totalItems) * 100);
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
		collectionLogManager.updateTotalItems();
		setCollectionLogTitle();

		highlightPages();
		updatePageHighlight();
	}

	private void setCollectionLogTitle()
	{
		String title = buildTitle();
		setCollectionLogTitle(title);
	}

	/**
	 * Updates the collection log title with updated counts
	 *
	 * @param title New collection log title value
	 */
	private void setCollectionLogTitle(String title)
	{
		Widget collectionLogTitleWidget = getCollectionLogTitle();
		if (collectionLogTitleWidget == null)
		{
			return;
		}
		collectionLogTitleWidget.setText(title);
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
		catch (IOException | JsonParseException e)
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
	 * Updates the count of unique items in the collection log
	 * Parses item counts from collection log title
	 */
	private void updateUniqueItems()
	{
		if (collectionLogManager.getCollectionLog() == null)
		{
			collectionLogManager.initCollectionLog();
		}

		collectionLogManager.updateUniqueCounts();
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
				return Text.removeTags(tab.getName());
			}
		}

		return null;
	}

	/**
	 * Check if a collection log exists for the current user on collectionlog.net
	 *
	 * @return collectionlog.net collection log exists
	 */
	private boolean collectionLogExists(String accountHash)
	{
		try
		{
			return apiClient.getCollectionLogExists(accountHash);
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
	 * Looks up and then replaces !log chat messages
	 *
	 * @param chatMessage The ChatMessage event
	 * @param message Text of the message
	 */
	private void collectionLogLookup(ChatMessage chatMessage, String message)
	{
		Player localPlayer = client.getLocalPlayer();
		String username = chatMessage.getName();
		if (chatMessage.getType().equals(ChatMessageType.PRIVATECHATOUT))
		{
			username = localPlayer.getName();
		}

		CollectionLog collectionLog;
		try
		{
			collectionLog = apiClient.getCollectionLog(sanitize(username));
		}
		catch (IOException e)
		{
			return;
		}
		clientThread.invoke(() ->
		{
			String replacementMessage;

			Matcher commandMatcher = COLLECTION_LOG_COMMAND_PATTERN.matcher(message);
			if (!commandMatcher.matches())
			{
				return;
			}

			String commandFilter = commandMatcher.group(1);
			String commandPage = commandMatcher.group(2);

			if (collectionLog == null)
			{
				replacementMessage = "No Collection Log data found for user.";
			}
			else if (commandPage == null)
			{
				replacementMessage = "Collection Log: " + collectionLog.getUniqueObtained() + "/" + collectionLog.getUniqueItems();
			}
			else
			{
				String pageArgument = CollectionLogPage.aliasPageName(commandPage);
				CollectionLogPage collectionLogPage;
				if (pageArgument.equals("any"))
				{
					collectionLogPage = collectionLog.randomPage();
				}
				else
				{
					collectionLogPage = collectionLog.searchForPage(pageArgument);
				}

				if (collectionLogPage == null)
				{
					replacementMessage = "No Collection Log page found.";
				}
				else
				{
					loadPageIcons(collectionLogPage.getItems());
					replacementMessage = buildMessageReplacement(collectionLogPage, commandFilter);
				}
			}

			chatMessage.getMessageNode().setValue(replacementMessage);
			client.runScript(ScriptID.BUILD_CHATBOX);
		});
	}

	/**
	 * Loads a list of Collection Log items into the client's mod icons.
	 *
	 * @param collectionLogItems List of items to load
	 */
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

	/**
	 * Builds the replacement messages for the !log command with a page argument
	 *
	 * @param collectionLogPage Page to format into a chat message
	 * @return Replacement message
	 */
	private String buildMessageReplacement(CollectionLogPage collectionLogPage, String commandFilter)
	{

		if (commandFilter == null)
		{
			commandFilter = "obtained";
		}

		List<CollectionLogItem> items = collectionLogPage.applyItemFilter(commandFilter.toLowerCase());
		StringBuilder itemBuilder = new StringBuilder();
		for (CollectionLogItem item : items)
		{
			String itemString = "<img=" + loadedCollectionLogIcons.get(item.getId()) + ">";
			if (item.getQuantity() > 1)
			{
				itemString += "x" + item.getQuantity();
			}
			itemString += "  ";
			itemBuilder.append(itemString);
		}

		String replacementMessage = collectionLogPage.getName() + " (" + commandFilter + "): " + items.size() + "/" + collectionLogPage.getItems().size() + " ";
		replacementMessage += itemBuilder.toString();

		return replacementMessage;
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

	private Widget getCollectionLogTitle()
	{
		Widget collLogContainer = client.getWidget(WidgetID.COLLECTION_LOG_ID, COLLECTION_LOG_CONTAINER);

		if (collLogContainer == null)
		{
			return null;
		}

		return collLogContainer.getDynamicChildren()[1];
	}

	private void updateObtainedItem(ItemStack itemStack)
	{
		if (collectionLogData == null)
		{
			loadCollectionLogData();
		}

		CollectionLog collectionLog = collectionLogManager.getCollectionLog();
		if (collectionLog == null)
		{
			collectionLogManager.initCollectionLog();
		}

		boolean itemFound = false;
		for (CollectionLogTab tab : collectionLog.getTabs().values())
		{
			for (CollectionLogPage page : tab.getPages().values())
			{
				CollectionLogItem existingItem = page.getItemById(itemStack.getId());
				if (existingItem == null)
				{
					continue;
				}

				itemFound = true;
				existingItem.setQuantity(existingItem.getQuantity() + itemStack.getQuantity());
				existingItem.setObtained(true);

				collectionLog.setTotalObtained(collectionLog.getTotalObtained() + 1);
			}
		}

		if (itemFound)
		{
			collectionLog.setUniqueObtained(collectionLog.getUniqueObtained() + 1);
		}

		obtainedItemName = null;
		inventoryItems = HashMultiset.create();
	}

	/**
	 * Delete profile from collectionlog.net
	 */
	public void deleteCollectionLog()
	{
		String username = client.getLocalPlayer().getName();
		String accountHash = String.valueOf(client.getAccountHash());
		apiClient.deleteCollectionLog(username, accountHash, new Callback()
		{
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{
				log.error("Unable to delete collectionlog.net profile: " + e.getMessage());
				collectionLogPanel.setStatus(
					"Error deleting collectionlog.net profile. Check Runelite logs for full error.",
					true,
					true
				);
				isCollectionLogDeleted = false;
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response)
			{
				response.close();
				collectionLogPanel.setStatus(
					"collectionlog.net profile successfully deleted.",
					false,
					true
				);
				isCollectionLogDeleted = true;
			}
		});
	}

	private void unsetOldConfigs()
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

	private void resetFlags()
	{
		isUserLoggedIn = false;
		isCollectionLogDeleted = false;
		userSettingsLoaded = false;
	}
}
