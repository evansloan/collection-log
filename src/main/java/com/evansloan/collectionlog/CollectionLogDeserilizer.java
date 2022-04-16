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

public class CollectionLogDeserilizer implements JsonDeserializer
{
    @Override
    public CollectionLog deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Gson gson = new Gson();
        JsonObject jsonObject = json.getAsJsonObject();

        jsonObject = jsonObject.get("collection_log").getAsJsonObject().get("tabs").getAsJsonObject();

        Map<String, CollectionLogTab> newTabs = new HashMap<>();
        for (String tabKey : jsonObject.keySet())
        {
            Map<String, CollectionLogPage> newPages = new HashMap<>();
            for (String pageKey : jsonObject.get(tabKey).getAsJsonObject().keySet())
            {
                List<CollectionLogItem> newItems = new ArrayList<>();
                for (JsonElement item : jsonObject.get(tabKey).getAsJsonObject().get(pageKey).getAsJsonObject().get("items").getAsJsonArray())
                {
                    CollectionLogItem newItem = gson.fromJson(item, CollectionLogItem.class);
                    newItems.add(newItem);
                }
                CollectionLogPage newPage = new CollectionLogPage(pageKey, newItems);
                newPages.put(pageKey, newPage);
            }
            CollectionLogTab newTab = new CollectionLogTab(tabKey, newPages);
            newTabs.put(tabKey, newTab);
        }
        return new CollectionLog(newTabs);
    }
}
