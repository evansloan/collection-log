package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class CollectionLogItem
{
    @Getter
    private final int id;
    @Getter
    private final String name;
    @Getter
    private final int quantity;
    @Getter
    private final boolean obtained;
    @Getter
    private final int sequence;
}
