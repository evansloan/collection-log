package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.List;

// A drop that follows the standard Binomial distribution. Note: This class supports drops that come from
// multiple item sources, but it requires the drop chance for the item to be the same across all sources.
public class BinomialDrop extends AbstractDrop {

    public BinomialDrop(List<RollInfo> rollInfos) {
        super(rollInfos);

        if (rollInfos.isEmpty()) {
            throw new IllegalArgumentException("At least one RollInfo is required.");
        }

        double dropChance = rollInfos.get(0).getDropChancePerRoll();
        for (RollInfo r : rollInfos) {
            if (r.getDropChancePerRoll() != dropChance) {
                throw new IllegalArgumentException("Probabilities for multiple drop sources must be equal.");
            }
        }
    }

    public BinomialDrop(RollInfo rollInfo) {
        this(ImmutableList.of(rollInfo));
    }

    @Override
    public double calculateLuck(CollectionLogItem item, CollectionLog collectionLog, CollectionLogConfig config) {
        int numSuccesses = getNumSuccesses(item, collectionLog, config);
        if (numSuccesses <= 0) {
            return 0;
        }
        int numTrials = getNumTrials(collectionLog, config);
        if (numSuccesses > numTrials) {
            // this can happen if a drop source is not accounted for
            return -1;
        }

        BinomialDistribution dist = new BinomialDistribution(numTrials, getDropChance());

        return dist.cumulativeProbability(numSuccesses - 1);
    }

    @Override
    public double calculateDryness(CollectionLogItem item, CollectionLog collectionLog, CollectionLogConfig config) {
        int numSuccesses = getNumSuccesses(item, collectionLog, config);
        int numTrials = getNumTrials(collectionLog, config);
        if (numTrials <= 0) {
            return 0;
        }
        if (numSuccesses > numTrials) {
            // this can happen if a drop source is not accounted for
            return -1;
        }

        BinomialDistribution dist = new BinomialDistribution(numTrials, getDropChance());

        int maxEquivalentNumSuccesses = getMaxEquivalentNumSuccesses(item, collectionLog, config);

        return 1 - dist.cumulativeProbability(maxEquivalentNumSuccesses);
    }

    // we have already validated that at least 1 RollInfo exists, and all RollInfos have the same drop chance
    private double getDropChance() {
        return rollInfos.get(0).getDropChancePerRoll();
    }

}
