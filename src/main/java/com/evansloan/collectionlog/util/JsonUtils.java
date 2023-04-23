package com.evansloan.collectionlog.util;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class JsonUtils
{
	@Inject
	Gson gson;

	private JsonObject parseFile(String filePath) throws IOException, JsonParseException
	{
		FileReader reader = new FileReader(filePath);
		JsonObject fileContents = new JsonParser().parse(reader).getAsJsonObject();
		reader.close();

		return fileContents;
	}

	public <T, D extends JsonDeserializer<T>> T readJsonFile(String filePath, Class<T> type, D deserializer)
	{
		try
		{
			JsonObject fileContents = parseFile(filePath);
			return gson.newBuilder()
				.registerTypeAdapter(type, deserializer)
				.create()
				.fromJson(fileContents, type);
		}
		catch (IOException | JsonParseException e)
		{
			log.error("Unable to read JSON file at path: " + filePath + "\n" + e.getMessage());
		}

		return null;
	}

	private boolean writeFile(String filePath, String contents)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(contents);
			writer.close();
			return true;
		}
		catch (IOException e)
		{
			log.error("Unable to write JSON file at path: " + filePath + "\n" + e.getMessage());
		}

		return false;
	}

	public <T> boolean writeJsonFile(String filePath, T data)
	{
		String contents = gson.newBuilder()
			.setPrettyPrinting()
			.create()
			.toJson(data, data.getClass());

		return writeFile(filePath, contents);
	}

	public <T, S extends JsonSerializer<T>> boolean writeJsonFile(String filePath, T data, S serializer)
	{
		String contents = gson.newBuilder()
			.registerTypeAdapter(data.getClass(), serializer)
			.setPrettyPrinting()
			.create()
			.toJson(data);

		return writeFile(filePath, contents);
	}

	public <T> JsonObject toJsonObject(T data)
	{
		return gson.toJsonTree(data).getAsJsonObject();
	}

	public <T, S extends JsonSerializer<T>> JsonObject toJsonObject(T data, S serializer)
	{
		return gson.newBuilder()
			.registerTypeAdapter(data.getClass(), serializer)
			.create()
			.toJsonTree(data)
			.getAsJsonObject();
	}

	public <T, D extends JsonDeserializer<T>> T fromJsonObject(JsonObject data, Class<T> type, D deserializer)
	{
		return gson.newBuilder()
			.registerTypeAdapter(type, deserializer)
			.create()
			.fromJson(data, type);
	}
}
