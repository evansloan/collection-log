package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum CollectionLogList
{
	BOSSES(12),
	RAIDS(16),
	CLUES(33),
	MINIGAMES(36),
	OTHER(35);

	private final int listIndex;
}
