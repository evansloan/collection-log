package com.evansloan.collectionlog;

import lombok.Getter;
import net.runelite.api.widgets.Widget;

@Getter
class CollectionLogItem
{
	private int id;
	private String name;
	private boolean obtained;
	private int quantity;

	CollectionLogItem(Widget item)
	{
		id = item.getItemId();
		name = item.getName().split(">")[1].split("<")[0];
		obtained = item.getOpacity() == 0;
		quantity = obtained ? item.getItemQuantity() : 0;
	}
}
