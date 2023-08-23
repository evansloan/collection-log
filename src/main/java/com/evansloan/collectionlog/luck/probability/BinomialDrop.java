package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// A drop that follows the standard Binomial distribution. Note: This class supports drops that come from
// multiple item sources, but it requires the drop chance for the item to be the same across all sources.
public class BinomialDrop extends AbstractDrop {

    final double dropChance;

    public BinomialDrop(List<LogItemSourceInfo> logItemSources, double dropChance) {
        super(logItemSources.stream()
                        .collect(Collectors.toMap(Function.identity(), s -> dropChance)));
        this.dropChance = dropChance;
    }

    public BinomialDrop(LogItemSourceInfo logItemSourceInfo, double dropChance) {
        super(ImmutableMap.of(logItemSourceInfo, dropChance));
        this.dropChance = dropChance;
    }

    @Override
    public double calculateLuck(CollectionLogItem item, CollectionLog collectionLog) {
        int numSuccesses = getNumSuccesses(item, collectionLog);
        if (numSuccesses <= 0) {
            return 0;
        }
        int numTrials = getNumTrials(collectionLog);
        if (numSuccesses > numTrials) {
            // this can happen if a drop source is not accounted for
            return -1;
        }

        BinomialDistribution dist = new BinomialDistribution(numTrials, dropChance);

        return dist.cumulativeProbability(numSuccesses - 1);
    }

    @Override
    public double calculateDryness(CollectionLogItem item, CollectionLog collectionLog) {
        int numSuccesses = getNumSuccesses(item, collectionLog);
        int numTrials = getNumTrials(collectionLog);
        if (numTrials <= 0) {
            return 0;
        }
        if (numSuccesses > numTrials) {
            // this can happen if a drop source is not accounted for
            return -1;
        }

        BinomialDistribution dist = new BinomialDistribution(numTrials, dropChance);

        int maxEquivalentNumSuccesses = getMaxEquivalentNumSuccesses(item, collectionLog);

        return 1 - dist.cumulativeProbability(maxEquivalentNumSuccesses);
    }

}
