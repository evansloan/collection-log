package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;

// When a fixed-size stack has a chance to drop (e.g. 1/64 chance for 3 Key master teleport scrolls), this is actually
// a binomial distribution where the number of successes is the number of items received divided by the stack size
public class FixedStackDrop extends BinomialDrop {

    private final int stackSize;

    public FixedStackDrop(LogItemSourceInfo logItemSourceInfo, double dropChancePerStack, int stackSize) {
        super(logItemSourceInfo, dropChancePerStack);
        this.stackSize = stackSize;
    }

    @Override
    protected int getNumSuccesses(CollectionLogItem item, CollectionLog collectionLog) {
        return item.getQuantity() / stackSize;
    }
}
