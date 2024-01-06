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

	public static AccountType valueOf(int accountTypeVar)
	{
		return Arrays.stream(values())
			.filter(accountType -> accountTypeVar == accountType.accountTypeVar)
			.findFirst()
			.orElse(null);
	}

	/**
	 * Returns an EnumSet of AccountType values based on the specified accountType varbit value.
	 *
	 * @param accountType The account type to match
	 * @return An EnumSet of AccountType values that match the specified accountType varbit value
	 */
	public static EnumSet<AccountType> getAllAccountTypes(int accountType)
	{
		EnumSet<AccountType> accountTypes = EnumSet.of(
			ALL,
			valueOf(accountType)
		);

		if (isInRange(accountType, IRONMAN, HARDCORE_IRONMAN))
		{
			accountTypes.add(IRONMAN);
		}

		if (isInRange(accountType, GROUP_IRONMAN, HARDCORE_GROUP_IRONMAN))
		{
			accountTypes.add(GROUP_IRONMAN);
		}

		return accountTypes;
	}

	private static boolean isInRange(int accountType, AccountType min, AccountType max)
	{
		return accountType >= min.accountTypeVar && accountType <= max.accountTypeVar;
	}
}
