package com.evansloan.collectionlog.luck.probability;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BinomialDiscreteApproxUniformSumDistributionTest {

    // Test the entire approximate distribution is within a small error of the binomial distribution
    private void test_cumulativeProbability_approachesBinomial(int numTrials, double successProbability, double minLootOnSuccess, double maxLootOnSuccess, double tolerance) {
        double avgLoot = 0.5 * (minLootOnSuccess + maxLootOnSuccess);

        BinomialDiscreteApproxUniformSumDistribution dist = new BinomialDiscreteApproxUniformSumDistribution(
                numTrials, successProbability, minLootOnSuccess, maxLootOnSuccess);
        BinomialDistribution binomialDistribution = new BinomialDistribution(numTrials, successProbability);

        for (int successes = 0;
             successes <= numTrials;
            // sample many points on the distribution, incrementing by at least 1 full success
             successes += (int) Math.max(1, numTrials * 0.01)) {

            double expectedP = binomialDistribution.cumulativeProbability(successes);

            // multiply by max loot range instead of avg range since we will accept any loot roll for comparing against
            // the binomial distribution
            double actualP = dist.cumulativeProbability(successes * maxLootOnSuccess);

            assertEquals("Distribution did not match binomial "
                            + ", numTrials: " + numTrials
                            + ", successProbability: " + successProbability
                            + ", minLootOnSuccess: " + minLootOnSuccess
                            + ", maxLootOnSuccess: " + maxLootOnSuccess,
                    expectedP, actualP, tolerance);
        }
    }

    @Test
    public void cumulativeProbability_approachesBinomial() {
        // Test every possible combination of number of trials, success chance, and amount of loot per success,
        // as long as p * n is high enough
        List<Integer> numTrialsList = ImmutableList.of(0, 1, 10, 100, 1000, 10_000);
        List<Double> successChanceList = ImmutableList.of(0.5, 0.1, 0.01, 0.001, 0.00001);
        List<Double> lootOnSuccessList = ImmutableList.of(100.0, 10_000.0, 1_000_000.0);

        double tolerance = 0.03;

        numTrialsList.forEach(numTrials -> {
            successChanceList.forEach(successChance -> {
                lootOnSuccessList.forEach(lootOnSuccess -> {
                    test_cumulativeProbability_approachesBinomial(numTrials, successChance,
                                lootOnSuccess * 0.9999, lootOnSuccess * 1.0001, tolerance);
                });
            });
        });
    }

    @Test
    public void cumulativeProbability_outOfRange() {
        BinomialDiscreteApproxUniformSumDistribution dist = new BinomialDiscreteApproxUniformSumDistribution(
                10, 0.5, 12, 34);

        assertEquals(0, dist.cumulativeProbability(-1), 0.0000000001);
        assertEquals(1, dist.cumulativeProbability(10 * 34), 0.0000000001);
        assertEquals(1, dist.cumulativeProbability(10 * 34 + 1), 0.0000000001);
    }


    // Test the entire approximate distribution is within a small error of the normal distribution
    private void test_cumulativeProbability_highProbabilityApproachesNormal(int numTrials, double successProbability, double minLootOnSuccess, double maxLootOnSuccess, double tolerance) {
        double avgLoot = 0.5 * (minLootOnSuccess + maxLootOnSuccess);

        BinomialDiscreteApproxUniformSumDistribution dist = new BinomialDiscreteApproxUniformSumDistribution(
                numTrials, successProbability, minLootOnSuccess, maxLootOnSuccess);

        double mean = numTrials * successProbability * avgLoot;
        double stdDev = Math.sqrt(1.0/12.0 * numTrials * Math.pow(maxLootOnSuccess - minLootOnSuccess, 2));
        NormalDistribution normalDistribution = new NormalDistribution(mean, stdDev);

        for (int successes = 0;
             successes <= numTrials;
            // sample many points on the distribution, incrementing by at least 1 full success
             successes += (int) Math.max(1, numTrials * 0.01)) {

            // use a continuity correction for a fair comparison
            double expectedP = normalDistribution.cumulativeProbability(successes * avgLoot + 0.5);

            // TODO: This is rather slow. Could use some optimization.
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
    @Ignore("This test is slow - manual run only.")
    public void cumulativeProbability_highProbabilityApproachesNormal() {
        // High success chance means we are summing nearly "numTrials" uniform distributions, and the central limit
        // theorem works well for the sum of continuous uniform distributions.
        List<Integer> numTrialsList = ImmutableList.of(101);
        List<Integer> minLootList = ImmutableList.of(0, 1, 10, 100, 10_000, 100_000, 1_000_000);
        List<Integer> maxLootList = ImmutableList.of(0, 1, 10, 100, 10_000, 100_000, 1_000_000);

        double successChance = 0.999999;

        // within 0.5% of the continuity-corrected normal distribution is pretty good...
        double tolerance = 0.005;

        numTrialsList.forEach(numTrials -> {
            minLootList.forEach(minLootOnSuccess -> {
                maxLootList.forEach(maxLootOnSuccess -> {
                    if (maxLootOnSuccess > minLootOnSuccess) {
                        test_cumulativeProbability_highProbabilityApproachesNormal(numTrials, successChance,
                                minLootOnSuccess, maxLootOnSuccess, tolerance);
                    }
                });
            });
        });
    }

}