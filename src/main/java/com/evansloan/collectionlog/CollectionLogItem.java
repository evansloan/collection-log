package com.evansloan.collectionlog;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.widgets.Widget;

@Getter
@Setter
class CollectionLogItem
{
	private int id;
	@SerializedName(value="qty", alternate = {"quantity"})
	private int quantity;

	CollectionLogItem(Widget item)
	{
		id = item.getItemId();
		quantity = item.getOpacity() == 0 ? item.getItemQuantity() : 0;
	}
}
