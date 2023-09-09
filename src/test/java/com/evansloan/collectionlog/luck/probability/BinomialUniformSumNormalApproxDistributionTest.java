package com.evansloan.collectionlog.luck.probability;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BinomialUniformSumNormalApproxDistributionTest {

    // Test the entire approximate distribution is within a small error of the binomial distribution
    private void test_cumulativeProbability_smallRangeApproachesBinomial(int numTrials, double successProbability, double minLootOnSuccess, double maxLootOnSuccess, double tolerance) {
        double avgLoot = 0.5 * (minLootOnSuccess + maxLootOnSuccess);

        BinomialUniformSumNormalApproxDistribution dist = new BinomialUniformSumNormalApproxDistribution(
                numTrials, successProbability, minLootOnSuccess, maxLootOnSuccess);
        BinomialDistribution binomialDistribution = new BinomialDistribution(numTrials, successProbability);

        for (int successes = 0;
             successes <= numTrials;
            // sample 1000 points on the distribution, if that many exist
             successes += (int) Math.max(1, numTrials * 0.001)) {

            double expectedP = binomialDistribution.cumulativeProbability(successes);

            double actualP = dist.cumulativeProbability(successes * avgLoot);

            assertEquals("Distribution did not match binomial "
                            + ", numTrials: " + numTrials
                            + ", successProbability: " + successProbability
                            + ", minLootOnSuccess: " + minLootOnSuccess
                            + ", maxLootOnSuccess: " + maxLootOnSuccess,
                    expectedP, actualP, tolerance);
        }
    }

    @Test
    public void cumulativeProbability_smallRangeApproachesBinomial() {
        // Test every possible combination of number of trials, success chance, and amount of loot per success,
        // as long as the approx number of successes is high enough
        List<Integer> numTrialsList = ImmutableList.of(10, 100, 10_000, 100_000);
        List<Double> successChanceList = ImmutableList.of(0.5, 0.1, 0.01, 0.001, 0.00001);
        List<Double> lootOnSuccessList = ImmutableList.of(10.0, 100.0, 10_000.0, 1_000_000.0);

        double tolerance = 0.007;

        numTrialsList.forEach(numTrials -> {
            successChanceList.forEach(successChance -> {
                lootOnSuccessList.forEach(lootOnSuccess -> {
                    // Normal approximation is only valid with enough data...
                    if (successChance * numTrials >= 100) {
                        test_cumulativeProbability_smallRangeApproachesBinomial(numTrials, successChance,
                                lootOnSuccess * 0.999, lootOnSuccess * 1.001, tolerance);
                    }
                });
            });
        });
    }

    @Test
    public void cumulativeProbability_outOfRange() {
        BinomialUniformSumNormalApproxDistribution dist = new BinomialUniformSumNormalApproxDistribution(
                10, 0.5, 12, 34);

        assertEquals(0, dist.cumulativeProbability(-1), 0.0000000001);
        assertEquals(1, dist.cumulativeProbability(10 * 34), 0.0000000001);
        assertEquals(1, dist.cumulativeProbability(10 * 34 + 1), 0.0000000001);
    }

    // Test the entire approximate distribution is within a small error of the normal distribution
    private void test_cumulativeProbability_highProbabilityApproachesNormal(int numTrials, double successProbability, double minLootOnSuccess, double maxLootOnSuccess, double tolerance) {
        double avgLoot = 0.5 * (minLootOnSuccess + maxLootOnSuccess);

        BinomialUniformSumNormalApproxDistribution dist = new BinomialUniformSumNormalApproxDistribution(
                numTrials, successProbability, minLootOnSuccess, maxLootOnSuccess);

        double mean = numTrials * successProbability * avgLoot;
        double stdDev = Math.sqrt(1.0/12.0 * numTrials * Math.pow(maxLootOnSuccess - minLootOnSuccess, 2));
        NormalDistribution normalDistribution = new NormalDistribution(mean, stdDev);

        for (int successes = 0;
             successes <= numTrials;
            // sample 1000 points on the distribution, if that many exist
             successes += (int) Math.max(1, numTrials * 0.001)) {

            double expectedP = normalDistribution.cumulativeProbability(successes * avgLoot);

            double actualP = dist.cumulativeProbability(successes * avgLoot);

            assertEquals("Distribution did not match normal distribution "
                            + ", numTrials: " + numTrials
                            + ", successProbability: " + successProbability
                            + ", minLootOnSuccess: " + minLootOnSuccess
                            + ", maxLootOnSuccess: " + maxLootOnSuccess,
                    expectedP, actualP, tolerance);
        }
    }

    @Test
    public void cumulativeProbability_highProbabilityApproachesNormal() {
        // Test every possible combination of number of trials, success chance, and amount of loot per success,
        // as long as the approx number of successes is high enough
        List<Integer> numTrialsList = ImmutableList.of(10, 100, 10_000, 100_000, 1_000_000);
        List<Integer> minLootList = ImmutableList.of(0, 1, 10, 100, 10_000, 100_000, 1_000_000, 10_000_000);
        List<Integer> maxLootList = ImmutableList.of(0, 1, 10, 100, 10_000, 100_000, 1_000_000, 10_000_000);

        double successChance = 0.999999;

        double tolerance = 0.00001;

        numTrialsList.forEach(numTrials -> {
            minLootList.forEach(minLootOnSuccess -> {
                maxLootList.forEach(maxLootOnSuccess -> {
                    // Normal approximation is only valid with enough data...
                    if (successChance * numTrials >= 100) {
                        if (maxLootOnSuccess > minLootOnSuccess) {
                            test_cumulativeProbability_highProbabilityApproachesNormal(numTrials, successChance,
                                    minLootOnSuccess, maxLootOnSuccess, tolerance);
                        }
                    }
                });
            });
        });
    }

}