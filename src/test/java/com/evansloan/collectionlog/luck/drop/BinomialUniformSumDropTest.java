package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.evansloan.collectionlog.luck.RollInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BinomialUniformSumDropTest {

    @Test
    public void testBinomialUniformSum_singleDropSource_onDropRate() {
        double dropChance = 0.1;
        int kc = 100000;
        int minRoll = 1000;
        int maxRoll = 3000;
        int numObtained = (int) ((minRoll + maxRoll) / 2 * dropChance * kc);
        double expectedLuck = 0.5;
        double expectedDryness = 0.5;
        // luck/dryness will be slightly off of 0.5 because of uncertainty
        double tolerance = 0.0025;

        BinomialUniformSumDrop drop = new BinomialUniformSumDrop(
                new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance),
                minRoll,
                maxRoll
        );

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, null);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, null);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testBinomialUniformSum_withMultipleSources_equalProbabilities() {
        double dropChance = 0.1;
        int kc1 = 100000;
        int kc2 = 200000;
        int minRoll = 1000;
        int maxRoll = 3000;
        int numObtained = (int) ((minRoll + maxRoll) / 2 * dropChance * (kc1 + kc2));
        double expectedLuck = 0.5;
        double expectedDryness = 0.5;
        // luck/dryness will be slightly off of 0.5 because of uncertainty and skewed distribution
        double tolerance = 0.002;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));

        BinomialUniformSumDrop drop = new BinomialUniformSumDrop(
                rollInfos,
                minRoll,
                maxRoll
        );

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, null);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, null);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testBinomialUniformSum_multiRoll_onDropRate() {
        double dropChance = 0.1;
        int kc = 1000000;
        int minRoll = 1000;
        int maxRoll = 3000;
        int rollsPerKc = 10;
        int numObtained = (int) ((minRoll + maxRoll) / 2 * dropChance * kc * rollsPerKc);
        double expectedLuck = 0.5;
        double expectedDryness = 0.5;
        // luck/dryness will be slightly off of 0.5 because of uncertainty and skewed distribution
        double tolerance = 0.00025;

        BinomialUniformSumDrop drop = new BinomialUniformSumDrop(
                new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance, rollsPerKc),
                minRoll,
                maxRoll
        );

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, null);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, null);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    private void doTest_binomialUniformSum(int kc, double dropChance, int numObtained, int minRoll, int maxRoll, double tolerance,
                                           double expectedLuck, double expectedDryness) {
        BinomialUniformSumDrop drop = new BinomialUniformSumDrop(
                new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance),
                minRoll,
                maxRoll
        );

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", (int) numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, null);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, null);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    private void doTest_binomialUniformSum_onDropRate(int kc, double dropChance, int minRoll, int maxRoll, double tolerance) {
        double expectedLuck = 0.5;
        double expectedDryness = 0.5;
        long numObtained = (long) ((minRoll + maxRoll) / 2 * dropChance * kc);
        if (numObtained > Integer.MAX_VALUE) {
            return;
        }
        doTest_binomialUniformSum(kc, dropChance, (int) numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_onDropRate_allCombinations() {
        List<Integer> numTrialsList = ImmutableList.of(100, 10_000, 100_000, 1_000_000);
        List<Double> successChanceList = ImmutableList.of(0.5, 0.1, 0.01, 0.001, 0.00001);
        // Making sure this drop works for large KC and loot per KC
        List<Integer> minLootList = ImmutableList.of(100, 10_000, 100_000, 1_000_000, 10_000_000);
        List<Integer> maxLootList = ImmutableList.of(100, 10_000, 100_000, 1_000_000, 10_000_000);

        // This means that luck/dryness are within a small percentage of 0.5 for every possible combination of the above
        // settings
        double tolerance = 0.01;

        numTrialsList.forEach(numTrials -> {
            successChanceList.forEach(successChance -> {
                minLootList.forEach(minLoot -> {
                    maxLootList.forEach(maxLoot -> {
                    // Median will only be near mean with enough data...
                        if (numTrials * successChance > 10 && maxLoot > minLoot) {
                            doTest_binomialUniformSum_onDropRate(numTrials, successChance,
                                    minLoot, maxLoot, tolerance);
                        }
                    });
                });
            });
        });
    }

    @Test
    public void testBinomialUniformSum_exact_sample() {
        int kc = 20;
        double dropChance = 0.01;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 5;
        int maxRoll = 25;

        int numObtained = 20;

        // The binomial chance of receiving no drops at all in 20 kc would be about 81.8%.
        // If 1 drop was received (16.5% chance), the chance of receiving less than 20 is 15/21.
        // If 2+ drops were received (1.7% chance), the chance of receiving less than 20 is very low (roughly 0.25*0.25, plus a little bit).
        // So, the expected luck should be approximately 0.818 + 0.165 * (15/21) + 0.017 * (~0.08) ~= 0.9372
        double expectedLuck = 0.9372;
        // If 1 drop was received (16.5% chance), the chance of receiving more than 20 is 5/21.
        // If 2+ drops were received (1.7% chance), the chance of receiving more than 20 is very high (roughly 1 - 0.25*0.25, minus a tiny bit).
        // So, the expected dryness should be approximately 0.165 * (5/21) + 0.017 * (~0.93) ~= 0.0549
        double expectedDryness = 0.0549;
        // luck/dryness could be slightly off because this is approximate
        double tolerance = 0.001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minExact_spooned_lowRange() {
        int kc = 20;
        double dropChance = 0.01;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 5;
        int maxRoll = 25;
        int numObtained = 5 * maxRoll;

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minExact_spooned_highRange() {
        int kc = 20;
        double dropChance = 0.01;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 500000;
        int maxRoll = 2500000;
        int numObtained = 5 * maxRoll;

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minExact_dry_lowRange() {
        int kc = 50;
        double dropChance = 0.1;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 5;
        int maxRoll = 25;
        int numObtained = 0;

        double expectedLuck = 0;
        double expectedDryness = 0.99;
        double tolerance = 0.01;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minExact_dry_highRange() {
        int kc = 50;
        double dropChance = 0.1;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 500000;
        int maxRoll = 2500000;
        int numObtained = 0;

        double expectedLuck = 0;
        double expectedDryness = 0.99;
        double tolerance = 0.01;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_maxExact_spooned_lowRange() {
        int kc = 500;
        double dropChance = 0.199;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 1);
        int minRoll = 5;
        int maxRoll = 25;
        int numObtained = 150 * (minRoll + maxRoll) / 2;

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_maxExact_spooned_highRange() {
        int kc = 500;
        double dropChance = 0.199;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 1);
        int minRoll = 500000;
        int maxRoll = 2500000;
        int numObtained = 150 * (minRoll + maxRoll) / 2;

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_maxExact_dry_lowRange() {
        int kc = 500;
        double dropChance = 0.199;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 1);
        int minRoll = 5;
        int maxRoll = 25;
        int numObtained = 50 * (minRoll + maxRoll) / 2;

        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_maxExact_dry_highRange() {
        int kc = 500;
        double dropChance = 0.199;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 1);
        int minRoll = 500000;
        int maxRoll = 2500000;
        int numObtained = 50 * (minRoll + maxRoll) / 2;

        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_approx_sample() {
        int kc = 2000;
        double dropChance = 0.051;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 5;
        int maxRoll = 25;

        int numObtained = 95 * 15;

        // comparing with a normal distribution with mean = 102*15 and standard deviation = approximately
        // 15*sqrt(2000*0.051*(1-0.051)) ~ 147.578 (assuming binomial behavior + some extra deviation from the min/max roll behavior)
        double expectedLuck = 0.23839;
        double expectedDryness = 0.76161;
        // luck/dryness could be slightly off because this is very approximate
        double tolerance = 0.04;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minApprox_spooned_lowRange() {
        int kc = 2000;
        double dropChance = 0.051;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 2);
        int minRoll = 5;
        int maxRoll = 25;
        // Any lower than this fails the test. This makes sense.
        int numObtained = (int) (1.4 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minApprox_spooned_highRange() {
        int kc = 2000;
        double dropChance = 0.051;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 2);
        int minRoll = 500000;
        int maxRoll = 2500000;
        int numObtained = (int) (1.4 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minApprox_dry_lowRange() {
        int kc = 2000;
        double dropChance = 0.051;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 2);
        int minRoll = 5;
        int maxRoll = 25;
        int numObtained = (int) (0.6 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_minApprox_dry_highRange() {
        int kc = 2000;
        double dropChance = 0.051;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        assertEquals(BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD, kc * dropChance, 2);
        int minRoll = 500000;
        int maxRoll = 2500000;
        int numObtained = (int) (0.6 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_manySuccessesApprox_spooned_lowRange() {
        int kc = 1_000_000;
        double dropChance = 0.101;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD * 100);
        int minRoll = 5;
        int maxRoll = 25;
        int numObtained = (int) (1.02 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_manySuccessesApprox_spooned_highRange() {
        int kc = 1_000_000;
        double dropChance = 0.101;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD * 100);
        // any higher runs into integer overflow, but this would not happen in a real scenario because the collection log
        // likely only stores up to Integer.MAX_VALUE items anyway.
        int minRoll = 5000;
        int maxRoll = 25000;
        int numObtained = (int) (1.02 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 1;
        double expectedDryness = 0;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_manySuccessesApprox_dry_lowRange() {
        int kc = 1_000_000;
        double dropChance = 0.101;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD * 100);
        int minRoll = 5;
        int maxRoll = 25;
        int numObtained = (int) (0.98 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_manySuccessesApprox_dry_highRange() {
        int kc = 1_000_000;
        double dropChance = 0.101;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD * 100);
        int minRoll = 5000;
        int maxRoll = 25000;
        int numObtained = (int) (0.98 * kc * dropChance * (minRoll + maxRoll) / 2);

        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.0001;

        doTest_binomialUniformSum(kc, dropChance, numObtained, minRoll, maxRoll, tolerance, expectedLuck, expectedDryness);
    }

    @Test
    public void testBinomialUniformSum_approx_guaranteed() {
        int kc = 2000;
        double dropChance = 1;
        assertTrue(kc * dropChance > BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 1;
        int maxRoll = 3;

        // comparing with a normal distribution with mean = 4000 and
        // standard deviation = sqrt(2000 * (3^3 - 1) / 12) ~= 36.5148, since the overall variance is kc * variance of a discrete
        // uniform distribution

        // This is not very good.  I think the uniform approximation of the discrete distribution is not reliable and
        // needs to be improved in the future. The variance may be off by a factor of ~2.
        double tolerance = 0.07;

        doTest_binomialUniformSum(kc, dropChance, 3900, minRoll, maxRoll, tolerance, 0.00308, 0.99692);
        doTest_binomialUniformSum(kc, dropChance, 3950, minRoll, maxRoll, tolerance, 0.08545, 0.91455);
        doTest_binomialUniformSum(kc, dropChance, 4000, minRoll, maxRoll, tolerance, 0.5, 0.5);
        doTest_binomialUniformSum(kc, dropChance, 4050, minRoll, maxRoll, tolerance, 0.91455, 0.08545);
        doTest_binomialUniformSum(kc, dropChance, 4100, minRoll, maxRoll, tolerance, 0.99692, 0.00308);
    }

    @Test
    public void testBinomialUniformSum_exact_guaranteed() {
        int kc = 2;
        double dropChance = 1;
        assertTrue(kc * dropChance < BinomialUniformSumDrop.NORMAL_APPROX_NUM_SUCCESSES_THRESHOLD);
        int minRoll = 1;
        int maxRoll = 3;

        // comparing with a normal distribution with mean = 4 and
        // standard deviation = sqrt(2 * (3^3 - 1) / 12) ~= 1.1547, since the overall variance is kc * variance of a discrete
        // uniform distribution

        // luck/dryness could be slightly off because this is very approximate
        double tolerance = 0.04;

        // this should be impossible. This is arguably a bug (and -1, -1 is also valid), but it's fine.
        doTest_binomialUniformSum(kc, dropChance, 1, minRoll, maxRoll, tolerance, 0, 1);
        // expected cases
        doTest_binomialUniformSum(kc, dropChance, 2, minRoll, maxRoll, tolerance, 0 / 9.0, 8 / 9.0);
        doTest_binomialUniformSum(kc, dropChance, 3, minRoll, maxRoll, tolerance, 1 / 9.0, 6 / 9.0);
        doTest_binomialUniformSum(kc, dropChance, 4, minRoll, maxRoll, tolerance, 3 / 9.0, 3 / 9.0);
        doTest_binomialUniformSum(kc, dropChance, 5, minRoll, maxRoll, tolerance, 6 / 9.0, 1 / 9.0);
        doTest_binomialUniformSum(kc, dropChance, 6, minRoll, maxRoll, tolerance, 8 / 9.0, 0 / 9.0);
        // this should be impossible
        doTest_binomialUniformSum(kc, dropChance, 7, minRoll, maxRoll, tolerance, -1, -1);
    }

}