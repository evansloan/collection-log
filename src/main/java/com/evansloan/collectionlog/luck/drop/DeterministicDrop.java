package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;

// An item that does not drop in a luck-based way. For example, drops received after a fixed KC, drops unlocked with
// currencies, or drops received by killing a boss in a specific way may fit into this category.
//
// TODO: revisit all "deterministic" drops. The rules on which items purchased from shops are tracked vs.
// untracked are very unclear.
public class DeterministicDrop extends AbstractUnsupportedDrop {

    @Override
    public String getIncalculableReason(CollectionLogItem item, CollectionLogConfig config) {
        return itemName + " is obtained in a non-luck-based way.";
    }

}
