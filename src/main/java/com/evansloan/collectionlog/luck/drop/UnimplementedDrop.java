package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;

public class UnimplementedDrop extends AbstractUnsupportedDrop {

   @Override
    public String getIncalculableReason(CollectionLogItem item, CollectionLogConfig config) {
        return itemName + " is not currently supported but may be in the future.";
    }
}
