package com.evansloan.collectionlog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request;
import okhttp3.RequestBody;

@Slf4j
@Singleton
public class CollectionLogApiClient
{
	private static final String COLLECTION_LOG_API_BASE = "api.collectionlog.net";
	private static final String COLLECTION_LOG_USER_PATH = "user";
	private static final String COLLECTION_LOG_LOG_PATH = "collectionlog";
	private static final String COLLECTION_LOG_JSON_KEY = "collection_log";
	private static final String COLLECTION_LOG_USER_AGENT = "Runelite collection-log/2.2";

	private static final String COLLECTION_LOG_TEMPLATE_BASE = "api.github.com";
	private static final String COLLECTION_LOG_TEMPLATE_USER = "gists";
	private static final String COLLECTION_LOG_TEMPLATE_GIST = "24179c0fbfb370ce162f69dde36d72f0";

	@Inject
	private CollectionLogConfig config;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	public void createUser(String username, String accountType, String accountHash, boolean isFemale) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_USER_PATH)
			.build();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("username", username);
		jsonObject.addProperty("account_type", accountType);
		jsonObject.addProperty("account_hash", accountHash);
		jsonObject.addProperty("is_female", isFemale);

		postRequest(url, jsonObject);
	}

	public boolean getCollectionLogExists(String accountHash) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
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

	public void createCollectionLog(JsonObject collectionLogData, String accountHash) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.build();

		JsonObject newCollectionLog = new JsonObject();
		newCollectionLog.add(COLLECTION_LOG_JSON_KEY, collectionLogData);
		newCollectionLog.addProperty("runelite_id", accountHash);

		postRequest(url, newCollectionLog);
	}

	public void updateCollectionLog(JsonObject collectionLogData, String accountHash) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.addPathSegment(accountHash)
			.build();

		JsonObject logData = new JsonObject();
		logData.add(COLLECTION_LOG_JSON_KEY, collectionLogData);

		putRequest(url, logData);
	}

	public JsonObject getCollectionLogTemplate() throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
			.host(COLLECTION_LOG_TEMPLATE_BASE)
			.addPathSegment(COLLECTION_LOG_TEMPLATE_USER)
			.addPathSegment(COLLECTION_LOG_TEMPLATE_GIST)
			.build();

		JsonObject githubRes = githubRequest(url);
		String content = githubRes.getAsJsonObject("files")
			.getAsJsonObject("collection_log_template.json")
			.get("content")
			.getAsString();

		return new JsonParser().parse(content).getAsJsonObject();
	}

	public CollectionLog getCollectionLog(String username) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
			.host(COLLECTION_LOG_API_BASE)
			.addPathSegment(COLLECTION_LOG_LOG_PATH)
			.addPathSegment(COLLECTION_LOG_USER_PATH)
			.addEncodedPathSegment(username)
			.build();

		return gson.newBuilder()
			.registerTypeAdapter(CollectionLog.class, new CollectionLogDeserializer(false))
			.create()
			.fromJson(getRequest(url), CollectionLog.class);
	}

	private JsonObject getRequest(HttpUrl url) throws IOException
	{
		Request request = new Request.Builder()
			.header("User-Agent", COLLECTION_LOG_USER_AGENT)
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
			.header("User-Agent", COLLECTION_LOG_USER_AGENT)
			.url(url)
			.post(body)
			.build();

		return apiRequest(request);
	}

	private void putRequest(HttpUrl url, JsonObject putData) throws IOException
	{
		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, putData.toString());
		Request request = new Request.Builder()
			.header("User-Agent", COLLECTION_LOG_USER_AGENT)
			.url(url)
			.put(body)
			.build();

		String errorMessage = "Unable to update collection log: {}";
		asyncApiRequest(request, requestCallback(errorMessage));
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

	private void asyncApiRequest(Request request, Callback callback) {
		if (!config.allowApiConnections())
		{
			return;
		}

		okHttpClient.newCall(request).enqueue(callback);
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

	private Callback requestCallback(String errorMessage)
	{
		return new Callback() {
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn(errorMessage, e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				response.close();
			}
		};
	}

	private JsonObject githubRequest(HttpUrl url) throws IOException
	{
		Request request = new Request.Builder()
			.header("User-Agent", COLLECTION_LOG_USER_AGENT)
			.url(url)
			.get()
			.build();

		Response response =  okHttpClient.newCall(request).execute();
		JsonObject responseJson = processResponse(response);
		response.close();
		return responseJson;
	}
}
