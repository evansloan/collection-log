package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HiddenShardDropTest {

    @Test
    public void testHiddenShardBinomial_singleDropSource() {
        double dropChancePerShard = 0.01;
        int shardsRequired = 3;
        int kc = 900;
        int numObtained = 2;
        double expectedLuck = 0.11447; // 5 or fewer hidden shards
        double expectedDryness = 0.54501; // 9 or more hidden shards
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        HiddenShardDrop drop = new HiddenShardDrop(new RollInfo(LogItemSourceInfo.DUKE_SUCELLUS_KILLS, dropChancePerShard), shardsRequired);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Magus vestige", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.DUKE_SUCELLUS_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, null);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, null);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testHiddenShardBinomial_singleDropSource_dry() {
        double dropChancePerShard = 0.01;
        int shardsRequired = 3;
        int kc = 900;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0.99394; // 3 or more hidden shards
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        HiddenShardDrop drop = new HiddenShardDrop(new RollInfo(LogItemSourceInfo.DUKE_SUCELLUS_KILLS, dropChancePerShard), shardsRequired);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Magus vestige", numObtained, false, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.DUKE_SUCELLUS_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog, null);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog, null);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

}