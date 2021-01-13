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

	@ConfigItem(
		keyName = "export_chat_message",
		name = "Chat message on export",
		description = "Show exported file location in chat box on collection log export",
		position = 5
	)
	default boolean sendExportChatMessage()
	{
		return true;
	}

	@ConfigItem(
		keyName = "obtained_counts",
		name = "Obtained counts",
		description = "How many items have been obtained in each category",
		hidden = true
	)
	default String obtainedCounts()
	{
		return "{}";
	}

	@ConfigItem(
		keyName = "obtained_items",
		name = "Obtained items",
		description = "All items that have been marked as obtained in the collection log",
		hidden = true
	)
	default String obtainedItems()
	{
		return "{}";
	}

	@ConfigItem(
		keyName = "completed_categories",
		name = "Completed categories",
		description = "Categories in which all items have been obtained",
		hidden = true
	)
	default String completedCategories()
	{
		return "[]";
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


	@ConfigItem(
		keyName = "kill_counts",
		name = "Kill counts",
		description = "Kill counts for each category",
		hidden = true
	)
	default String killCounts()
	{
		return "{}";
	}
}
