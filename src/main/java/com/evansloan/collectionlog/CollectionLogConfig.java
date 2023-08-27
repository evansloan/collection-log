package com.evansloan.collectionlog;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("collectionlog")
public interface CollectionLogConfig extends Config
{
	String NUM_ABYSSAL_LANTERNS_PURCHASED_KEY = "num_abyssal_lanterns_purchased";
	String NUM_INVALID_BARROWS_KC_KEY = "num_invalid_barrows_kc";

	String PLUGIN_VERSION = "3.1.0";

	Color DEFAULT_GREEN = new Color(13, 193, 13);
	Color DEFAULT_ORANGE = new Color(255, 152, 31);
	Color DEFAULT_RED = new Color(204, 44, 44);

	@ConfigItem(
		keyName = "show_collection_log_panel",
		name = "Show the collection log side panel",
		description = "Show the collection log side panel",
		position = 1
	)
	default boolean showCollectionLogSidePanel()
	{
		return true;
	}

	@ConfigSection(
		name = "Appearance",
		description = "Modify the appearance of the collection log",
		position = 2
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
		return DEFAULT_GREEN;
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
		return DEFAULT_ORANGE;
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
		return DEFAULT_RED;
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

	@ConfigSection(
			name = "Luck calculation",
			description = "Config options for calculation collection log luck",
			position = 5
	)
	String luckSection = "Luck calculation";

	// Other players' luck will always show, for example though the !log command, but the player may want to hide
	// their own luck because it could be unpleasant to see.
	@ConfigItem(
			keyName = "hide_personal_luck_calculation",
			name = "Hide personal luck",
			description = "Disable the display of your own luck calculations",
			position = 1,
			section = luckSection
	)
	default boolean hidePersonalLuckCalculation()
	{
		return false;
	}

	// Purchasing Abyssal Lanterns prevents calculating how many the player has received through the Rewards Guardian.
	// The calculation can be corrected if the player inputs the number purchased from the shop.
	@ConfigItem(
			keyName = NUM_ABYSSAL_LANTERNS_PURCHASED_KEY,
			name = "# Abyssal Lanterns bought",
			description = "The number of Abyssal Lanterns you bought from the Guardians of the Rift shop.",
			position = 2,
			section = luckSection
	)
	default int numAbyssalLanternsPurchased()
	{
		return 0;
	}

	// Completing Barrows without killing all 6 brothers, for example if rapidly resetting to finish Barrows combat
	// achievements, drastically reduces the chance of receiving unique loot. The player can configure an approximate
	// number of Barrows KC they have wasted, including summing fractional less-than-6-brother-kills, to make the luck
	// calculation more accurate. This is completely optional, and being exact is not really necessary.
	@ConfigItem(
			keyName = NUM_INVALID_BARROWS_KC_KEY,
			name = "# Barrows KC wasted",
			description = "The effective number of Barrows KC wasted by killing < 6 brothers. 4-5 brothers killed ~= 0.5 KC wasted.",
			position = 3,
			section = luckSection
	)
	default int numInvalidBarrowsKc()
	{
		return 0;
	}
}
