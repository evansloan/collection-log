package com.evansloan.collectionlog;

import com.evansloan.collectionlog.util.CollectionLogDeserializer;
import com.evansloan.collectionlog.util.CollectionLogSerializer;
import com.evansloan.collectionlog.util.JsonUtils;
import com.evansloan.collectionlog.util.UserSettingsDeserializer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.ItemComposition;
import net.runelite.api.StructComposition;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;

@Slf4j
@Singleton
public class CollectionLogManager
{
	private static final List<Integer> COLLECTION_LOG_TAB_STRUCT_IDS = ImmutableList.of(
		471, // Bosses
		472, // Raids
		473, // Clues
		474, // Minigames
		475  // Other
	);
	private static final List<Integer> COLLECTION_LOG_TAB_ENUM_IDS = ImmutableList.of(2103, 2104, 2105, 2106, 2107);
	private static final int COLLECTION_LOG_TAB_NAME_PARAM_ID = 682;
	private static final int COLLECTION_LOG_TAB_ENUM_PARAM_ID = 683;
	private static final int COLLECTION_LOG_PAGE_NAME_PARAM_ID = 689;
	private static final int COLLECTION_LOG_PAGE_ITEMS_ENUM_PARAM_ID = 690;
	private static final int COLLECTION_LOG_KILL_COUNT_SCRIPT_ID = 2735;

	private static final int COLLECTION_LOG_UNIQUE_OBTAINED_VARP_ID = 2943;
	private static final int COLLECTION_LOG_UNIQUE_ITEMS_VARP_ID = 2944;

	private static final File COLLECTION_LOG_DIR = new File(RUNELITE_DIR, "collectionlog");
	private static final File COLLECTION_LOG_SAVE_DATA_DIR = new File(COLLECTION_LOG_DIR, "data");
	private static final File COLLECTION_LOG_EXPORT_DIR = new File(COLLECTION_LOG_DIR, "exports");
	private static final Pattern COLLECTION_LOG_FILE_PATTERN = Pattern.compile("collectionlog-([\\w\\s-]+).json");

	/*
	 * Map of item IDs that differ in page items struct vs ID on item widget in the collection log
	 * Both IDs are valid, but causes duplicates on site
	 *
	 * Key: Page struct item ID
	 * Value: Item widget item ID
	 */
	private static final Map<Integer, Integer> ITEM_ID_MAP = new ImmutableMap.Builder<Integer, Integer>()
		.put(10859, 25617) // Tea flask
		.put(10877, 25618) // Red satchel
		.put(10878, 25619) // Green satchel
		.put(10879, 25620) // Red satchel
		.put(10880, 25621) // Black satchel
		.put(10881, 25622) // Gold satchel
		.put(10882, 25623) // Rune satchel
		.put(13273, 25624) // Unsired
		.put(12019, 25627) // Coal bag
		.put(12020, 25628) // Gem bag
		.put(24882, 25629) // Plank sack
		.put(12854, 25630) // Flamtaer bag
		.build();

	private final Map<String, CollectionLog> loadedCollectionLogs = new HashMap<>();

	@Setter
	private String username;

	@Getter
	private boolean isInitialized;

	@Getter
	private CollectionLog collectionLog;

	@Getter
	@Setter
	private UserSettings userSettings = new UserSettings();

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private JsonUtils jsonUtils;


