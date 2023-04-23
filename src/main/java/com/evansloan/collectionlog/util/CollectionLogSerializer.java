package com.evansloan.collectionlog.util;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogPage;
import com.evansloan.collectionlog.CollectionLogTab;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import java.util.Map;

public class CollectionLogSerializer implements JsonSerializer<CollectionLog>
{
	private static final String COLLECTION_LOG_ITEMS_KEY = "items";
	private static final String COLLECTION_LOG_KILL_COUNTS_KEY = "killCounts";
	private static final String COLLECTION_LOG_TABS_KEY = "tabs";
	private static final String COLLECTION_LOG_TOTAL_OBTAINED_KEY = "totalObtained";
	private static final String COLLECTION_LOG_TOTAL_ITEMS_KEY = "totalItems";
	private static final String COLLECTION_LOG_UNIQUE_OBTAINED_KEY = "uniqueObtained";
	private static final String COLLECTION_LOG_UNIQUE_ITEMS_KEY = "uniqueItems";
	private static final String COLLECTION_LOG_IS_UPDATED_KEY = "isUpdated";

	@Override
	public JsonElement serialize(CollectionLog collectionLog, Type type, JsonSerializationContext context)
	{
		JsonObject collectionLogJson = new JsonObject();
		collectionLogJson.addProperty(COLLECTION_LOG_TOTAL_OBTAINED_KEY, collectionLog.getTotalObtained());
		collectionLogJson.addProperty(COLLECTION_LOG_TOTAL_ITEMS_KEY, collectionLog.getTotalItems());
		collectionLogJson.addProperty(COLLECTION_LOG_UNIQUE_OBTAINED_KEY, collectionLog.getUniqueObtained());
		collectionLogJson.addProperty(COLLECTION_LOG_UNIQUE_ITEMS_KEY, collectionLog.getUniqueItems());

		JsonObject jsonTabs = new JsonObject();

		for (Map.Entry<String, CollectionLogTab> tab : collectionLog.getTabs().entrySet())
		{
			JsonObject jsonTab = new JsonObject();

			for (Map.Entry<String, CollectionLogPage> page : tab.getValue().getPages().entrySet())
			{
				JsonObject jsonPage = new JsonObject();
				JsonElement jsonItems = context.serialize(page.getValue().getItems());
				JsonElement jsonKillCounts = context.serialize(page.getValue().getKillCounts());

				jsonPage.addProperty(COLLECTION_LOG_IS_UPDATED_KEY, page.getValue().isUpdated());
				jsonPage.add(COLLECTION_LOG_ITEMS_KEY, jsonItems);
				jsonPage.add(COLLECTION_LOG_KILL_COUNTS_KEY, jsonKillCounts);

				jsonTab.add(page.getKey(), jsonPage);
			}

			jsonTabs.add(tab.getKey(), jsonTab);
		}

		collectionLogJson.add(COLLECTION_LOG_TABS_KEY, jsonTabs);

		return collectionLogJson;
	}
}
