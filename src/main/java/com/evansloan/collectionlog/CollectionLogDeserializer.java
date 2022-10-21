package com.evansloan.collectionlog;

import com.google.gson.Gson;
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
    @Override
    public CollectionLog deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Gson gson = new Gson();
        JsonObject jsonObjectLog = json.getAsJsonObject().get("collectionLog").getAsJsonObject();
        JsonObject jsonObjectTabs  = jsonObjectLog.get("tabs").getAsJsonObject();

        Map<String, CollectionLogTab> newTabs = new HashMap<>();
        for (String tabKey : jsonObjectTabs.keySet())
        {
            JsonObject tab = jsonObjectTabs.get(tabKey).getAsJsonObject();
            Map<String, CollectionLogPage> newPages = new HashMap<>();

            for (String pageKey : tab.keySet())
            {
                JsonObject page = tab.get(pageKey).getAsJsonObject();
                List<CollectionLogItem> newItems = new ArrayList<>();

                for (JsonElement item : page.get("items").getAsJsonArray())
                {
                    CollectionLogItem newItem = gson.fromJson(item, CollectionLogItem.class);
                    newItems.add(newItem);
                }

                List<CollectionLogKillCount> newKillCounts = new ArrayList<>();
                JsonElement pageKillCounts = page.get("killCount");

                if (pageKillCounts != null)
                {
                    for (JsonElement killCount : pageKillCounts.getAsJsonArray())
                    {
                        CollectionLogKillCount newKillCount = gson.fromJson(killCount, CollectionLogKillCount.class);
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
            jsonObjectLog.get("username").getAsString(),
            jsonObjectLog.get("totalObtained").getAsInt(),
            jsonObjectLog.get("totalItems").getAsInt(),
            jsonObjectLog.get("uniqueObtained").getAsInt(),
            jsonObjectLog.get("uniqueItems").getAsInt(),
            newTabs
        );
    }
}