	/**
	 * Init CollectionLog object with all items in the collection log. Does not include quantity or obtained status.
	 * Based off cs2 scripts
	 * <a href="https://github.com/Joshua-F/cs2-scripts/blob/master/scripts/%5Bproc,collection_draw_list%5D.cs2">2731 proc_collection_draw_list</a>
	 * and
	 * <a href="https://github.com/Joshua-F/cs2-scripts/blob/master/scripts/%5Bproc,collection_draw_log%5D.cs2">2732 proc_collection_draw_log</a>
	 * If a user has previously clicked through the collection log with the plugin installed,
	 * obtained and quantity will be set for each item if item exists in local save file.
	 */
	public void initCollectionLog()
	{
		CollectionLog saveFileCollectionLog = loadedCollectionLogs.get(username);
		boolean saveDataExists = saveFileCollectionLog != null;

		int totalObtained = 0;
		int totalItems = 0;
		Map<String, CollectionLogTab> collectionLogTabs = new HashMap<>();

		for (Integer structId : COLLECTION_LOG_TAB_STRUCT_IDS)
		{
			StructComposition tabStruct = client.getStructComposition(structId);
			String tabName = tabStruct.getStringValue(COLLECTION_LOG_TAB_NAME_PARAM_ID);
			int tabEnumId = tabStruct.getIntValue(COLLECTION_LOG_TAB_ENUM_PARAM_ID);
			EnumComposition tabEnum = client.getEnum(tabEnumId);

			Map<String, CollectionLogPage> collectionLogPages = new HashMap<>();
			for (Integer pageStructId : tabEnum.getIntVals())
			{
				StructComposition pageStruct = client.getStructComposition(pageStructId);
				String pageName = pageStruct.getStringValue(COLLECTION_LOG_PAGE_NAME_PARAM_ID);
				int pageItemsEnumId = pageStruct.getIntValue(COLLECTION_LOG_PAGE_ITEMS_ENUM_PARAM_ID);
				EnumComposition pageItemsEnum = client.getEnum(pageItemsEnumId);

				List<CollectionLogItem> pageItems = new ArrayList<>();
				List<CollectionLogKillCount> pageKillCounts = new ArrayList<>();

				CollectionLogPage saveFilePage = null;
				if (saveDataExists)
				{
					saveFilePage = saveFileCollectionLog.searchForPage(pageName);
				}

				for (Integer pageItemId : pageItemsEnum.getIntVals())
				{
					ItemComposition itemComposition = itemManager.getItemComposition(pageItemId);
					CollectionLogItem item = CollectionLogItem.fromItemComposition(itemComposition, pageItems.size());

					if (ITEM_ID_MAP.containsKey(item.getId()))
					{
						item.setId(ITEM_ID_MAP.get(item.getId()));
					}

					if (saveDataExists && saveFilePage != null)
					{
						CollectionLogItem saveFileItem = saveFilePage.getItemById(item.getId());
						if (saveFileItem != null)
						{
							item.setQuantity(saveFileItem.getQuantity());
							item.setObtained(saveFileItem.isObtained());
						}
					}

					pageItems.add(item);
					totalItems += 1;
					if (item.isObtained())
					{
						totalObtained += 1;
					}
				}

				// Run script to get kill counts for current page, pushes 3 (possibly empty) strings onto stack
				client.runScript(COLLECTION_LOG_KILL_COUNT_SCRIPT_ID, pageStruct.getId());
				List<String> killCountStrings = new ArrayList<>(
					Arrays.asList(Arrays.copyOfRange(client.getStringStack(), 0, 3))
				);
				Collections.reverse(killCountStrings);

				for (String killCountString : killCountStrings)
				{
					if (killCountString.isEmpty())
					{
						continue;
					}
					CollectionLogKillCount killCount = CollectionLogKillCount.fromString(killCountString, pageKillCounts.size());
					pageKillCounts.add(killCount);
				}

				boolean isUpdated = saveFilePage != null && saveFilePage.isUpdated();
				CollectionLogPage collectionLogPage = new CollectionLogPage(pageName, pageItems, pageKillCounts, isUpdated);

				collectionLogPages.put(pageName, collectionLogPage);
			}

			collectionLogTabs.put(tabName, new CollectionLogTab(tabName, collectionLogPages));
		}

		collectionLog = new CollectionLog(
			username,
			totalObtained,
			totalItems,
			client.getVarpValue(COLLECTION_LOG_UNIQUE_OBTAINED_VARP_ID),
			client.getVarpValue(COLLECTION_LOG_UNIQUE_ITEMS_VARP_ID),
			collectionLogTabs
		);

		isInitialized = true;
	}

	private String getDataFilePath(String fileName)
	{
		File directory = new File(COLLECTION_LOG_SAVE_DATA_DIR + File.separator + username);
		directory.mkdirs();
		return directory + File.separator + fileName;
	}

	public String getCollectionLogFilePath()
	{
		String fileName = "collectionlog-" + username + ".json";
		return getDataFilePath(fileName);
	}

	public String getUserSettingsFilePath()
	{
		String fileName = "settings-" + username + ".json"; // TODO: Username is null here
		return getDataFilePath(fileName);
	}

	public String getExportFilePath()
	{
		File directory = COLLECTION_LOG_EXPORT_DIR;
		String exportDate = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
		String fileName = exportDate + "-collectionlog-" + username + ".json";

		directory.mkdir();
		return directory + File.separator + fileName;
	}

