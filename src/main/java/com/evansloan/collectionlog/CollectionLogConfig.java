package com.evansloan.collectionlog;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("collectionlog")
public interface CollectionLogConfig extends Config
{
	@ConfigItem(
		keyName = "display_as_percentage",
		name = "Display as percentage",
		description = "Display collection log progress as a percentage"
	)
	default boolean displayAsPercentage()
	{
		return false;
	}
}
