package com.evansloan.collectionlog;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionLogDeserializer implements JsonDeserializer<CollectionLog>
{
    private final String COLLECTION_LOG_KEY = "collectionLog";
    private final String COLLECTION_LOG_ITEMS_KEY = "items";
    private final String COLLECTION_LOG_KILL_COUNTS_KEY = "killCounts";
    private final String COLLECTION_LOG_TABS_KEY = "tabs";
    private final String COLLECTION_LOG_TOTAL_OBTAINED_KEY = "totalObtained";
    private final String COLLECTION_LOG_TOTAL_ITEMS_KEY = "totalItems";
    private final String COLLECTION_LOG_UNIQUE_OBTAINED_KEY = "uniqueObtained";
    private final String COLLECTION_LOG_UNIQUE_ITEMS_KEY = "uniqueItems";
    private final String COLLECTION_LOG_USERNAME_KEY = "username";

    private final boolean isFileDeserialize;

    private final Map<String, String> keyMap = new HashMap<String, String>() {
        {
            put(COLLECTION_LOG_KEY, COLLECTION_LOG_KEY);
            put(COLLECTION_LOG_KILL_COUNTS_KEY, COLLECTION_LOG_KILL_COUNTS_KEY);
            put(COLLECTION_LOG_TOTAL_OBTAINED_KEY, COLLECTION_LOG_TOTAL_OBTAINED_KEY);
            put(COLLECTION_LOG_TOTAL_ITEMS_KEY, COLLECTION_LOG_TOTAL_ITEMS_KEY);
            put(COLLECTION_LOG_UNIQUE_OBTAINED_KEY, COLLECTION_LOG_UNIQUE_OBTAINED_KEY);
            put(COLLECTION_LOG_UNIQUE_ITEMS_KEY, COLLECTION_LOG_UNIQUE_ITEMS_KEY);
        }
    };

    CollectionLogDeserializer(boolean isFileDeserialize)
    {
        this.isFileDeserialize = isFileDeserialize;
        if (isFileDeserialize)
        {
            keyMap.put(COLLECTION_LOG_KEY, "collection_log");
            keyMap.put(COLLECTION_LOG_KILL_COUNTS_KEY, "kill_count");
            keyMap.put(COLLECTION_LOG_TOTAL_OBTAINED_KEY, "total_obtained");
            keyMap.put(COLLECTION_LOG_TOTAL_ITEMS_KEY, "total_items");
            keyMap.put(COLLECTION_LOG_UNIQUE_OBTAINED_KEY, "unique_obtained");
            keyMap.put(COLLECTION_LOG_UNIQUE_ITEMS_KEY, "unique_items");
        }
    }

    @Override
    public CollectionLog deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObjectLog;
        if (isFileDeserialize)
        {
            jsonObjectLog = json.getAsJsonObject();
        }
        else
        {
            jsonObjectLog = json.getAsJsonObject().get(keyMap.get(COLLECTION_LOG_KEY)).getAsJsonObject();
        }

        JsonObject jsonObjectTabs = jsonObjectLog.get(COLLECTION_LOG_TABS_KEY).getAsJsonObject();

        Map<String, CollectionLogTab> newTabs = new HashMap<>();
        for (String tabKey : jsonObjectTabs.keySet())
        {
            JsonObject tab = jsonObjectTabs.get(tabKey).getAsJsonObject();
            Map<String, CollectionLogPage> newPages = new HashMap<>();

            for (String pageKey : tab.keySet())
            {
                JsonObject page = tab.get(pageKey).getAsJsonObject();
                List<CollectionLogItem> newItems = new ArrayList<>();

                for (JsonElement item : page.get(COLLECTION_LOG_ITEMS_KEY).getAsJsonArray())
                {
                    CollectionLogItem newItem = context.deserialize(item, CollectionLogItem.class);
                    newItems.add(newItem);
                }

                List<CollectionLogKillCount> newKillCounts = new ArrayList<>();
                JsonElement pageKillCounts = page.get(keyMap.get(COLLECTION_LOG_KILL_COUNTS_KEY));

                if (pageKillCounts != null)
                {
                    for (JsonElement killCount : pageKillCounts.getAsJsonArray())
                    {
                        CollectionLogKillCount newKillCount;
                        if (isFileDeserialize)
                        {
                            String killCountString = killCount.getAsString();
                            String killCountName = killCountString.split(": ")[0];
                            int killCountAmount = Integer.parseInt(killCountString.split(": ")[1]);
                            newKillCount = new CollectionLogKillCount(killCountName, killCountAmount);
                        }
                        else
                        {
                            newKillCount = context.deserialize(killCount, CollectionLogKillCount.class);
                        }
                        newKillCounts.add(newKillCount);
                    }
                }
                CollectionLogPage newPage = new CollectionLogPage(pageKey, newItems, newKillCounts);
                newPages.put(pageKey, newPage);
            }
            CollectionLogTab newTab = new CollectionLogTab(tabKey, newPages);
            newTabs.put(tabKey, newTab);
        }
        return new CollectionLog(
            "",
            jsonObjectLog.get(keyMap.get(COLLECTION_LOG_TOTAL_OBTAINED_KEY)).getAsInt(),
            jsonObjectLog.get(keyMap.get(COLLECTION_LOG_TOTAL_ITEMS_KEY)).getAsInt(),
            jsonObjectLog.get(keyMap.get(COLLECTION_LOG_UNIQUE_OBTAINED_KEY)).getAsInt(),
            jsonObjectLog.get(keyMap.get(COLLECTION_LOG_UNIQUE_ITEMS_KEY)).getAsInt(),
            newTabs
        );
    }
}
