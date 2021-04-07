package com.evansloan.collectionlog;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Provides;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
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
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.LootManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
@PluginDescriptor(
	name = "Collection Log",
	description = "Add items obtained/total items to the top of the collection log",
	tags = {"collection", "log"}
)
public class CollectionLogPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "collectionlog";
	private static final String OBTAINED_COUNTS = "obtained_counts";
	private static final String OBTAINED_ITEMS = "obtained_items";
	private static final String TOTAL_ITEMS = "total_items";
	private static final String KILL_COUNTS = "kill_counts";

	private static final int COLLECTION_LOG_GROUP_ID = 621;
	private static final int COLLECTION_LOG_CONTAINER = 1;
	private static final int COLLECTION_LOG_CATEGORY_HEAD = 19;
	private static final int COLLECTION_LOG_CATEGORY_ITEMS = 35;
	private static final int COLLECTION_LOG_DRAW_LIST_SCRIPT_ID = 2730;
	private static final int COLLECTION_LOG_CATEGORY_VARBIT_INDEX = 2049;
	private static final int COLLECTION_LOG_DEFAULT_HIGHLIGHT = 901389;

	private static final String COLLECTION_LOG_TITLE = "Collection Log";
	private static final Pattern COLLECTION_LOG_TITLE_REGEX = Pattern.compile("Collection Log - (?:U: )?(?:(\\d+)/(\\d+)|([\\d\\.%]+))(?: T: \\d+/\\d+)?");
	private static final String COLLECTION_LOG_TARGET = "Collection log";
	private static final String COLLECTION_LOG_EXPORT = "Export";
	private static final File COLLECTION_LOG_EXPORT_DIR = new File(RUNELITE_DIR, "collectionlog");

	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log: .*");

	private final Gson GSON = new Gson();

	private Map<String, Integer> obtainedCounts = new HashMap<>();
	private Map<String, List<CollectionLogItem>> obtainedItems = new HashMap<>();
	private Map<String, Integer> killCounts = new HashMap<>();

	private boolean newCollectionLogItem = false;
	private Multiset<Integer> inventory;

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
	private LootManager lootManager;

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
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP, "completed_categories");
			configManager.unsetConfiguration(CONFIG_GROUP, "highlight_completed");
			configManager.unsetConfiguration(CONFIG_GROUP, "new_item_chat_message");

			loadConfig();
			update();
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
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			loadConfig();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == COLLECTION_LOG_DRAW_LIST_SCRIPT_ID)
		{
			clientThread.invokeLater(this::getCategory);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (varbitChanged.getIndex() == COLLECTION_LOG_CATEGORY_VARBIT_INDEX)
		{
			clientThread.invokeLater(this::getCategory);
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
		if (!entry.getTarget().endsWith(COLLECTION_LOG_TARGET))
		{
			return;
		}

		MenuEntry menuEntry = new MenuEntry();
		menuEntry.setOption(COLLECTION_LOG_EXPORT);
		menuEntry.setTarget(entry.getTarget());
		menuEntry.setType(MenuAction.RUNELITE.getId());
		menuEntry.setIdentifier(entry.getIdentifier());
		client.setMenuEntries(ArrayUtils.insert(1, client.getMenuEntries(), menuEntry));
	}


	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOption().equals(COLLECTION_LOG_EXPORT) && event.getMenuTarget().endsWith(COLLECTION_LOG_TARGET))
		{
			exportItems();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		if (COLLECTION_LOG_ITEM_REGEX.matcher(chatMessage.getMessage()).matches())
		{
			newCollectionLogItem = true;
			getInventory();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		if (itemContainerChanged.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		if (newCollectionLogItem)
		{
			WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
			Collection<ItemStack> items = lootManager.getItemSpawns(playerLocation);
			getInventoryDiff(itemContainerChanged.getItemContainer(), items);
		}
	}

	private void getInventory()
	{
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer != null)
		{
			inventory = HashMultiset.create();
			Arrays.stream(itemContainer.getItems()).forEach(item -> inventory.add(item.getId(), item.getQuantity()));
		}
	}

	private void getInventoryDiff(ItemContainer itemContainer, Collection<ItemStack> items)
	{
		if (inventory == null)
		{
			newCollectionLogItem = false;
			return;
		}

		Multiset<Integer> currentInventory = HashMultiset.create();
		Arrays.stream(itemContainer.getItems()).forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));
		items.forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

		final Multiset<Integer> diff = Multisets.difference(currentInventory, inventory);

		List<ItemStack> newItems = diff.entrySet().stream()
			.map(item -> new ItemStack(item.getElement(), item.getCount(), client.getLocalPlayer().getLocalLocation()))
			.collect(Collectors.toList());

		if (newItems.isEmpty())
		{
			newCollectionLogItem = false;
			inventory = null;
			return;
		}

		for (ItemStack itemStack : newItems)
		{
			ItemComposition itemComp = itemManager.getItemComposition(itemStack.getId());
			updateObtainedItems(itemComp, itemStack.getQuantity());
		}

		newCollectionLogItem = false;
		inventory = null;
	}

	private void exportItems()
	{
		COLLECTION_LOG_EXPORT_DIR.mkdir();

		String fileName = new SimpleDateFormat("'collectionlog-'yyyyMMdd'T'HHmmss'.json'").format(new Date());
		String filePath = COLLECTION_LOG_EXPORT_DIR + File.separator  + fileName;

		try (JsonWriter writer = new JsonWriter(new FileWriter(filePath)))
		{
			writer.setIndent("  ");
			writer.beginObject();
			for (Map.Entry<String, List<CollectionLogItem>> entry : obtainedItems.entrySet())
			{
				String categoryName = entry.getKey();

				writer.name(categoryName);
				writer.beginObject();

				if (killCounts.containsKey(categoryName))
				{
					writer.name("kill_count").value(killCounts.get(categoryName));
				}
				else
				{
					writer.name("kill_count").nullValue();
				}

				writer.name("items");
				writer.beginArray();
				for (CollectionLogItem item : entry.getValue())
				{
					writer.beginObject();
					writer.name("id").value(item.getId());
					writer.name("name").value(itemManager.getItemComposition(item.getId()).getName());
					writer.name("obtained").value(item.getQuantity() > 0);
					writer.name("quantity").value(item.getQuantity());
					writer.endObject();
				}
				writer.endArray();
				writer.endObject();
			}
			writer.endObject();

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
		catch (IOException e)
		{
			log.error("Unable to export Collection log items: " + e.getMessage());
			if (config.notifyOnExport())
			{
				notifier.notify("Unable to export collection log: " + e.getMessage());
			}
		}
	}

	private void getItems(String categoryTitle)
	{
		Widget itemsContainer = client.getWidget(COLLECTION_LOG_GROUP_ID, COLLECTION_LOG_CATEGORY_ITEMS);

		if (itemsContainer == null)
		{
			return;
		}

		Widget[] items = itemsContainer.getDynamicChildren();
		List<CollectionLogItem> collectionLogItems = new ArrayList<>();
		for (Widget item : items)
		{
			collectionLogItems.add(new CollectionLogItem(item));
		}

		obtainedItems.put(categoryTitle, collectionLogItems);
		saveConfig(obtainedItems, OBTAINED_ITEMS);
	}

	private void getKillCount(String categoryTitle, Widget categoryHead)
	{
		Widget[] children = categoryHead.getDynamicChildren();
		if (children.length < 3) {
			return;
		}

		String killCount = categoryHead.getDynamicChildren()[2].getText();
		killCount = killCount.split(": ")[1]
			.split(">")[1]
			.split("<")[0]
			.replace(",", "");
		killCounts.put(categoryTitle, Integer.parseInt(killCount));
		saveConfig(killCounts, KILL_COUNTS);
	}

	private void getCategory()
	{
		Widget categoryHead = client.getWidget(COLLECTION_LOG_GROUP_ID, COLLECTION_LOG_CATEGORY_HEAD);

		if (categoryHead == null)
		{
			return;
		}

		String categoryTitle = categoryHead.getDynamicChildren()[0].getText();

		getItems(categoryTitle);
		getKillCount(categoryTitle, categoryHead);

		List<CollectionLogItem> categoryItems = obtainedItems.get(categoryTitle);
		int itemCount = categoryItems.stream().filter(c -> c.getQuantity() > 0).toArray().length;
		int prevItemCount = getCategoryItemCount(categoryTitle);

		if (itemCount == prevItemCount)
		{
			update();
			return;
		}

		int prevTotalObtained = getCategoryItemCount("total");
		obtainedCounts.put("total", prevTotalObtained + (itemCount - prevItemCount));
		obtainedCounts.put(categoryTitle, itemCount);
		saveConfig(obtainedCounts, OBTAINED_COUNTS);

		update();
	}

	private void highlightCategories()
	{
		for (CollectionLogList listType : CollectionLogList.values())
		{
			Widget categoryList = client.getWidget(COLLECTION_LOG_GROUP_ID, listType.getListIndex());

			if (categoryList == null)
			{
				continue;
			}

			Widget[] names = categoryList.getDynamicChildren();
			for (Widget name : names)
			{
				if (name.getTextColor() == COLLECTION_LOG_DEFAULT_HIGHLIGHT)
				{
					name.setTextColor(config.highlightColor().getRGB());
				}
			}
		}
	}

	private String buildTitle()
	{
		StringBuilder titleBuilder = new StringBuilder(COLLECTION_LOG_TITLE);
		List<String> titleSections = new ArrayList<>();

		if (config.displayUniqueItems())
		{
			Widget collLogContainer = client.getWidget(COLLECTION_LOG_GROUP_ID, COLLECTION_LOG_CONTAINER);

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
			int totalObtained = getCategoryItemCount("total");
			String totalTitle = String.format("T: %d/%d", totalObtained, config.totalItems());

			if (config.displayAsPercentage())
			{
				totalTitle = String.format("T: %.2f%%", ((double) totalObtained / config.totalItems()) * 100);
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

	private void update()
	{
		setTotalItems();

		String title = buildTitle();
		setCollectionLogTitle(title);

		highlightCategories();
	}

	private void setCollectionLogTitle(String title)
	{
		Widget collLogContainer = client.getWidget(COLLECTION_LOG_GROUP_ID, COLLECTION_LOG_CONTAINER);

		if (collLogContainer == null)
		{
			return;
		}

		Widget collLogTitle = collLogContainer.getDynamicChildren()[1];
		collLogTitle.setText(title);
	}

	private int getCategoryItemCount(String categoryTitle)
	{
		if (obtainedCounts.containsKey(categoryTitle))
		{
			return obtainedCounts.get(categoryTitle);
		}
		return 0;
	}

	private void loadConfig()
	{
		String counts = getConfigJsonString(OBTAINED_COUNTS);
		String items = getConfigJsonString(OBTAINED_ITEMS);
		String kc = getConfigJsonString(KILL_COUNTS);

		obtainedCounts = GSON.fromJson(counts, new TypeToken<Map<String, Integer>>(){}.getType());
		obtainedItems = GSON.fromJson(items, new TypeToken<Map<String, List<CollectionLogItem>>>(){}.getType());
		killCounts = GSON.fromJson(kc, new TypeToken<Map<String, Integer>>(){}.getType());
	}

	private void saveConfig(Object items, String configKey)
	{
		String json = GSON.toJson(items);
		configManager.setRSProfileConfiguration(CONFIG_GROUP, configKey, json);
		configManager.unsetConfiguration(CONFIG_GROUP + "." + client.getUsername(), configKey);
	}

	private void setTotalItems()
	{
		int newTotal = 0;
		for (Map.Entry<String, List<CollectionLogItem>> entry : obtainedItems.entrySet())
		{
			newTotal += entry.getValue().size();
		}

		if (newTotal > config.totalItems())
		{
			configManager.setConfiguration(CONFIG_GROUP, TOTAL_ITEMS, newTotal);
		}
	}

	private String getConfigJsonString(String configKey)
	{
		String jsonString = configManager.getRSProfileConfiguration(CONFIG_GROUP, configKey);
		if (jsonString == null)
		{
			return "{}";
		}
		return jsonString;
	}

	private void updateObtainedItems(ItemComposition item, int quantity)
	{
		for (Map.Entry<String, List<CollectionLogItem>> entry : obtainedItems.entrySet())
		{
			String category = entry.getKey();
			entry.getValue().stream().filter(savedItem -> savedItem.getId() == item.getId()).forEach(savedItem -> {
				int newQuantity = obtainedCounts.get(category) == null ? 1 : obtainedCounts.get(category) + 1;
				obtainedCounts.put(category, newQuantity);
				obtainedCounts.put("total", obtainedCounts.get("total") + 1);
				savedItem.setQuantity(savedItem.getQuantity() + quantity);
			});
		}

		saveConfig(obtainedItems, OBTAINED_ITEMS);
		saveConfig(obtainedCounts, OBTAINED_COUNTS);
	}
}
