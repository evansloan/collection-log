package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;

// When a drop requires a number of hidden "shards" to accumulate (e.g. the player must hit the Desert Treasure 2 boss
// vestige drop 3 times) before actually receiving the item, this is actually a binomial distribution
// where the number of successes is the number of items received multiplied by the number of
// hidden shards.
// Extra shards are not taken into account, but this is hidden information anyway, so this implementation is still
// correct.
public class HiddenShardDrop extends BinomialDrop {

    private final int shardsRequired;

    public HiddenShardDrop(LogItemSourceInfo logItemSourceInfo, double dropChancePerShard, int shardsRequired) {
        super(logItemSourceInfo, dropChancePerShard);
        this.shardsRequired = shardsRequired;
    }

    @Override
    protected int getNumSuccesses(CollectionLogItem item, CollectionLog collectionLog) {
        return item.getQuantity() * shardsRequired;
    }

    // Anyone who has received the same number of actual drops (and any unknown number of hidden shards) is
    // "in the same boat" in terms of luck
    @Override
    protected int getMaxEquivalentNumSuccesses(CollectionLogItem item, CollectionLog collectionLog) {
        return (item.getQuantity() + 1) * shardsRequired - 1;
    }

}
