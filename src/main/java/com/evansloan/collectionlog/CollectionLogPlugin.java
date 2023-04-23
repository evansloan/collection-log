package com.evansloan.collectionlog;

import com.evansloan.collectionlog.ui.Icon;
import com.evansloan.collectionlog.util.CollectionLogDeserializer;
import com.evansloan.collectionlog.util.JsonUtils;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.ScriptID;
import net.runelite.api.WorldType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

	private static final int COLLECTION_LOG_CONTAINER = 1;
	private static final int COLLECTION_LOG_DRAW_LIST_SCRIPT_ID = 2730;
	private static final int COLLECTION_LOG_ACTIVE_TAB_VARBIT_ID = 6905;
	private static final int COLLECTION_LOG_ACTIVE_PAGE_VARBIT_ID = 6906;

	private static final String COLLECTION_LOG_TITLE = "Collection Log";
	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log: (.*)");
	private static final String COLLECTION_LOG_TARGET = "Collection log";
	private static final String COLLECTION_LOG_EXPORT = "Export";
	private static final String COLLECTION_LOG_COMMAND_STRING = "!log";
	private static final List<String> COLLECTION_LOG_COMMAND_FILTERS = ImmutableList.of("missing", "obtained", "dupes");
	private static final Pattern COLLECTION_LOG_COMMAND_PATTERN = Pattern.compile("!log\\s*(" + String.join("|", COLLECTION_LOG_COMMAND_FILTERS) + ")?\\s*([\\w\\s]+)?", Pattern.CASE_INSENSITIVE);

	private static final int ADVENTURE_LOG_COLLECTION_LOG_SELECTED_VARBIT_ID = 12061;
	private static final Pattern ADVENTURE_LOG_TITLE_PATTERN = Pattern.compile("The Exploits of (.+)");

	private CollectionLogPanel collectionLogPanel;
	private NavigationButton navigationButton;

	@Setter
	private boolean isCollectionLogDeleted = false;
	private boolean isUserLoggedIn = false;
	private boolean userSettingsLoaded = false;
	private boolean isPohOwner = false;

	private String obtainedItemName;
	private Multiset<Integer> inventoryItems;
	private Map<Integer, Integer> loadedCollectionLogIcons;

	@Getter
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

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
	private ScheduledExecutorService executor;

	@Inject
	private ItemManager itemManager;

	@Inject
	private CollectionLogApiClient apiClient;

	@Inject
	private CollectionLogManager collectionLogManager;

	@Inject
	private JsonUtils jsonUtils;

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

		// Load all save files up front on executor thread to mitigate lag on log open
		executor.submit(() -> collectionLogManager.loadCollectionLogFiles());

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

		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			isUserLoggedIn = true;

			if (collectionLogPanel != null)
			{
				collectionLogPanel.setStatus("", false, true);
			}

			unsetOldConfigs();
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN ||
			gameStateChanged.getGameState() == GameState.HOPPING)
		{
			saveCollectionLogData();
			collectionLogManager.reset();
			resetFlags();
		}

		if (collectionLogPanel != null)
		{
			collectionLogPanel.onGameStateChanged(gameStateChanged);
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

		collectionLogManager.setUsername(client.getLocalPlayer().getName());

		executor.execute(() -> {
			UserSettings userSettings = collectionLogManager.loadUserSettingsFile();
			if (userSettings == null)
			{
				userSettings = new UserSettings();
			}
			collectionLogManager.setUserSettings(userSettings);
			collectionLogPanel.setUserSettings(userSettings);
		});

		userSettingsLoaded = true;
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == COLLECTION_LOG_DRAW_LIST_SCRIPT_ID)
		{
			clientThread.invokeLater(this::getPage);
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

		if (!entryTarget.toLowerCase().endsWith(COLLECTION_LOG_TARGET.toLowerCase()))
		{
			return;
		}

		client.createMenuEntry(1)
			.setOption(COLLECTION_LOG_EXPORT)
			.setTarget(entryTarget)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				boolean collectionLogSaved = collectionLogManager.saveCollectionLogFile(true);
				if (collectionLogSaved)
				{
					String filePath = collectionLogManager.getExportFilePath();
					String message = "Collection log exported to " + filePath;

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
			});
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
			if (!collectionLogManager.isInitialized())
			{
				collectionLogManager.initCollectionLog();
			}
			collectionLogManager.updateUniqueCounts();
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

		if (!config.allowApiConnections() || !isSaved)
		{
			return;
		}

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

		JsonObject collectionLogJson = collectionLogManager.getCollectionLogJsonObject();
		JsonObject userSettingsJson = collectionLogManager.getUserSettingsJsonObject();

		uploadCollectionLog(username, accountType, accountHash, isFemale, userSettingsJson, collectionLogJson);
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

		List<CollectionLogItem> items = pageToUpdate.getItems();
		items.clear();

		Widget[] widgetItems = itemsContainer.getDynamicChildren();
		for (Widget widgetItem : widgetItems)
		{
			String itemName = itemManager.getItemComposition(widgetItem.getItemId()).getMembersName();
			boolean isObtained = widgetItem.getOpacity() == 0;
			int quantity = isObtained ? widgetItem.getItemQuantity() : 0;

			items.add(new CollectionLogItem(
				widgetItem.getItemId(),
				itemName,
				quantity,
				isObtained,
				items.size()
			));

			if (config.showQuantityForAllObtainedItems() && quantity > 0)
			{
				widgetItem.setItemQuantityMode(ItemQuantityMode.ALWAYS);
			}
		}

		Widget[] children = pageHead.getDynamicChildren();
		if (children.length < 3)
		{
			// Page does not have kill count widgets, mark as updated and early return
			pageToUpdate.setUpdated(true);
			return;
		}

		List<CollectionLogKillCount> killCounts = pageToUpdate.getKillCounts();
		killCounts.clear();

		Widget[] killCountWidgets = Arrays.copyOfRange(children, 2, children.length);
		for (Widget killCountWidget : killCountWidgets)
		{
			String killCountString = killCountWidget.getText();
			CollectionLogKillCount killCount = CollectionLogKillCount.fromString(killCountString, killCounts.size());
			killCounts.add(killCount);
		}

		pageToUpdate.setUpdated(true);
	}

	/**
	 * Load the current entry being viewed in the collection log
	 * and get/update relevant information contained in the entry
	 */
	private void getPage()
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

		Widget activeTab = getActiveTab();
		if (activeTab == null)
		{
			return;
		}

		String activeTabName = Text.removeTags(activeTab.getName());
		CollectionLogTab collectionLogTab = collectionLogManager.getTabByName(activeTabName);
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

		CollectionLogPage pageToUpdate = collectionLogManager.getPageByName(pageTitle);
		if (pageToUpdate == null)
		{
			return;
		}

		int prevObtainedItemCount = pageToUpdate.getObtainedItemCount();

		updatePage(pageHead, pageToUpdate);

		int newObtainedItemCount = pageToUpdate.getObtainedItemCount();

		if (collectionLogManager.getTabByName(activeTabName) != null)
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

		CollectionLog collectionLog = collectionLogManager.getCollectionLog();

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
		if (pageList == null)
		{
			return;
		}

		Widget[] pageNameWidgets = pageList.getDynamicChildren();
		for (Widget pageNameWidget : pageNameWidgets)
		{
			String pageName = pageNameWidget.getText()
				.replace(" *", "");
			CollectionLogPage collectionLogPage = collectionLogManager.getPageByName(pageName);

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
		if (pageList == null)
		{
			return;
		}

		int pageIndex = client.getVarbitValue(COLLECTION_LOG_ACTIVE_PAGE_VARBIT_ID);
		Widget pageNameWidget = pageList.getDynamicChildren()[pageIndex];

		String pageName = pageNameWidget.getText()
			.replace(" *", "");
		CollectionLogPage collectionLogPage = collectionLogManager.getPageByName(pageName);

		Color pageNameColor = getPageNameColor(collectionLogPage);
		pageNameWidget.setTextColor(pageNameColor.getRGB());
		pageNameWidget.setText(pageName);
	}

	/**
	 * Get the current tab opened in the collection log
	 *
	 * @return Open collection log tab
	 */
	private Widget getActiveTab()
	{
		Widget tabsWidget = client.getWidget(WidgetInfo.COLLECTION_LOG_TABS);
		if (tabsWidget == null)
		{
			return null;
		}

		int tabIndex = client.getVarbitValue(COLLECTION_LOG_ACTIVE_TAB_VARBIT_ID);
		return tabsWidget.getStaticChildren()[tabIndex];
	}

	/**
	 * Get the current page list opened in the collection log
	 *
	 * @return Open collection log page list
	 */
	private Widget getActivePageList()
	{
		Widget tab = getActiveTab();
		if (tab == null)
		{
			return null;
		}

		String tabName = Text.removeTags(tab.getName());
		int listIndex = CollectionLogList.valueOf(tabName.toUpperCase()).getListIndex();
		return client.getWidget(WidgetID.COLLECTION_LOG_ID, listIndex);
	}

	/**
	 * Get the appropriate page name highlight color based on configs/items obtained
	 *
	 * @param collectionLogPage Page to highlight
	 * @return Page name highlight color
	 */
	private Color getPageNameColor(CollectionLogPage collectionLogPage)
	{
		Color pageNameColor = CollectionLogConfig.DEFAULT_ORANGE;
		int obtainedItemCount = collectionLogPage.getObtainedItemCount();

		if (obtainedItemCount == 0 && config.highlightIncompletePages())
		{
			pageNameColor = config.emptyHighlightColor();
		}
		if (obtainedItemCount > 0 && obtainedItemCount < collectionLogPage.getItems().size() && config.highlightIncompletePages())
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

		if (!collectionLogManager.isInitialized())
		{
			return "";
		}

		CollectionLog collectionLog = collectionLogManager.getCollectionLog();

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
			int totalItems = collectionLog.getTotalItems();

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

		try
		{
			apiClient.getCollectionLog(Text.sanitize(username), new Callback()
			{
				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e)
				{
					log.error("Unable to resolve !log command: " + e.getMessage());
					clientThread.invoke(() -> getCommandOutput(chatMessage, message, null));
				}

				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
				{
					JsonObject collectionLogJson = apiClient.processResponse(response);
					CollectionLog collectionLog = jsonUtils.fromJsonObject(
						collectionLogJson.getAsJsonObject("collectionLog"),
						CollectionLog.class,
						new CollectionLogDeserializer()
					);
					clientThread.invoke(() -> getCommandOutput(chatMessage, message, collectionLog));

					response.close();
				}
			});
		}
		catch (IOException e)
		{
			log.error("Unable to resolve !log command: " + e.getMessage());
		}
	}

	private void getCommandOutput(ChatMessage chatMessage, String message, CollectionLog collectionLog)
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
	 * Get the collection log title widget
	 *
	 * @return Collection log title widget
	 */
	private Widget getCollectionLogTitle()
	{
		Widget collLogContainer = client.getWidget(WidgetID.COLLECTION_LOG_ID, COLLECTION_LOG_CONTAINER);

		if (collLogContainer == null)
		{
			return null;
		}

		return collLogContainer.getDynamicChildren()[1];
	}

	/**
	 * Update newly obtained item
	 *
	 * @param itemStack Obtained item
	 */
	private void updateObtainedItem(ItemStack itemStack)
	{
		boolean itemUpdated = collectionLogManager.updateObtainedItem(itemStack);

		if (!itemUpdated)
		{
			collectionLogPanel.setStatus(
				"Unable to update data for item \"" + obtainedItemName + "\". Open the collection log page(s)" +
				"it exists in to update.",
				true,
				true
			);
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

	private void uploadCollectionLog(String username, String accountType, String accountHash, boolean isFemale, JsonObject userSettings, JsonObject collectionLog)
	{
		apiClient.updateUser(username, accountType, accountHash, isFemale, userSettings, uploadCollectionLogCallback(() -> {
			apiClient.updateCollectionLog(collectionLog, accountHash, uploadCollectionLogCallback(null));
		}));
	}

	private Callback uploadCollectionLogCallback(Runnable onSuccess)
	{
		String errorDisplay = "Error uploading data to collectionlog.net. Check Runelite logs for full error.";
		String errorLog = "Unable to upload data to collectionlog.net: ";
		return new Callback()
		{
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{
				log.error(errorLog + e.getMessage());
				collectionLogPanel.setStatus(
					errorDisplay,
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

				if (onSuccess != null)
				{
					onSuccess.run();
				}
			}
		};
	}
}
