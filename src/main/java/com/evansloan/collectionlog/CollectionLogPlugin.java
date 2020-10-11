package com.evansloan.collectionlog;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
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
	private static final String COMPLETED_CATEGORIES = "completed_categories";
	private static final String TOTAL_ITEMS = "total_items";
	private static final String KILL_COUNTS = "kill_counts";

	private static final int COLLECTION_LOG_GROUP_ID = 621;
	private static final int COLLECTION_LOG_CONTAINER = 1;
	private static final int COLLECTION_LOG_CATEGORY_HEAD = 19;
	private static final int COLLECTION_LOG_CATEGORY_ITEMS = 35;
	private static final int COLLECTION_LOG_CATEGORY_VARBIT_INDEX = 2049;
	private static final String COLLECTION_LOG_TITLE = "Collection Log";
	private static final String COLLECTION_LOG_TARGET = "Collection log";
	private static final String COLLECTION_LOG_EXPORT = "Export";
	private static final File COLLECTION_LOG_EXPORT_DIR = new File(RUNELITE_DIR, "collectionlog");

	private String group;
	private final Gson GSON = new Gson();

	private Map<String, Integer> obtainedCounts = new HashMap<>();
	private Map<String, CollectionLogItem[]> obtainedItems = new HashMap<>();
	private List<String> completedCategories = new ArrayList<>();
	private Map<String, Integer> killCounts = new HashMap<>();

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

	@Provides
	CollectionLogConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CollectionLogConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		String configKey = event.getKey();
		String username = client.getUsername();

		if (configKey.equals(username + "." + OBTAINED_COUNTS) ||
			 configKey.equals(username + "." + OBTAINED_ITEMS) ||
			 configKey.equals(username + "." + COMPLETED_CATEGORIES) ||
			 !event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		update();
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
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
			group = CONFIG_GROUP + "." + client.getUsername();
			loadConfig();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == COLLECTION_LOG_GROUP_ID)
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

	private void exportItems()
	{
		COLLECTION_LOG_EXPORT_DIR.mkdir();

		String fileName = new SimpleDateFormat("'collectionlog-'yyyyMMdd'T'HHmmss'.json'").format(new Date());
		String filePath = COLLECTION_LOG_EXPORT_DIR + File.separator  + fileName;

		try (JsonWriter writer = new JsonWriter(new FileWriter(filePath)))
		{
			writer.setIndent("  ");
			writer.beginObject();
			for (Map.Entry<String, CollectionLogItem[]> entry : obtainedItems.entrySet())
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
					writer.name("name").value(item.getName());
					writer.name("obtained").value(item.isObtained());
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
		CollectionLogItem[] collectionLogItems = new CollectionLogItem[items.length];
		for (Widget item : items)
		{
			collectionLogItems[item.getIndex()] = new CollectionLogItem(item);
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

		CollectionLogItem[] categoryItems = obtainedItems.get(categoryTitle);
		int itemCount = Arrays.stream(categoryItems).filter(CollectionLogItem::isObtained).toArray().length;
		int prevItemCount = getCategoryItemCount(categoryTitle);
		int totalItemCount = categoryItems.length;

		if (itemCount == totalItemCount && !completedCategories.contains(categoryTitle))
		{
			completedCategories.add(categoryTitle);
			saveConfig(completedCategories, COMPLETED_CATEGORIES);
		}
		else if (itemCount < totalItemCount && completedCategories.contains(categoryTitle))
		{
			completedCategories.remove(categoryTitle);
		}

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
				if (completedCategories.contains(name.getText()))
				{
					name.setTextColor(config.highlightColor().getRGB() & 0x00FFFFFF);
				}
			}
		}
	}

	private String buildTitle()
	{
		setTotalItems();
		int totalObtained = getCategoryItemCount("total");
		String title = String.format("%s - %d/%d", COLLECTION_LOG_TITLE, totalObtained, config.totalItems());

		if (config.displayAsPercentage())
		{
			title = String.format("%s - %.2f%%", COLLECTION_LOG_TITLE, ((double) totalObtained / config.totalItems()) * 100);
		}

		return title;
	}

	private void update()
	{
		String title = buildTitle();
		setCollectionLogTitle(title);

		if (config.highlightCompleted())
		{
			highlightCategories();
		}
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
		String completed = getConfigJsonString(COMPLETED_CATEGORIES);
		String kc = getConfigJsonString(KILL_COUNTS);

		obtainedCounts = GSON.fromJson(counts, new TypeToken<Map<String, Integer>>(){}.getType());
		obtainedItems = GSON.fromJson(items, new TypeToken<Map<String, CollectionLogItem[]>>(){}.getType());
		completedCategories = GSON.fromJson(completed, new TypeToken<List<String>>(){}.getType());
		killCounts = GSON.fromJson(kc, new TypeToken<Map<String, Integer>>(){}.getType());
	}

	private void saveConfig(Object items, String configKey)
	{
		String json = GSON.toJson(items);
		configManager.setConfiguration(group, configKey, json);
	}

	private void setTotalItems()
	{
		int newTotal = 0;
		for (Map.Entry<String, CollectionLogItem[]> entry : obtainedItems.entrySet())
		{
			newTotal += entry.getValue().length;
		}

		if (newTotal > config.totalItems())
		{
			configManager.setConfiguration(CONFIG_GROUP, TOTAL_ITEMS, newTotal);
		}
	}

	private String getConfigJsonString(String configKey)
	{
		String jsonString = configManager.getConfiguration(group, configKey);
		if (jsonString == null)
		{
			return "{}";
		}
		return jsonString;
	}
}
