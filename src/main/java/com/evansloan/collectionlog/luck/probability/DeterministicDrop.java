package com.evansloan.collectionlog.luck.probability;

// An item that does not drop in a luck-based way. For example, drops received after a fixed KC, drops unlocked with
// currencies, or drops received by killing a boss in a specific way may fit into this category.
public class DeterministicDrop extends AbstractUnsupportedDrop {

    public DeterministicDrop() {
        super("This item is obtained in a non-luck-based way.");
    }
}
