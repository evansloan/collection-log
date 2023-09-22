package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.CollectionLogKillCount;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.evansloan.collectionlog.luck.RollInfo;
import com.evansloan.collectionlog.luck.probability.PoissonBinomialDistribution;
import com.evansloan.collectionlog.luck.probability.PoissonBinomialRefinedNormalApproxDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A drop that follows the Poisson binomial distribution (used for drops that are obtained from multiple activities
// or bosses where the drop chances are not necessarily equal).
public class PoissonBinomialDrop extends AbstractDrop {

    // If numSuccesses > this value, use the Refined Normal Approximation instead of the exact distribution.
    private static final int NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD = 100;

    // If numTrials > this value, use the Refined Normal Approximation instead of the exact distribution.
    // This is necessary more for performance than accuracy.
    private static final int NORMAL_APPROX_NUM_TRIALS_THRESHOLD = 500;

    public PoissonBinomialDrop(List<RollInfo> rollInfos) {
        super(rollInfos);
    }

    // Duplicate all drop source's probabilities by the number of respective KC
    private List<Double> convertKcToProbabilities(CollectionLog collectionLog, CollectionLogConfig config) {
        List<Double> probabilities = new ArrayList<>();

        for (int i = 0; i < rollInfos.size(); i++) {
            RollInfo rollInfo = rollInfos.get(i);

            CollectionLogKillCount kc = collectionLog.searchForKillCount(rollInfo.getDropSource().getName());
            if (kc != null) {
                int numRolls = (int) Math.round(kc.getAmount() * getRollsPerKc(rollInfo, config));
                double dropChance = getDropChance(rollInfo, config);

                numRolls = getNumRollsForCustomDrops(rollInfo, i, numRolls, config);

                probabilities.addAll(Collections.nCopies(numRolls, dropChance));
            }
        }

        return probabilities;
    }

    private double getExactOrApproxCumulativeProbability(int numSuccesses, int numTrials, CollectionLog collectionLog, CollectionLogConfig config) {
        List<Double> probabilities = convertKcToProbabilities(collectionLog, config);

        if (numSuccesses > NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD
                || numTrials > NORMAL_APPROX_NUM_TRIALS_THRESHOLD) {
            return new PoissonBinomialRefinedNormalApproxDistribution(probabilities)
                    .cumulativeProbability(numSuccesses);
        } else {
            return new PoissonBinomialDistribution(probabilities)
                    .cumulativeProbability(numSuccesses);
        }
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

        return getExactOrApproxCumulativeProbability(numSuccesses - 1, numTrials, collectionLog, config);
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

        int maxEquivalentNumSuccesses = getMaxEquivalentNumSuccesses(item, collectionLog, config);

        return 1 - getExactOrApproxCumulativeProbability(maxEquivalentNumSuccesses, numTrials, collectionLog, config);
    }

    private int getNumRollsForCustomDrops(RollInfo rollInfo, int rollInfoIndex, int numRolls, CollectionLogConfig config) {
        if (
                rollInfo.getDropSource().equals(LogItemSourceInfo.TZTOK_JAD_KILLS)
                        && configOptions.contains(CollectionLogConfig.NUM_FIRE_CAPES_SACRIFICED_KEY)
        ) {
            // Only the first KC should be at non-slayer task drop chance
            if (rollInfoIndex == 0) {
                return Math.min(1, numRolls);
            }
            // All other kc should be at slayer task probability
            else if (rollInfoIndex == 1) {
                return numRolls - Math.min(1, numRolls);
            }
            // add probabilities for cape sacrifices
            else if (rollInfoIndex == 2) {
                // The player cannot have sacrificed more capes than they have KC
                return Math.max(0, Math.min(numRolls, config.numFireCapesSacrificed()));
            }
        }
        else if (
                rollInfo.getDropSource().equals(LogItemSourceInfo.TZKAL_ZUK_KILLS)
                        && configOptions.contains(CollectionLogConfig.NUM_INFERNAL_CAPES_SACRIFICED_KEY)
        ) {
            // Only the first KC should be at non-slayer task drop chance
            if (rollInfoIndex == 0) {
                return Math.min(1, numRolls);
            }
            // All other kc should be at slayer task probability
            else if (rollInfoIndex == 1) {
                return numRolls - Math.min(1, numRolls);
            }
            // add probabilities for cape sacrifices
            else if (rollInfoIndex == 2) {
                // The player cannot have sacrificed more capes than they have KC
                return Math.max(0, Math.min(numRolls, config.numInfernalCapesSacrificed()));
            }
        }

        return numRolls;
    }

}
