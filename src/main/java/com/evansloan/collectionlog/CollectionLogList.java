package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum CollectionLogList
{
	BOSSES(12),
	RAIDS(16),
	CLUES(31),
	MINIGAMES(34),
	OTHER(33);

	private int listIndex;
}
