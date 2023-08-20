package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.google.common.collect.ImmutableMap;

// A normal, single-source, fixed drop rate item. This is a special case of the Poisson binomial distribution
// where all success probabilities are the same.
public class BinomialDrop extends PoissonBinomialDrop {

    public BinomialDrop(LogItemSourceInfo logItemSourceInfo, double dropChance) {
        super(ImmutableMap.of(logItemSourceInfo, dropChance));
    }

}
