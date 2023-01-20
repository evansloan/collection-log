package com.evansloan.collectionlog;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("collectionlog")
public interface CollectionLogConfig extends Config
{
	@ConfigItem(
		keyName = "upload_collection_log",
		name = "Allow collectionlog.net connections",
		description = "Allows collection log data to upload on log out and the chat command to pull data",
		position = 1,
		section = exportingSection,
		warning = "Enabling this option submits your IP address and account hash to a 3rd party website not controlled or verified by the RuneLite Developers."
	)
	default boolean allowApiConnections()
	{
		return false;
	}

	@ConfigItem(
		keyName = "show_collection_log_panel",
		name = "Show the collection log side panel",
		description = "Show the collection log side panel",
		position = 2
	)
	default boolean showCollectionLogSidePanel()
	{
		return true;
	}

	@ConfigSection(
		name = "Appearance",
		description = "Modify the appearance of the collection log",
		position = 3
	)
	String appearanceSection = "appearance";

	@ConfigItem(
		keyName = "display_unique_items",
		name = "Display unique items",
		description = "Display unique obtained collection log items",
		position = 1,
		section = appearanceSection
	)
	default boolean displayUniqueItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "display_total_items",
		name = "Display total items",
		description = "Display total obtained collection log items",
		position = 2,
		section = appearanceSection
	)
	default boolean displayTotalItems()
	{
		return false;
	}

	@ConfigItem(
		keyName = "display_as_percentage",
		name = "Display item counts as percentage",
		description = "Display collection log progress as a percentage",
		position = 3,
		section = appearanceSection
	)
	default boolean displayAsPercentage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlight_incomplete_pages",
		name = "Highlight incomplete pages",
		description = "Highlight incomplete page titles",
		position = 4,
		section = appearanceSection
	)
	default boolean highlightIncompletePages()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlight_color",
		name = "Completed page highlight color",
		description = "Sets the highlight color of completed pages",
		position = 5,
		section = appearanceSection
	)
	default Color highlightColor()
	{
		return new Color(13, 193, 13);
	}

	@Alpha
	@ConfigItem(
		keyName = "in_progress_highlight_color",
		name = "In progress page highlight color",
		description = "Sets the highlight color of page titles with at least one item obtained",
		position = 6,
		section = appearanceSection
	)
	default Color inProgressHighlightColor()
	{
		return new Color(231, 228, 4);
	}

	@Alpha
	@ConfigItem(
		keyName = "empty_highlight_color",
		name = "Empty page highlight color",
		description = "Sets the highlight color of page titles with no items obtained",
		position = 7,
		section = appearanceSection
	)
	default Color emptyHighlightColor()
	{
		return new Color(204, 44, 44);
	}

	@ConfigItem(
		keyName = "show_quantity_for_all_obtained_items",
		name = "Show quantity for all obtained items",
		description = "Show the quantity of items where only one has been obtained",
		position = 8,
		section = appearanceSection
	)
	default boolean showQuantityForAllObtainedItems()
	{
		return false;
	}

	@ConfigSection(
		name = "Exporting",
		description = "Config options for exporting collection log data",
		position = 4
	)
	String exportingSection = "exporting";

	@ConfigItem(
		keyName = "notify_on_export",
		name = "Notify on export",
		description = "Send a notification on collection log export",
		position = 1,
		section = exportingSection
	)
	default boolean notifyOnExport()
	{
		return true;
	}

	@ConfigItem(
		keyName = "export_chat_message",
		name = "Chat message on export",
		description = "Show exported file location in chat box on collection log export",
		position = 2,
		section = exportingSection
	)
	default boolean sendExportChatMessage()
	{
		return true;
	}
}
