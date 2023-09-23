package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;

// Base class for all unsupported or unimplemented drops
public abstract class AbstractUnsupportedDrop implements DropLuck {

    protected String itemName;

    @Override
    public abstract String getIncalculableReason(CollectionLogItem item, CollectionLogConfig config);

    @Override
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}
