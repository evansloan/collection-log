package com.evansloan.collectionlog;

import java.util.EnumSet;
import net.runelite.api.vars.AccountType;

public enum DisplayRankType
{
	ALL,
	NORMAL,
	IRONMAN,
	HARDCORE_IRONMAN,
	ULTIMATE_IRONMAN,
	GROUP_IRONMAN,
	HARDCORE_GROUP_IRONMAN;

	public static EnumSet<DisplayRankType> getRankTypesFromAccountType(AccountType accountType)
	{
		EnumSet<DisplayRankType> displayRankTypes = EnumSet.of(
			ALL,
			DisplayRankType.valueOf(accountType.toString())
		);

		if (accountType.isIronman())
		{
			displayRankTypes.add(IRONMAN);
		}
		if (accountType.isGroupIronman())
		{
			displayRankTypes.add(GROUP_IRONMAN);
		}

		return displayRankTypes;
	}
}
