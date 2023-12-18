package com.evansloan.collectionlog;

import java.util.Arrays;
import java.util.EnumSet;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType
{
	IRONMAN(1),
	ULTIMATE_IRONMAN(2),
	HARDCORE_IRONMAN(3),
	GROUP_IRONMAN(4),
	HARDCORE_GROUP_IRONMAN(5),
	UNRANKED_GROUP_IRONMAN(6),
	ALL(100);

	private final int accountTypeVar;

	public static AccountType valueOf(int accountType)
	{
		return Arrays.stream(values())
			.filter(displayRankType -> accountType == displayRankType.accountTypeVar)
			.findFirst()
			.get();
	}

	public static EnumSet<AccountType> getRankTypesFromAccountType(int accountType)
	{
		EnumSet<AccountType> accountTypes = EnumSet.of(
			ALL,
			valueOf(accountType)
		);

		if (accountType >= IRONMAN.accountTypeVar &&
			accountType <= HARDCORE_GROUP_IRONMAN.accountTypeVar)
		{
			accountTypes.add(IRONMAN);
		}

		if (accountType >= GROUP_IRONMAN.accountTypeVar &&
			accountType <= HARDCORE_GROUP_IRONMAN.accountTypeVar)
		{
			accountTypes.add(GROUP_IRONMAN);
		}

		return accountTypes;
	}
}
