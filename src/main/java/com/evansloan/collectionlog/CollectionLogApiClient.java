package com.evansloan.collectionlog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request;
import okhttp3.RequestBody;

@Singleton
public class CollectionLogApiClient
{
    private static final String COLLECTION_LOG_API_BASE = "api.collectionlog.net";
    private static final String COLLECTION_LOG_USER_PATH = "user";
    private static final String COLLECTION_LOG_LOG_PATH = "collectionlog";
    private static final String COLLECTION_LOG_JSON_KEY = "collection_log";

    @Inject
    private CollectionLogConfig config;

    @Inject
    private OkHttpClient okHttpClient;

    public void createUser(String username, String userHash) throws IOException
    {
        HttpUrl url = new HttpUrl.Builder()
            .scheme("https")
            .host(COLLECTION_LOG_API_BASE)
            .addPathSegment(COLLECTION_LOG_USER_PATH)
            .build();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("runelite_id", userHash);

        postRequest(url, jsonObject);
    }

    public JsonObject getCollectionLog(String userHash) throws IOException
    {
        HttpUrl url = new HttpUrl.Builder()
            .scheme("https")
            .host(COLLECTION_LOG_API_BASE)
            .addPathSegment(COLLECTION_LOG_LOG_PATH)
            .addPathSegment("runelite")
            .addPathSegment(userHash)
            .build();

        JsonObject response = getRequest(url);
        if (response == null)
        {
            return new JsonObject();
        }
        return response.get(COLLECTION_LOG_JSON_KEY).getAsJsonObject();
    }

    public void createCollectionLog(JsonObject collectionLogData, String userHash) throws IOException
    {
        HttpUrl url = new HttpUrl.Builder()
            .scheme("https")
            .host(COLLECTION_LOG_API_BASE)
            .addPathSegment(COLLECTION_LOG_LOG_PATH)
            .build();

        JsonObject newCollectionLog = new JsonObject();
        newCollectionLog.add(COLLECTION_LOG_JSON_KEY, collectionLogData);
        newCollectionLog.addProperty("runelite_id", userHash);

        JsonObject response = postRequest(url, newCollectionLog);
        response.get(COLLECTION_LOG_JSON_KEY).getAsJsonObject();
    }

    public void updateCollectionLog(JsonObject collectionLogData, String userHash) throws IOException
    {
        HttpUrl url = new HttpUrl.Builder()
            .scheme("https")
            .host(COLLECTION_LOG_API_BASE)
            .addPathSegment(COLLECTION_LOG_LOG_PATH)
            .addPathSegment(userHash)
            .build();

        JsonObject logData = new JsonObject();
        logData.add(COLLECTION_LOG_JSON_KEY, collectionLogData);

        putRequest(url, logData);
    }

    private JsonObject getRequest(HttpUrl url) throws IOException
    {
        Request request = new Request.Builder()
            .header("User-Agent", "Runelite")
            .url(url)
            .get()
            .build();

        return apiRequest(request);
    }

    private JsonObject postRequest(HttpUrl url, JsonObject postData) throws IOException
    {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, postData.toString());
        Request request = new Request.Builder()
            .header("User-Agent", "Runelite")
            .url(url)
            .post(body)
            .build();

        return apiRequest(request);
    }

    private JsonObject putRequest(HttpUrl url, JsonObject putData) throws IOException
    {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, putData.toString());
        Request request = new Request.Builder()
            .header("User-Agent", "Runelite")
            .url(url)
            .put(body)
            .build();

        return apiRequest(request);
    }

    private JsonObject apiRequest(Request request) throws IOException
    {
        if (!config.uploadCollectionLog())
        {
            return null;
        }

        Response response =  okHttpClient.newCall(request).execute();
        JsonObject responseJson = processResponse(response);
        response.close();
        return responseJson;
    }

    private JsonObject processResponse(Response response) throws IOException
    {
        if (!response.isSuccessful())
        {
            return null;
        }

        ResponseBody resBody = response.body();
        if (resBody == null)
        {
            return null;
        }
        return new JsonParser().parse(resBody.string()).getAsJsonObject();
    }
}
