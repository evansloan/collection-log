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

	@ConfigItem(
		keyName = "notify_on_export",
		name = "Notify on export",
		description = "Send a notification on collection log export"
	)
	default boolean notifyOnExport()
	{
		return true;
	}

	@ConfigItem(
		keyName = "total_items",
		name = "Total items",
		description = "Total number of items within the collection log",
		hidden = true
	)
	default int totalItems()
	{
		return 0;
	}
}
