package com.evansloan.collectionlog;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.widgets.Widget;

@Getter
@Setter
class CollectionLogItem
{
	private int id;
	private int quantity;

	CollectionLogItem(Widget item)
	{
		id = item.getItemId();
		quantity = item.getItemQuantity();
	}
}
