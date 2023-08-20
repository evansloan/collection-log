package com.evansloan.collectionlog.luck.probability;

// Items whose KC is not tracked by the collection log cannot currently have their luck calculated. In the future,
// it may be possible to use the loot tracker plugin to implement some of these items.
public class MissingKillCountDrop extends AbstractUnsupportedDrop {

    public MissingKillCountDrop() {
        super("The collection log has no KC for one or more drop sources for this item.");
    }
}
