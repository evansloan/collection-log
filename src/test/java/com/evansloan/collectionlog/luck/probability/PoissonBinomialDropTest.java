package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PoissonBinomialDropTest {

    @Test
    public void testPoissonBinomial_singleDropSource() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        PoissonBinomialDrop drop = new PoissonBinomialDrop(ImmutableList.of(new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance)));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_withMultipleSources_equalProbabilities() {
        double dropChance = 0.01;
        int kc1 = 40;
        int kc2 = 60;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_unequalProbabilities_lowKc_lowSuccess() {
        double dropChance = 0.0001;
        int kc1 = 40;
        int kc2 = 60;
        int numObtained = 0;
        double expectedLuck = 0;
        // 100kc total into a 1/10k drop = roughly 1% dryness. Makes sense.
        double expectedDryness = 0.00995;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_unequalProbabilities_midKc_lowSuccess() {
        double dropChance = 0.01;
        int kc1 = 400;
        int kc2 = 600;
        int numObtained = 2;
        double expectedLuck = 0.00006;
        double expectedDryness = 0.99709;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_unequalProbabilities_midKc_highSuccess() {
        double dropChance = 0.01;
        int kc1 = 400;
        int kc2 = 600;
        int numObtained = 20;
        double expectedLuck = 0.99697;
        double expectedDryness = 0.00122;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_unequalProbabilities_highKc_highSuccess() {
        double dropChance = 0.3;
        int kc1 = 40000;
        int kc2 = 60000;
        int numObtained = 30200;
        double expectedLuck = 0.91563;
        double expectedDryness = 0.0833;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);

        // Luck + dryness should converge summing to 1 with high KC because the confidence increases
        assertEquals(1, expectedDryness + expectedLuck, 0.002);
    }

    @Test
    public void testPoissonBinomial_unequalProbabilities_highKc_lowSuccess() {
        double dropChance = 0.00001;
        int kc1 = 40000;
        int kc2 = 60000;
        int numObtained = 4;
        double expectedLuck = 0.97845;
        double expectedDryness = 0.00187;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);

        // Luck + dryness should converge summing to 1 with high KC because the confidence increases
        assertEquals(1, expectedDryness + expectedLuck, 0.02);
    }

    @Test
    public void testPoissonBinomial_0Obtained() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0.63397;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        PoissonBinomialDrop drop = new PoissonBinomialDrop(ImmutableList.of(new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance)));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, false, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_0Trials() {
        double dropChance = 0.5;
        int kc = 0;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0;
        double tolerance = 0.00001;

        PoissonBinomialDrop drop = new PoissonBinomialDrop(ImmutableList.of(new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance)));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_spooooooooooooooooned() {
        double dropChance = 0.5;
        int kc = 1000000;
        int numObtained = 1000000;
        double expectedLuck = 1;
        double expectedDryness = 0;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.000000001;

        PoissonBinomialDrop drop = new PoissonBinomialDrop(ImmutableList.of(new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance)));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_dryyyyyyyyyyyyyyyyyyyyy() {
        double dropChance = 0.5;
        int kc = 100000;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.000000001;

        PoissonBinomialDrop drop = new PoissonBinomialDrop(ImmutableList.of(new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance)));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, false, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_multiSource_0Obtained() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0.63397;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc / 2,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc / 2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, false, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_multiSource_0Trials() {
        double dropChance = 0.5;
        int kc = 0;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0;
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_multiSource_spooooooooooooooooned() {
        double dropChance = 0.5;
        int kc = 1000000;
        int numObtained = 1000000;
        double expectedLuck = 1;
        double expectedDryness = 0;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.000000001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc / 2,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc / 2);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_multiSource_dryyyyyyyyyyyyyyyyyyyyy() {
        double dropChance = 0.5;
        int kc = 100000;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.000000001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance),
                new RollInfo(LogItemSourceInfo.CALLISTO_KILLS, dropChance));
        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, false, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testPoissonBinomial_missingDropSource() {
        double dropChance = 0.5;
        int kc = 10;
        // Somehow, the number of items obtained is greater than kc. The collection log must be out of date, or the item
        // has a drop source that was not configured properly.
        int numObtained = 11;
        double expectedLuck = -1;
        double expectedDryness = -1;
        double tolerance = 0.00001;

        PoissonBinomialDrop drop = new PoissonBinomialDrop(ImmutableList.of(new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance)));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testKcString() {
        double dropChance = 0.01;
        int kc = 100;
        String expectedKcString = "100x Abyssal Sire kills";

        PoissonBinomialDrop drop = new PoissonBinomialDrop(ImmutableList.of(new RollInfo(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance)));

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        String actualKcString = drop.getKillCountDescription(mockCollectionLog);
        assertEquals(expectedKcString, actualKcString);
    }

    @Test
    public void testKcString_multipleDropSources() {
        double dropChance1 = 0.01;
        double dropChance2 = 0.02;
        double dropChance3 = 0.03;
        int kc1 = 100;
        int kc2 = 200;
        int kc3 = 150;
        String expectedKcString = "200x Calvar'ion kills, 150x Spindel kills, 100x Artio kills";

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALVARION_KILLS.getName(), kc2,
                LogItemSourceInfo.SPINDEL_KILLS.getName(), kc3
        );

        List<RollInfo> rollInfos = ImmutableList.of(
                new RollInfo(LogItemSourceInfo.ARTIO_KILLS, dropChance1),
                new RollInfo(LogItemSourceInfo.CALVARION_KILLS, dropChance2),
                new RollInfo(LogItemSourceInfo.SPINDEL_KILLS, dropChance3));

        PoissonBinomialDrop drop = new PoissonBinomialDrop(rollInfos);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String actualKcString = drop.getKillCountDescription(mockCollectionLog);
        assertEquals(expectedKcString, actualKcString);
    }

}