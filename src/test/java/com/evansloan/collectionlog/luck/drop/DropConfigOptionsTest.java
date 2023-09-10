package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.evansloan.collectionlog.luck.RollInfo;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.*;

public class DropConfigOptionsTest {

    @Test
    public void getIncalculableReason_withoutConfigDisplaysError() {
        AbstractDrop drop = new BinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, 1.0 / 700))
        .withConfigOption(CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

        CollectionLogItem item = new CollectionLogItem(1234, "an item", 1, true, 3);

        assertThat(drop.getIncalculableReason(item, null),
                CoreMatchers.containsString("only available for your own character"));
    }

    @Test
    public void getIncalculableReason_withConfig() {
        AbstractDrop drop = new BinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, 1.0 / 700))
        .withConfigOption(CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

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

        AbstractDrop abyssalLanternDrop = new BinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, dropChance))
        .withConfigOption(CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

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

        AbstractDrop abyssalLanternDrop = new BinomialDrop(new RollInfo(LogItemSourceInfo.RIFTS_SEARCHES, dropChance))
        .withConfigOption(CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY);

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

        // default 0 wasted Barrows chests.
        CollectionLogConfig config = new CollectionLogConfig() {};

        AbstractDrop drop = new BinomialDrop(new RollInfo(LogItemSourceInfo.BARROWS_CHESTS_OPENED, dropChance))
        .withConfigOption(CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY);

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

        // The player has configured the number of invalid Barrows KC.
        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public int numInvalidBarrowsKc() {
                return 35;
            }
        };

        AbstractDrop drop = new BinomialDrop(new RollInfo(LogItemSourceInfo.BARROWS_CHESTS_OPENED, dropChance))
        .withConfigOption(CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.BARROWS_CHESTS_OPENED.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_barrowsBoltRacks_disabled() {
        // The player has configured the number of invalid Barrows KC.
        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public int numInvalidBarrowsKc() {
                return 35;
            }
        };

        AbstractDrop drop = new BinomialUniformSumDrop(new RollInfo(LogItemSourceInfo.BARROWS_CHESTS_OPENED, 1.0/8.096, 7),
                35, 40)
                .withConfigOption(CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY)
                .withConfigOption(CollectionLogConfig.BARROWS_BOLT_RACKS_ENABLED_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Bolt racks", 13579, true, 0);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNotNull(incalculableReason);
        assertTrue(incalculableReason.contains("bolt rack"));
    }

    @Test
    public void calculateLuck_barrowsBoltRacks_enabled() {
        double dropChance = 0.01;
        // Even though 135 were completed, 35 were wasted, so the luck is as if only 1 was received
        int kc = 135;
        // exactly on drop rate
        int numObtained = 25 * 7;
        double expectedLuck = 0.5;
        double expectedDryness = 0.5;
        double tolerance = 0.03;

        // The player has configured the number of invalid Barrows KC.
        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public int numInvalidBarrowsKc() {
                return 35;
            }

            @Override
            public boolean barrowsBoltRacksEnabled() {
                return true;
            }
        };

        AbstractDrop drop = new BinomialUniformSumDrop(new RollInfo(LogItemSourceInfo.BARROWS_CHESTS_OPENED, dropChance, 7),
                20, 30)
                .withConfigOption(CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY)
                .withConfigOption(CollectionLogConfig.BARROWS_BOLT_RACKS_ENABLED_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.BARROWS_CHESTS_OPENED.getName(), kc);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        // Make sure subtracting invalid Barrows KC also works
        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }
}