package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// A drop that follows the Poisson binomial distribution (used for drops that are obtained from multiple activities
// or bosses where the drop chances are not necessarily equal).
public class BinomialDrop extends AbstractDropProbabilityDistribution {

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
        int numSuccesses = getNumSuccesses(item);
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

    // Return the percent chance of having received more drops in the same KC than the player has. In other words,
    // return the percent of players that would be luckier than this player by this point.
    @Override
    public double calculateDryness(CollectionLogItem item, CollectionLog collectionLog) {
        int numSuccesses = getNumSuccesses(item);
        int numTrials = getNumTrials(collectionLog);
        if (numTrials <= 0) {
            return 0;
        }
        if (numSuccesses > numTrials) {
            // this can happen if a drop source is not accounted for
            return -1;
        }

        BinomialDistribution dist = new BinomialDistribution(numTrials, dropChance);

        int maxEquivalentNumSuccesses = getMaxEquivalentNumSuccesses(item);

        return 1 - dist.cumulativeProbability(maxEquivalentNumSuccesses);
    }

}