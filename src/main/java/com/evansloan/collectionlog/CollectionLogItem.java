package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemComposition;

@Getter
@AllArgsConstructor
public class CollectionLogItem
{

    private final int id;

    private final String name;

    @Setter
    private int quantity;

    @Setter
    private boolean obtained;

    private final int sequence;

    public CollectionLogItem merge(CollectionLogItem itemToMerge)
    {
        return new CollectionLogItem(
            id,
            name,
            quantity + itemToMerge.quantity,
            obtained || itemToMerge.obtained,
            sequence
        );
    }

    public static CollectionLogItem fromItemComposition(ItemComposition itemComposition, Integer sequence)
    {
        return new CollectionLogItem(
            itemComposition.getId(),
            itemComposition.getMembersName(),
            0,
            false,
            sequence
        );
    }
}
