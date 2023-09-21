package com.evansloan.collectionlog;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("collectionlog")
public interface CollectionLogConfig extends Config
{
	String NUM_ABYSSAL_LANTERNS_PURCHASED_KEY = "num_abyssal_lanterns_purchased";
	String NUM_INVALID_BARROWS_KC_KEY = "num_invalid_barrows_kc";
	String BARROWS_BOLT_RACKS_ENABLED_KEY = "barrows_bolt_racks_enabled";
	String AVG_PERSONAL_COX_POINTS_KEY = "avg_personal_cox_points";
	String AVG_PERSONAL_COX_CM_POINTS_KEY = "avg_personal_cox_cm_points";
	String AVG_PERSONAL_TOB_POINTS_KEY = "avg_personal_tob_points";
	String AVG_PERSONAL_TOB_HM_POINTS_KEY = "avg_personal_tob_hm_points";
	String ENTRY_TOA_UNIQUE_CHANCE_KEY = "entry_toa_unique_chance";
	String REGULAR_TOA_UNIQUE_CHANCE_KEY = "regular_toa_unique_chance";
	String EXPERT_TOA_UNIQUE_CHANCE_KEY = "expert_toa_unique_chance";
	String AVG_NIGHTMARE_TEAM_SIZE_KEY = "avg_nightmare_team_size";
	String AVG_NIGHTMARE_REWARDS_FRACTION_KEY = "avg_nightmare_rewards_fraction";
	String AVG_NEX_REWARDS_FRACTION_KEY = "avg_nex_rewards_fraction";

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

	@ConfigItem(
			keyName = BARROWS_BOLT_RACKS_ENABLED_KEY,
			name = "Bolt racks enabled",
			description = "Whether or not you try to get enough points at Barrows to receive bolt racks.",
			position = 4,
			section = luckSection
	)
	default boolean barrowsBoltRacksEnabled()
	{
		return false;
	}

	@ConfigItem(
			keyName = AVG_PERSONAL_COX_POINTS_KEY,
			name = "CoX points per raid",
			description = "The average # of points you personally receive per Chambers of Xeric raid.",
			position = 5,
			section = luckSection
	)
	default int avgPersonalCoxPoints()
	{
		return 30_000;
	}

	@ConfigItem(
			keyName = AVG_PERSONAL_COX_CM_POINTS_KEY,
			name = "CoX CM points per raid",
			description = "The average # of points you personally receive per Chambers of Xeric Challenge Mode raid.",
			position = 6,
			section = luckSection
	)
	default int avgPersonalCoxCmPoints()
	{
		return 45_000;
	}

	@ConfigItem(
			keyName = AVG_PERSONAL_TOB_POINTS_KEY,
			name = "ToB point fraction",
			description = "The average fraction of max team points you receive per Theatre of Blood raid, including MVP points.",
			position = 7,
			section = luckSection
	)
	default double avgPersonalTobPointFraction()
	{
		return 0.25;
	}

	@ConfigItem(
			keyName = AVG_PERSONAL_TOB_HM_POINTS_KEY,
			name = "ToB HM point fraction",
			description = "The average fraction of max team points you receive per Theatre of Blood Hard Mode raid, including MVP points.",
			position = 8,
			section = luckSection
	)
	default double avgPersonalTobHmPointFraction()
	{
		return 0.2;
	}

	// Note: This assumes that there is no reason to ever do a raid less than 50 invocation level.
	@ConfigItem(
			keyName = ENTRY_TOA_UNIQUE_CHANCE_KEY,
			name = "Entry ToA Unique Chance",
			description = "Use a plugin/calc to estimate your chance of a unique for your typical raid setup. Defaults to 50 invocation level.",
			position = 9,
			section = luckSection
	)
	default double entryToaUniqueChance()
	{
		return 0.0076;
	}

	@ConfigItem(
			keyName = REGULAR_TOA_UNIQUE_CHANCE_KEY,
			name = "Regular ToA Unique Chance",
			description = "Use a plugin/calc to estimate your chance of a unique for your typical raid setup. Defaults to 150 invocation level.",
			position = 10,
			section = luckSection
	)
	default double regularToaUniqueChance()
	{
		return 0.0202;
	}

	@ConfigItem(
			keyName = EXPERT_TOA_UNIQUE_CHANCE_KEY,
			name = "Expert ToA Unique Chance",
			description = "Use a plugin/calc to estimate your chance of a unique for your typical raid setup. Defaults to 300 invocation level.",
			position = 11,
			section = luckSection
	)
	default double expertToaUniqueChance()
	{
		return 0.0440;
	}

	@ConfigItem(
			keyName = AVG_NIGHTMARE_TEAM_SIZE_KEY,
			name = "Avg Nightmare team size",
			description = "Average team size when killing The Nightmare of Ashihama. Decimals can be used.",
			position = 12,
			section = luckSection
	)
	default double avgNightmareTeamSize() {
		return 5;
	}

	@ConfigItem(
			keyName = AVG_NIGHTMARE_REWARDS_FRACTION_KEY,
			name = "Avg Nightmare rewards fraction",
			description = "Your average fraction of the contribution to killing The Nightmare of Ashihama." +
					" This should include MVP bonuses, so multiply by 1.05 if always MVP, or less accordingly.",
			position = 13,
			section = luckSection
	)
	default double avgNightmareRewardsFraction() {
		// average MVP rate of 20% with an average contribution on a 5-man team
		return 0.202;
	}

	@ConfigItem(
			keyName = AVG_NEX_REWARDS_FRACTION_KEY,
			name = "Avg Nex rewards fraction",
			description = "Your average fraction of the contribution to killing Nex." +
					" This should include MVP bonuses, so multiply by 1.1 if always MVP, or less accordingly.",
			position = 14,
			section = luckSection
	)
	default double avgNexRewardsFraction() {
		// average MVP rate of 20% with an average contribution on a 5-man team
		return 0.204;
	}
}
