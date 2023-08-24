package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;

// This is identical to BinomialDrop, but a specific KC is ignored when a drop is guaranteed at that KC. For example,
// Vorkath's head is guaranteed at 50 kc, so to calculate "luck", the 1 is subtracted from both KC and # heads received
// starting at the 50th kc.
public class GuaranteedOnceBinomialDrop extends BinomialDrop {

    private final int dropGuaranteedOnKc;

    public GuaranteedOnceBinomialDrop(RollInfo rollInfo, int dropGuaranteedOnKc) {
        super(rollInfo);
        this.dropGuaranteedOnKc = dropGuaranteedOnKc;
    }

    @Override
    protected int getNumSuccesses(CollectionLogItem item, CollectionLog collectionLog) {
        int kc = super.getNumTrials(collectionLog);
        return kc < dropGuaranteedOnKc ? item.getQuantity() : item.getQuantity() - 1;
    }

    @Override
    protected int getNumTrials(CollectionLog collectionLog) {
        int kc = super.getNumTrials(collectionLog);
        return kc < dropGuaranteedOnKc ? kc : kc - 1;
    }
}
