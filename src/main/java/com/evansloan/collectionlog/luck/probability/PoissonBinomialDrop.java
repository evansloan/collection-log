package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.CollectionLogKillCount;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// A drop that follows the Poisson binomial distribution (used for drops that are obtained from multiple activities
// or bosses where the drop chances are not necessarily equal).
public class PoissonBinomialDrop extends AbstractDrop {

    // If numSuccesses > this value, use the Refined Normal Approximation instead of the exact distribution
    private static final int NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD = 100;

    // If numTrials > this value, use the Refined Normal Approximation instead of the exact distribution
    private static final int NORMAL_APPROX_NUM_TRIALS_THRESHOLD = 200;

    public PoissonBinomialDrop(Map<LogItemSourceInfo, Double> logSourceDropRates) {
        super(logSourceDropRates);
    }

    // Duplicate all drop source's probabilities by the number of respective KC
    private List<Double> convertKcToProbabilities(CollectionLog collectionLog) {
        List<Double> probabilities = new ArrayList<>();

        for (LogItemSourceInfo logItemSourceInfo : logSourceDropRates.keySet()) {
            double dropRate = logSourceDropRates.get(logItemSourceInfo);
            CollectionLogKillCount kc = collectionLog.searchForKillCount(logItemSourceInfo.getName());
            if (kc != null) {
                probabilities.addAll(Collections.nCopies(kc.getAmount(), dropRate));
            }
        }

        return probabilities;
    }

    private double getExactOrApproxCumulativeProbability(int numSuccesses, int numTrials, CollectionLog collectionLog) {
        List<Double> probabilities = convertKcToProbabilities(collectionLog);

        AbstractCustomProbabilityDistribution dist;
        if (numSuccesses > NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD
                || numTrials > NORMAL_APPROX_NUM_TRIALS_THRESHOLD) {
            dist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        } else {
            dist = new PoissonBinomialDistribution(probabilities);
        }

        return dist.cumulativeProbability(numSuccesses);
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

        return getExactOrApproxCumulativeProbability(numSuccesses - 1, numTrials, collectionLog);
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

        int maxEquivalentNumSuccesses = getMaxEquivalentNumSuccesses(item, collectionLog);

        return 1 - getExactOrApproxCumulativeProbability(maxEquivalentNumSuccesses, numTrials, collectionLog);
    }

}
