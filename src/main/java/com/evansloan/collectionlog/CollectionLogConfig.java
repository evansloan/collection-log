package com.evansloan.collectionlog;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("collectionlog")
public interface CollectionLogConfig extends Config
{
	@ConfigItem(
		keyName = "highlight_completed",
		name = "Highlight completed categories",
		description = "Change the color of completed category names",
		position = 1
	)
	default boolean highlightCompleted()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlight_color",
		name = "Completed category highlight color",
		description = "Sets the highlight color of completed categories",
		position = 2
	)
	default Color highlightColor()
	{
		return Color.GREEN;
	}
	@ConfigItem(
		keyName = "display_as_percentage",
		name = "Display as percentage",
		description = "Display collection log progress as a percentage",
		position = 3
	)
	default boolean displayAsPercentage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "notify_on_export",
		name = "Notify on export",
		description = "Send a notification on collection log export",
		position = 4
	)
	default boolean notifyOnExport()
	{
		return true;
	}
}
