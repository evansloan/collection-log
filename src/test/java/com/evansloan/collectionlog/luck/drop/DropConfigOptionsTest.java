package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.evansloan.collectionlog.luck.RollInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Map;

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

    @Test
    public void calculateLuck_CoXUniques() {
        // Twisted bow drop rate is about 30_000 / 867_600 / 34.5 = 0.100%

        // 0.100% chance per KC
        int regularKc = 1000;
        // 0.150% chance per KC
        int cmKc = 2000;
        // exactly on drop rate.
        int numObtained = 4;

        // Luck should ~= dryness because the player is almost exactly on drop rate for twisted bows.
        double expectedLuck = 0.43;
        double expectedDryness = 0.37;
        double tolerance = 0.01;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public int avgPersonalCoxPoints() {
                return 30_000;
            }

            @Override
            public int avgPersonalCoxCmPoints() {
                return 45_000;
            }
        };

        AbstractDrop drop =  new PoissonBinomialDrop(ImmutableList.of(
                        new RollInfo(LogItemSourceInfo.CHAMBERS_OF_XERIC_COMPLETIONS, 1.0 / 34.5),
                        new RollInfo(LogItemSourceInfo.CHAMBERS_OF_XERIC_CM_COMPLETIONS, 1.0 / 34.5)
                ))
                        .withConfigOption(CollectionLogConfig.AVG_PERSONAL_COX_POINTS_KEY)
                        .withConfigOption(CollectionLogConfig.AVG_PERSONAL_COX_CM_POINTS_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.CHAMBERS_OF_XERIC_COMPLETIONS.getName(), regularKc,
                LogItemSourceInfo.CHAMBERS_OF_XERIC_CM_COMPLETIONS.getName(), cmKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_CoXCmRecolorsUnaffectedByPoints() {
        int kc = 400;
        int numObtained = 1;

        // calculated online
        double expectedLuck = 0.368;
        double expectedDryness = 0.264;
        double tolerance = 0.001;

        CollectionLogConfig config = new CollectionLogConfig(){};

        AbstractDrop drop = new BinomialDrop(new RollInfo(LogItemSourceInfo.CHAMBERS_OF_XERIC_CM_COMPLETIONS, 1.0 / 400));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.CHAMBERS_OF_XERIC_CM_COMPLETIONS.getName(), kc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_ToBUniques() {
        // Scythe drop rate is about 0.25 / 172.9 = 0.1446% for regular KC
        // Let's use 0.3 / 138.6 = 0.2165% for hard mode KC to help make sure this test is accurate

        int regularKc = 692;
        int hmKc = 462;
        // exactly on drop rate.
        int numObtained = 2;

        // Approximating based on a binomial with success probability 1/560
        double expectedLuck = 0.389;
        double expectedDryness = 0.340;
        // That's a surprisingly good approximation (< ~2% error)
        double tolerance = 0.02;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double avgPersonalTobPointFraction() {
                return 0.25;
            }

            @Override
            public double avgPersonalTobHmPointFraction() {
                return 0.3;
            }
        };

        AbstractDrop drop =  new PoissonBinomialDrop(ImmutableList.of(
                new RollInfo(LogItemSourceInfo.THEATRE_OF_BLOOD_COMPLETIONS, 1.0 / 172.9),
                new RollInfo(LogItemSourceInfo.THEATRE_OF_BLOOD_HARD_COMPLETIONS, 1.0 / 138.6)
        ))
                .withConfigOption(CollectionLogConfig.AVG_PERSONAL_TOB_POINTS_KEY)
                .withConfigOption(CollectionLogConfig.AVG_PERSONAL_TOB_HM_POINTS_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Scythe of Vitur", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.THEATRE_OF_BLOOD_COMPLETIONS.getName(), regularKc,
                LogItemSourceInfo.THEATRE_OF_BLOOD_HARD_COMPLETIONS.getName(), hmKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_ToBHmRecolorsUnaffectedByPoints() {
        int kc = 400;
        int numObtained = 1;

        // calculated online
        double expectedLuck = 0.368;
        double expectedDryness = 0.264;
        double tolerance = 0.001;

        CollectionLogConfig config = new CollectionLogConfig(){};

        AbstractDrop drop = new BinomialDrop(new RollInfo(LogItemSourceInfo.THEATRE_OF_BLOOD_HARD_COMPLETIONS, 1.0 / 400));

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.THEATRE_OF_BLOOD_HARD_COMPLETIONS.getName(), kc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_ToAUniques() {
        // Shadow drop rate is about:
        // 0.01 / 24 / 50 = 0.000833333% for entry KC
        // 0.02 / 24  = 0.083333333% for regular KC
        // 0.05 / 24  = 0.208333333% for expert KC

        // equivalent to 1/5th of a shadow
        int entryKc = 24000;
        // equivalent to 1 shadow
        int regularKc = 1200;
        // equivalent to 2 shadows
        int expertKc = 960;

        // on drop rate. Should be very slightly dry because of the entry KC.
        int numObtained = 3;

        // Approximating based on a binomial with success probability 1/1000, n = 3200 (expected value = 2+1+0.2)
        double expectedLuck = 0.379;
        double expectedDryness = 0.398;
        // extremely low error compared to the binomial
        double tolerance = 0.005;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double entryToaUniqueChance() {
                return 0.01;
            }

            @Override
            public double regularToaUniqueChance() {
                return 0.02;
            }

            @Override
            public double expertToaUniqueChance() {
                return 0.05;
            }
        };

        AbstractDrop drop = new PoissonBinomialDrop(ImmutableList.of(
                // drop chance is reduced by 98% in entry mode
                new RollInfo(LogItemSourceInfo.TOMBS_OF_AMASCUT_ENTRY_COMPLETIONS, 1.0 / 24 / 50),
                new RollInfo(LogItemSourceInfo.TOMBS_OF_AMASCUT_COMPLETIONS, 1.0 / 24),
                new RollInfo(LogItemSourceInfo.TOMBS_OF_AMASCUT_EXPERT_COMPLETIONS, 1.0 / 24)
        ))
                .withConfigOption(CollectionLogConfig.ENTRY_TOA_UNIQUE_CHANCE_KEY)
                .withConfigOption(CollectionLogConfig.REGULAR_TOA_UNIQUE_CHANCE_KEY)
                .withConfigOption(CollectionLogConfig.EXPERT_TOA_UNIQUE_CHANCE_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Tumeken's shadow (uncharged)", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.TOMBS_OF_AMASCUT_ENTRY_COMPLETIONS.getName(), entryKc,
                LogItemSourceInfo.TOMBS_OF_AMASCUT_COMPLETIONS.getName(), regularKc,
                LogItemSourceInfo.TOMBS_OF_AMASCUT_EXPERT_COMPLETIONS.getName(), expertKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_ToA_pet() {
        // Pet drop rate is about:
        // 0.0389% for entry KC (50s, unique chance 0.7624%)
        // 0.0604% for regular KC (150s, unique chance 1.9737%)
        // 0.2018% for expert KC (350s, unique chance 6.0534%)

        // equivalent to 1 pet
        int entryKc = 2571;
        // equivalent to 1 pet
        int regularKc = 1656;
        // equivalent to 1 pet
        int expertKc = 495;

        // on drop rate. Should be very slightly dry because of the entry KC.
        int numObtained = 3;

        // Approximating based on a binomial with success probability 1/1000, n = 3000 (expected value = 3)
        double expectedLuck = 0.423;
        double expectedDryness = 0.353;
        double tolerance = 0.025;

        // Overriding with custom unique chances
        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double entryToaUniqueChance() {
                return 0.007624;
            }

            @Override
            public double regularToaUniqueChance() {
                return 0.019737;
            }

            @Override
            public double expertToaUniqueChance() {
                return 0.060534;
            }
        };

        AbstractDrop drop = new PoissonBinomialDrop(ImmutableList.of(
                // drop chance is handled elsewhere
                new RollInfo(LogItemSourceInfo.TOMBS_OF_AMASCUT_ENTRY_COMPLETIONS, 1.0),
                new RollInfo(LogItemSourceInfo.TOMBS_OF_AMASCUT_COMPLETIONS, 1.0),
                new RollInfo(LogItemSourceInfo.TOMBS_OF_AMASCUT_EXPERT_COMPLETIONS, 1.0)
        ))
                .withConfigOption("Tumeken's guardian");

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Tumeken's guardian", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.TOMBS_OF_AMASCUT_ENTRY_COMPLETIONS.getName(), entryKc,
                LogItemSourceInfo.TOMBS_OF_AMASCUT_COMPLETIONS.getName(), regularKc,
                LogItemSourceInfo.TOMBS_OF_AMASCUT_EXPERT_COMPLETIONS.getName(), expertKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_Nightmare_uniques() {
        // Inquisitor's Mace drop rate is about:
        // 1/1200 / 2 * 1.05 for Nightmare (teams of 2, always MVPs for testing purposes)
        // 1/2000 for Phosani's Nightmare

        // equivalent to 1 drop
        int regularKc = 2286;
        // equivalent to 1 drop
        int phosaniKc = 2000;

        // on drop rate.
        int numObtained = 2;

        // Approximating based on a binomial with success probability 1/1000, n = 2000 (expected value = 2)
        double expectedLuck = 0.406;
        double expectedDryness = 0.323;
        double tolerance = 0.006;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double avgNightmareTeamSize() {
                return 2;
            }

            @Override
            public double avgNightmareRewardsFraction() {
                return 0.5 * 1.05;
            }
        };

        AbstractDrop drop = new PoissonBinomialDrop(ImmutableList.of(
                new RollInfo(LogItemSourceInfo.NIGHTMARE_KILLS, 1.0 / 1200),
                new RollInfo(LogItemSourceInfo.PHOSANIS_NIGHTMARE_KILLS, 1.0 / 2000)
        ))
                .withConfigOption(CollectionLogConfig.AVG_NIGHTMARE_TEAM_SIZE_KEY)
                .withConfigOption(CollectionLogConfig.AVG_NIGHTMARE_REWARDS_FRACTION_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.NIGHTMARE_KILLS.getName(), regularKc,
                LogItemSourceInfo.PHOSANIS_NIGHTMARE_KILLS.getName(), phosaniKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_Nightmare_pet() {
        // Pet drop rate is about:
        // 1/4000 for Nightmare (teams of 5)
        // 1/1400 for Phosani's Nightmare

        // equivalent to 1 pet
        int regularKc = 4000;
        // equivalent to 1 pet
        int phosaniKc = 1400;

        // on drop rate.
        int numObtained = 2;

        // Approximating based on a binomial with success probability 1/1000, n = 2000 (expected value = 2)
        double expectedLuck = 0.406;
        double expectedDryness = 0.323;
        double tolerance = 0.01;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double avgNightmareTeamSize() {
                return 5;
            }

            @Override
            // This is purposely set way too high to check that the pet drop is not affected by contribution
            public double avgNightmareRewardsFraction() {
                return 0.5;
            }
        };

        AbstractDrop drop = new PoissonBinomialDrop(ImmutableList.of(
                        new RollInfo(LogItemSourceInfo.NIGHTMARE_KILLS, 1.0 / 800),
                        new RollInfo(LogItemSourceInfo.PHOSANIS_NIGHTMARE_KILLS, 1.0 / 1400)
                ))
                        .withConfigOption("Little nightmare");

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.NIGHTMARE_KILLS.getName(), regularKc,
                LogItemSourceInfo.PHOSANIS_NIGHTMARE_KILLS.getName(), phosaniKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_Nightmare_jar() {
        // drop rate is about:
        // 1/1950 for Nightmare (teams of 2, MVPs half the time)
        // 1/4000 for Phosani's Nightmare

        // equivalent to 1 drop
        int regularKc = 1950;
        // equivalent to 1 drop
        int phosaniKc = 4000;

        // on drop rate.
        int numObtained = 2;

        // Approximating based on a binomial with success probability 1/1000, n = 2000 (expected value = 2)
        double expectedLuck = 0.406;
        double expectedDryness = 0.323;
        // This tolerance successfully distinguishes between 1900, 1950, and 2000 regular KC to make sure the
        // MVP contribution is counted correctly
        double tolerance = 0.006;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double avgNightmareTeamSize() {
                return 2;
            }

            @Override
            // MVP half the time
            public double avgNightmareRewardsFraction() {
                return 0.5 * (1 + 0.5 * 1.05);
            }
        };

        AbstractDrop drop = new PoissonBinomialDrop(ImmutableList.of(
                new RollInfo(LogItemSourceInfo.NIGHTMARE_KILLS, 1.0 / 2000),
                new RollInfo(LogItemSourceInfo.PHOSANIS_NIGHTMARE_KILLS, 1.0 / 4000)
        ))
                .withConfigOption("Jar of dreams");

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.NIGHTMARE_KILLS.getName(), regularKc,
                LogItemSourceInfo.PHOSANIS_NIGHTMARE_KILLS.getName(), phosaniKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_Nex_uniques() {
        // Nihil horn drop rate is about:
        // 1 / 258 / 4 * 1.1 for Nex (teams of 4, always MVPs for testing purposes)

        // equivalent to 1 drop
        int kc = 1234;

        // on drop rate.
        int numObtained = 1;

        // Calculated using online binomial calculator
        double expectedLuck = 0.268;
        double expectedDryness = 0.379;
        double tolerance = 0.001;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double avgNexRewardsFraction() {
                return 0.25 * 1.1;
            }
        };

        AbstractDrop drop = new BinomialDrop(new RollInfo(LogItemSourceInfo.NEX_KILLS, 1.0 / 258))
                .withConfigOption(CollectionLogConfig.AVG_NEX_REWARDS_FRACTION_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(LogItemSourceInfo.NEX_KILLS.getName(), kc);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void calculateLuck_Wintertodt_dragon_axe() {
        // This is a good test of combining config options with multiple drop sources where only some of the drop sources
        // are relevant to that config option

        // drop rate is about:
        // 1/128 for Dag kings
        // 1/10000 for WT crates.

        // equivalent to 2 drops
        int primeKc = 128 * 2;
        // equivalent to 3 drops
        int rexKc = 128 * 3;
        // equivalent to 4 drops
        int supremeKc = 128 * 4;
        // equivalent to 1 drop (at 2.5 rolls per crate)
        int wintertodtKc = 4000;

        // on drop rate.
        int numObtained = 1 + 2 + 3 + 4;

        // Approximating based on a binomial with success probability 1/1000, n = 10000 (expected value = 10)
        double expectedLuck = 0.458;
        double expectedDryness = 0.417;
        double tolerance = 0.002;

        CollectionLogConfig config = new CollectionLogConfig() {
            @Override
            public double numRollsPerWintertodtCrate() {
                return 2.5;
            }
        };

        AbstractDrop drop = new PoissonBinomialDrop(ImmutableList.of(
                new RollInfo(LogItemSourceInfo.DAGANNOTH_PRIME_KILLS, 1.0 / 128),
                new RollInfo(LogItemSourceInfo.DAGANNOTH_REX_KILLS, 1.0 / 128),
                new RollInfo(LogItemSourceInfo.DAGANNOTH_SUPREME_KILLS, 1.0 / 128),
                new RollInfo(LogItemSourceInfo.WINTERTODT_KILLS, 1.0 / 10000)
        ))
                .withConfigOption(CollectionLogConfig.NUM_ROLLS_PER_WINTERTODT_CRATE_KEY);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.DAGANNOTH_PRIME_KILLS.getName(), primeKc,
                LogItemSourceInfo.DAGANNOTH_REX_KILLS.getName(), rexKc,
                LogItemSourceInfo.DAGANNOTH_SUPREME_KILLS.getName(), supremeKc,
                LogItemSourceInfo.WINTERTODT_KILLS.getName(), wintertodtKc);
        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKcs(kcs);

        String incalculableReason = drop.getIncalculableReason(mockItem, config);
        assertNull(incalculableReason);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, config);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, config);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

}