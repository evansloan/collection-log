package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CollectionLogKillCount
{
    @Getter
    private final String name;
    @Getter
    private final int amount;

    public CollectionLogKillCount merge(CollectionLogKillCount killCountToMerge)
    {
        return new CollectionLogKillCount(name, amount + killCountToMerge.amount);
    }
}
