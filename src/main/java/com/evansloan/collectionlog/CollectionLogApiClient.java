package com.evansloan.collectionlog;

import com.evansloan.collectionlog.util.CollectionLogDeserializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
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

	@Inject
	private CollectionLogConfig config;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	public void updateUser(String username, String accountType, String accountHash, boolean isFemale, JsonObject userSettings) throws IOException
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

		postRequest(url, jsonObject);
	}

	public boolean getCollectionLogExists(String accountHash) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme(COLLECTION_LOG_API_SCHEME)
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.addPathSegment("exists")
			.addPathSegment(accountHash)
			.build();

		JsonObject response = getRequest(url);
		if (response == null)
		{
			return false;
		}
		return response.get("exists").getAsBoolean();
	}

	public void createCollectionLog(JsonObject collectionLogData, String accountHash, Callback callback) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme(COLLECTION_LOG_API_SCHEME)
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.build();

		JsonObject newCollectionLog = new JsonObject();
		newCollectionLog.add(COLLECTION_LOG_JSON_KEY, collectionLogData);
		newCollectionLog.addProperty("accountHash", accountHash);

		postRequest(url, newCollectionLog, callback);
	}

	public void updateCollectionLog(JsonObject collectionLogData, String accountHash, Callback callback) throws IOException
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

	public CollectionLog getCollectionLog(String username) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme(COLLECTION_LOG_API_SCHEME)
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.addPathSegment(COLLECTION_LOG_USER_PATH)
			.addEncodedPathSegment(username)
			.build();

		JsonObject responseData = getRequest(url);

		return gson.newBuilder()
			.registerTypeAdapter(CollectionLog.class, new CollectionLogDeserializer())
			.create()
			.fromJson(responseData.get(COLLECTION_LOG_JSON_KEY), CollectionLog.class);
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

	private Request buildGetRequest(HttpUrl url)
	{
		return createRequestBuilder(url)
			.get()
			.build();
	}

	private JsonObject getRequest(HttpUrl url) throws IOException
	{
		Request request = buildGetRequest(url);
		return apiRequest(request);
	}

	private void getRequest(HttpUrl url, Callback callback)
	{
		if (callback == null)
		{
			String errorMessage = "Unable to execute get request: {}";
			callback = requestCallback(errorMessage);
		}

		Request request = buildGetRequest(url);
		apiRequest(request, callback);
	}

	private Request buildPostRequest(HttpUrl url, JsonObject postData)
	{
		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, postData.toString());
		return createRequestBuilder(url)
			.post(body)
			.build();
	}

	private JsonObject postRequest(HttpUrl url, JsonObject postData) throws IOException
	{
		Request request = buildPostRequest(url, postData);
		return apiRequest(request);
	}

	private void postRequest(HttpUrl url, JsonObject postData, Callback callback)
	{
		if (callback == null)
		{
			String errorMessage = "Unable to execute post request: {}";
			callback = requestCallback(errorMessage);
		}

		Request request = buildPostRequest(url, postData);
		apiRequest(request, callback);
	}

	private Request buildPutRequest(HttpUrl url, JsonObject putData)
	{
		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, putData.toString());
		return createRequestBuilder(url)
			.put(body)
			.build();
	}

	private void putRequest(HttpUrl url, JsonObject putData, Callback callback)
	{
		if (callback == null)
		{
			String errorMessage = "Unable to update collection log: {}";
			callback = requestCallback(errorMessage);
		}
		Request request = buildPutRequest(url, putData);
		apiRequest(request, callback);
	}

	private Request buildDeleteRequest(HttpUrl url, JsonObject deleteData)
	{
		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, deleteData.toString());
		return createRequestBuilder(url)
			.delete(body)
			.build();
	}

	private void deleteRequest(HttpUrl url, JsonObject deleteData, Callback callback)
	{
		if (callback == null)
		{
			String errorMessage = "Unable to delete collection log: {}";
			callback = requestCallback(errorMessage);
		}
		Request request = buildDeleteRequest(url, deleteData);
		apiRequest(request, callback);
	}

	private JsonObject apiRequest(Request request) throws IOException
	{
		if (!config.allowApiConnections())
		{
			return null;
		}

		Response response = okHttpClient.newCall(request).execute();
		JsonObject responseJson = processResponse(response);
		response.close();
		return responseJson;
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

	private Callback requestCallback(String errorMessage)
	{
		return new Callback()
		{
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{
				log.warn(errorMessage, e.getMessage());
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response)
			{
				response.close();
			}
		};
	}
}
