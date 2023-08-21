package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import org.junit.Test;

import static org.junit.Assert.*;

public class FixedStackDropTest {

    @Test
    public void testFixedStackBinomial_singleDropSource() {
        double dropChancePerStack = 0.01;
        int stackSize = 3;
        int kc = 350;
        int numObtained = 9;
        double expectedLuck = 0.31945; // 6 or fewer total items
        double expectedDryness = 0.46391; // 9 or more total items
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        FixedStackDrop drop = new FixedStackDrop(LogItemSourceInfo.CERBERUS_KILLS, dropChancePerStack, stackSize);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Key master teleport", numObtained, true, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.CERBERUS_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testFixedStackBinomial_singleDropSource_dry() {
        double dropChancePerStack = 0.01;
        int stackSize = 3;
        int kc = 350;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0.97033; // 3 or more total items
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        FixedStackDrop drop = new FixedStackDrop(LogItemSourceInfo.CERBERUS_KILLS, dropChancePerStack, stackSize);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "Key master teleport", numObtained, false, 0);

        CollectionLog mockCollectionLog = CollectionLogLuckTestUtils.getMockCollectionLogWithKc(
                LogItemSourceInfo.CERBERUS_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

}