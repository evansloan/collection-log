package com.evansloan.collectionlog.util;

import com.evansloan.collectionlog.UserSettings;
import com.evansloan.collectionlog.AccountType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class UserSettingsDeserializer implements JsonDeserializer<UserSettings>
{
	private static final String USER_SETTINGS_DISPLAY_RANK = "displayRank";
	private static final String USER_SETTINGS_SHOW_QUANTITY = "showQuantity";

	@Override
	public UserSettings deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject userSettingsJson = jsonElement.getAsJsonObject();
		AccountType displayRank = AccountType.valueOf(userSettingsJson.get(USER_SETTINGS_DISPLAY_RANK).getAsString());
		boolean showQuantity = userSettingsJson.get(USER_SETTINGS_SHOW_QUANTITY).getAsBoolean();

		return new UserSettings(displayRank, showQuantity);
	}
}
