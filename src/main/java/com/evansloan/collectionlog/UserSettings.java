package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettings
{
	private DisplayRankType displayRank = DisplayRankType.ALL;
	private boolean showQuantity = true;
}
