package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;

// Items whose KC is not tracked by the collection log cannot currently have their luck calculated. In the future,
// it may be possible to use the loot tracker plugin to implement some of these items.
public class MissingKillCountDrop extends AbstractUnsupportedDrop {

    @Override
    public String getIncalculableReason(CollectionLogItem item, CollectionLogConfig config) {
        return "The collection log has no reliable KC for one or more drop sources for " + itemName;
    }
}
