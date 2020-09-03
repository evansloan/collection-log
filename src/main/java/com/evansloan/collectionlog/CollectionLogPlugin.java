package com.evansloan.collectionlog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Provides;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
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
	private static final String TOTAL_ITEMS = "total_items";

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

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

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
		if (event.getGroup().equals(CONFIG_GROUP))
		{
			setCollectionLogTitle();
		}
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			loadItems();
			setCollectionLogTitle();
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
			loadItems();
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
		client.setMenuEntries(ArrayUtils.insert(event.getMenuEntries().length, client.getMenuEntries(), menuEntry));
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
				writer.name(entry.getKey());
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
			}
			writer.endObject();

			if (config.notifyOnExport())
			{
				notifier.notify("Collection log exported to " + filePath);
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
		saveItems(obtainedItems, OBTAINED_ITEMS);

		int itemCount = Arrays.stream(obtainedItems.get(categoryTitle)).filter(CollectionLogItem::isObtained).toArray().length;
		int prevItemCount = getCategoryItemCount(categoryTitle);

		if (itemCount == prevItemCount)
		{
			setCollectionLogTitle();
			return;
		}

		int prevTotalObtained = getCategoryItemCount("total");
		obtainedCounts.put("total", prevTotalObtained + (itemCount - prevItemCount));
		obtainedCounts.put(categoryTitle, itemCount);
		saveItems(obtainedCounts, OBTAINED_COUNTS);

		setCollectionLogTitle();
	}

	private String buildTitle()
	{
		setTotalItems();
		int totalObtained = getCategoryItemCount("total");
		int totalItems = Integer.parseInt(configManager.getConfiguration(CONFIG_GROUP, TOTAL_ITEMS));
		String title = String.format("%s - %d/%d", COLLECTION_LOG_TITLE, totalObtained, totalItems);

		if (config.displayAsPercentage())
		{
			title = String.format("%s - %.2f%%", COLLECTION_LOG_TITLE, ((double) totalObtained / totalItems) * 100);
		}

		return title;
	}

	private void setCollectionLogTitle()
	{
		String title = buildTitle();
		setCollectionLogTitle(title);
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

	private void loadItems()
	{
		String counts = configManager.getConfiguration(group, OBTAINED_COUNTS);
		String items = configManager.getConfiguration(group, OBTAINED_ITEMS);

		if (counts == null)
		{
			counts = "{}";
		}

		if (items == null)
		{
			items = "{}";
		}

		obtainedCounts = GSON.fromJson(counts, new TypeToken<Map<String, Integer>>()
		{
		}.getType());
		obtainedItems = GSON.fromJson(items, new TypeToken<Map<String, CollectionLogItem[]>>()
		{
		}.getType());
	}

	private void saveItems(Map<String, ?> items, String configKey)
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

		int total = 0;
		if (configManager.getConfigurationKeys(CONFIG_GROUP).contains(TOTAL_ITEMS))
		{
			total = Integer.parseInt(configManager.getConfiguration(CONFIG_GROUP, TOTAL_ITEMS));
		}

		if (newTotal > total)
		{
			configManager.setConfiguration(CONFIG_GROUP, TOTAL_ITEMS, newTotal);
		}
	}
}
