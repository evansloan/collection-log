package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfiguredBinomialDropTest {

    @Test
    public void getIncalculableReason_withoutConfigDisplaysError() {
        ConfiguredBinomialDrop drop = new ConfiguredBinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, 1.0 / 700),
                CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

        CollectionLogItem item = new CollectionLogItem(1234, "an item", 1, true, 3);

        assertThat(drop.getIncalculableReason(item, null),
                CoreMatchers.containsString("only available for your own character"));
    }

    @Test
    public void getIncalculableReason_withConfig() {
        ConfiguredBinomialDrop drop = new ConfiguredBinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, 1.0 / 700),
                CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

        CollectionLogItem item = new CollectionLogItem(1234, "an item", 1, true, 3);

        CollectionLogConfig config = new CollectionLogConfig() {};

        assertNull(drop.getIncalculableReason(item, config));
    }

    @Test
    public void calculateLuck_abyssalLantern_withoutModification() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        // default 0 lanterns purchased
        CollectionLogConfig config = new CollectionLogConfig() {};

        ConfiguredBinomialDrop abyssalLanternDrop = new ConfiguredBinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, dropChance),
                CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.RIFTS_SEARCHES.getName(), kc);

        double actualLuck = abyssalLanternDrop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = abyssalLanternDrop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_abyssalLantern_withModification() {
        double dropChance = 0.01;
        int kc = 100;
        // Even though 3 were obtained, 2 were purchased, so the luck is as if only 1 was received
        int numObtained = 3;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        // The player has configured the number of lanterns purchased to 2
        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public int numAbyssalLanternsPurchased() {
                return 2;
            }
        };

        ConfiguredBinomialDrop abyssalLanternDrop = new ConfiguredBinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, dropChance),
                CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.RIFTS_SEARCHES.getName(), kc);

        double actualLuck = abyssalLanternDrop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = abyssalLanternDrop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_wastedBarrowsChests_withoutModification() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        // default 0 lanterns purchased
        CollectionLogConfig config = new CollectionLogConfig() {};

        ConfiguredBinomialDrop drop = new ConfiguredBinomialDrop(new RollInfo(LogItemSourceInfo.BARROWS_CHESTS_OPENED, dropChance),
                CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.BARROWS_CHESTS_OPENED.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_wastedBarrowsChests_withModification() {
        double dropChance = 0.01;
        // Even though 135 were completed, 35 were wasted, so the luck is as if only 1 was received
        int kc = 135;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        // The player has configured the number of lanterns purchased to 2
        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public int numInvalidBarrowsKc() {
                return 35;
            }
        };

        ConfiguredBinomialDrop drop = new ConfiguredBinomialDrop(new RollInfo(LogItemSourceInfo.BARROWS_CHESTS_OPENED, dropChance),
                CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.BARROWS_CHESTS_OPENED.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }
}