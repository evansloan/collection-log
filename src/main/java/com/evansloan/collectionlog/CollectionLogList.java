package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum CollectionLogList
{
	BOSSES(12),
	RAIDS(16),
	CLUES(32),
	MINIGAMES(35),
	OTHER(34);

	private final int listIndex;
}
