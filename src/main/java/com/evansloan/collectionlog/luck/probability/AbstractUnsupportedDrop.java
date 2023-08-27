package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;

// Base class for all unsupported or unimplemented drops
public abstract class AbstractUnsupportedDrop implements DropLuck {

    private String reason;

    public AbstractUnsupportedDrop(String reason) {
        super();
        this.reason = reason;
    }

    @Override
    public String getIncalculableReason(CollectionLogItem item, CollectionLogConfig config) {
        return reason;
    }

}
