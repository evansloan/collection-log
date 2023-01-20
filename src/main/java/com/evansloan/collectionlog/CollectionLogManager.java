package com.evansloan.collectionlog;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.ItemComposition;
import net.runelite.api.StructComposition;
import net.runelite.client.game.ItemManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
@Singleton
public class CollectionLogManager
{
	private static final List<Integer> COLLECTION_LOG_TAB_STRUCT_IDS = ImmutableList.of(471, 472, 473, 474, 475);
	private static final List<Integer> COLLECTION_LOG_TAB_ENUM_IDS = ImmutableList.of(2103, 2104, 2105, 2106, 2107);
	private static final int COLLECTION_LOG_TAB_NAME_STRUCT_PARAM_ID = 682;
	private static final int COLLECTION_LOG_TAB_ENUM_PARAM_ID = 683;
	private static final int COLLECTION_LOG_PAGE_NAME_PARAM_ID = 689;
	private static final int COLLECTION_LOG_PAGE_ITEMS_ENUM_PARAM_ID = 690;

	private static final int COLLECTION_LOG_UNIQUE_OBTAINED_VARP_ID = 2943;
	private static final int COLLECTION_LOG_UNIQUE_ITEMS_VARP_ID = 2944;

	private static final File COLLECTION_LOG_SAVE_DATA_DIR = new File(RUNELITE_DIR, "collectionlog");
	private static final File COLLECTION_LOG_EXPORT_DIR = new File(COLLECTION_LOG_SAVE_DATA_DIR, "exports");

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Gson gson;

	@Getter
	private CollectionLog collectionLog;

	/**
	 * Init CollectionLog object with all items in the collection log.
	 * Does not include obtained/quantity information.
	 */
	public void initCollectionLog()
	{
		boolean saveDataExists = true;
		CollectionLog saveFileCollectionLog = loadFromSaveFile();
		if (saveFileCollectionLog == null)
		{
			saveDataExists = false;
		}

		int totalObtained = 0;
		int totalItems = 0;
		Map<String, CollectionLogTab> collectionLogTabs = new HashMap<>();
		for (Integer structId : COLLECTION_LOG_TAB_STRUCT_IDS)
		{
			StructComposition tabStruct = client.getStructComposition(structId);
			String tabName = tabStruct.getStringValue(COLLECTION_LOG_TAB_NAME_STRUCT_PARAM_ID);
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

					if (saveDataExists && saveFilePage != null)
					{
						CollectionLogItem saveFileItem = saveFilePage.getItemById(itemComposition.getId());
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

				CollectionLogPage collectionLogPage = new CollectionLogPage(pageName, pageItems, pageKillCounts);
				if (saveFilePage != null)
				{
					collectionLogPage.setUpdated(true);
				}
				collectionLogPages.put(pageName, collectionLogPage);
			}

			collectionLogTabs.put(tabName, new CollectionLogTab(tabName, collectionLogPages));
		}

		collectionLog = new CollectionLog(
			client.getLocalPlayer().getName(),
			totalObtained,
			totalItems,
			client.getVarpValue(COLLECTION_LOG_UNIQUE_OBTAINED_VARP_ID),
			client.getVarpValue(COLLECTION_LOG_UNIQUE_ITEMS_VARP_ID),
			collectionLogTabs
		);
	}

	/**
	 * TODO: Move file loading logic into init
	 * Pull items based off page name
	 */
	private CollectionLog loadFromSaveFile()
	{
		try
		{
			String fileName = "collectionlog-" + client.getLocalPlayer().getName() + ".json";
			FileReader reader = new FileReader(COLLECTION_LOG_SAVE_DATA_DIR + File.separator + fileName);
			JsonObject collectionLogSaveData = new JsonParser().parse(reader).getAsJsonObject();
			reader.close();

			return gson.newBuilder()
				.registerTypeAdapter(CollectionLog.class, new CollectionLogDeserializer(true))
				.create()
				.fromJson(collectionLogSaveData, CollectionLog.class);
		}
		catch (IOException | JsonParseException e)
		{
			log.info("Unable to load collection log data from save file: " + e.getMessage());
			return null;
		}
	}

	public void clearCollectionLog()
	{
		collectionLog = null;
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
}
