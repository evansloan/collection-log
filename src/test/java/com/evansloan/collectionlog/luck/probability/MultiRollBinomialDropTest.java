package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MultiRollBinomialDropTest {
    @Test
    public void testMultiRollBinomial_singleDropSource() {
        double dropChance = 0.01;
        int kc = 50;
        int rollsPerKc = 2;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        MultiRollBinomialDrop drop = new MultiRollBinomialDrop(LogItemSourceInfo.ZULRAH_KILLS, rollsPerKc, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.ZULRAH_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testMultiRollBinomial_withMultipleSources() {
        double dropChance = 0.01;
        int kc1 = 20;
        int rollsPerBoss1 = 2;
        int kc2 = 20;
        int rollsPerBoss2 = 3;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALLISTO_KILLS.getName(), kc2);
        Map<LogItemSourceInfo, Integer> rollsPerSource = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS, 2,
                LogItemSourceInfo.CALLISTO_KILLS, 3);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        List<LogItemSourceInfo> dropSources = ImmutableList.of(LogItemSourceInfo.ARTIO_KILLS, LogItemSourceInfo.CALLISTO_KILLS);
        MultiRollBinomialDrop drop = new MultiRollBinomialDrop(rollsPerSource, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testMultiRollBinomial_equivalentToBinomialForSingleRoll() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        MultiRollBinomialDrop drop = new MultiRollBinomialDrop(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, 1, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }
}