package com.evansloan.collectionlog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
@Singleton
public class CollectionLogApiClient
{
	private static final String COLLECTION_LOG_API_BASE = "api.collectionlog.net";
	private static final String COLLECTION_LOG_API_SCHEME = "https";
	private static final String COLLECTION_LOG_USER_PATH = "user";
	private static final String COLLECTION_LOG_LOG_PATH = "collectionlog";
	private static final String COLLECTION_LOG_JSON_KEY = "collectionLog";
	private static final String COLLECTION_LOG_USER_AGENT = "Runelite collection-log/" + CollectionLogConfig.PLUGIN_VERSION;
	private static final MediaType COLLECTION_LOG_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");;

	@Inject
	private CollectionLogConfig config;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	public void updateUser(String username, String accountType, String accountHash, boolean isFemale, JsonObject userSettings, Callback callback)
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme(COLLECTION_LOG_API_SCHEME)
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_USER_PATH)
			.build();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("username", username);
		jsonObject.addProperty("accountType", accountType);
		jsonObject.addProperty("accountHash", accountHash);
		jsonObject.addProperty("isFemale", isFemale);
		jsonObject.add("userSettings", userSettings);

		postRequest(url, jsonObject, callback);
	}

	public void updateCollectionLog(JsonObject collectionLogData, String accountHash, Callback callback)
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme(COLLECTION_LOG_API_SCHEME)
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.addPathSegment(accountHash)
			.build();

		JsonObject logData = new JsonObject();
		logData.add(COLLECTION_LOG_JSON_KEY, collectionLogData);

		putRequest(url, logData, callback);
	}

	public void getCollectionLog(String username, Callback callback) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme(COLLECTION_LOG_API_SCHEME)
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.addPathSegment(COLLECTION_LOG_USER_PATH)
			.addEncodedPathSegment(username)
			.build();

		getRequest(url, callback);
	}

	public void deleteCollectionLog(String username, String accountHash, Callback callback)
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme(COLLECTION_LOG_API_SCHEME)
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.addPathSegment("delete")
			.build();

		JsonObject deleteBody = new JsonObject();
		deleteBody.addProperty("username", username);
		deleteBody.addProperty("accountHash", accountHash);

		deleteRequest(url, deleteBody, callback);
	}

	private Request.Builder createRequestBuilder(HttpUrl url)
	{
		return new Request.Builder()
			.header("User-Agent", COLLECTION_LOG_USER_AGENT)
			.url(url);
	}

	private void getRequest(HttpUrl url, Callback callback)
	{
		Request request = createRequestBuilder(url)
			.get()
			.build();
		apiRequest(request, callback);
	}

	private void postRequest(HttpUrl url, JsonObject postData, Callback callback)
	{
		RequestBody body = RequestBody.create(COLLECTION_LOG_MEDIA_TYPE, postData.toString());
		Request request = createRequestBuilder(url)
			.post(body)
			.build();
		apiRequest(request, callback);
	}

	private void putRequest(HttpUrl url, JsonObject putData, Callback callback)
	{
		RequestBody body = RequestBody.create(COLLECTION_LOG_MEDIA_TYPE, putData.toString());
		Request request = createRequestBuilder(url)
			.put(body)
			.build();
		apiRequest(request, callback);
	}

	private void deleteRequest(HttpUrl url, JsonObject deleteData, Callback callback)
	{
		RequestBody body = RequestBody.create(COLLECTION_LOG_MEDIA_TYPE, deleteData.toString());
		Request request = createRequestBuilder(url)
			.delete(body)
			.build();
		apiRequest(request, callback);
	}

	private void apiRequest(Request request, Callback callback)
	{
		if (!config.allowApiConnections())
		{
			return;
		}

		okHttpClient.newCall(request).enqueue(callback);
	}

	public JsonObject processResponse(Response response) throws IOException
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