	public void loadCollectionLogFiles(File directory)
	{
		if (!directory.exists())
		{
			return;
		}

		File[] files = directory.listFiles();
		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			Matcher matcher = COLLECTION_LOG_FILE_PATTERN.matcher(file.getName());
			if (!matcher.matches())
			{
				continue;
			}

			String fileUsername = matcher.group(1);
			CollectionLog loadedCollectionLog = jsonUtils.readJsonFile(file.getPath(), CollectionLog.class, new CollectionLogDeserializer());
			loadedCollectionLogs.put(fileUsername, loadedCollectionLog);
		}
	}

	public void loadCollectionLogFiles()
	{
		loadCollectionLogFiles(COLLECTION_LOG_SAVE_DATA_DIR);
	}

	public UserSettings loadUserSettingsFile()
	{
		return jsonUtils.readJsonFile(getUserSettingsFilePath(), UserSettings.class, new UserSettingsDeserializer());
	}

	public boolean saveCollectionLogFile(boolean isExport)
	{
		String filePath = getCollectionLogFilePath();
		if (isExport)
		{
			filePath = getExportFilePath();
		}

		boolean isSaved = jsonUtils.writeJsonFile(filePath, collectionLog, new CollectionLogSerializer());
		if (isSaved)
		{
			loadedCollectionLogs.put(username, collectionLog);
		}

		return isSaved;
	}

	public boolean saveUserSettingsFile()
	{
		return jsonUtils.writeJsonFile(getUserSettingsFilePath(), userSettings);
	}

	/**
	 * Deletes saved collection log data for the current user
	 */
	public void deleteSaveFile()
	{
		String filePath = getCollectionLogFilePath();
		File savedData = new File(filePath);
		if (!savedData.delete())
		{
			log.error("Unable to delete collection log save file: " + filePath);
			return;
		}

		loadedCollectionLogs.remove(username);
		isInitialized = false;
		collectionLog = null;
	}

	public void reset()
	{
		collectionLog = null;
		isInitialized = false;
		username = null;
		userSettings = new UserSettings();
	}

	public void updateUniqueCounts()
	{
		collectionLog.setUniqueObtained(client.getVarpValue(COLLECTION_LOG_UNIQUE_OBTAINED_VARP_ID));
		collectionLog.setUniqueItems(client.getVarpValue(COLLECTION_LOG_UNIQUE_ITEMS_VARP_ID));
	}

	/**
	 * Updates the total amount of items in the collection log
	 */
	public void updateTotalItems()
	{
		int newTotal = 0;
		for (CollectionLogTab tab : collectionLog.getTabs().values())
		{
			Collection<CollectionLogPage> pages = tab.getPages().values();
			newTotal += pages.stream().mapToInt(page -> page.getItems().size()).sum();
		}

		if (newTotal > collectionLog.getTotalItems())
		{
			collectionLog.setTotalItems(newTotal);
		}
	}

	public CollectionLogTab getTabByName(String tabName)
	{
		return collectionLog.getTabs().get(tabName);
	}

	public CollectionLogPage getPageByName(String pageName)
	{
		return collectionLog.searchForPage(pageName);
	}

	public boolean updateObtainedItem(ItemStack itemStack)
	{
		if (!isInitialized)
		{
			return false;
		}

		boolean itemUpdated = false;
		for (CollectionLogTab tab : collectionLog.getTabs().values())
		{
			for (CollectionLogPage page : tab.getPages().values())
			{
				CollectionLogItem existingItem = page.getItemById(itemStack.getId());
				if (existingItem == null)
				{
					continue;
				}

				itemUpdated = true;
				existingItem.setQuantity(existingItem.getQuantity() + itemStack.getQuantity());
				existingItem.setObtained(true);

				collectionLog.setTotalObtained(collectionLog.getTotalObtained() + 1);
			}
		}

		if (itemUpdated)
		{
			collectionLog.setUniqueObtained(collectionLog.getUniqueObtained() + 1);
		}

		return false;
	}

	public JsonObject getCollectionLogJsonObject()
	{
		return jsonUtils.toJsonObject(collectionLog, new CollectionLogSerializer());
	}

	public JsonObject getUserSettingsJsonObject()
	{
		return jsonUtils.toJsonObject(userSettings);
	}
}
