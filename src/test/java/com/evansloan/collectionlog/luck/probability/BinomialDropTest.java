package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.*;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BinomialDropTest {

    private CollectionLog getMockCollectionLogWithKcs(Map<String, Integer> sourceToKcMap) {
        List<CollectionLogKillCount> killCounts = sourceToKcMap.entrySet().stream()
                .map(entry -> new CollectionLogKillCount(entry.getKey(), entry.getValue(), 0))
                .collect(Collectors.toList());

        List<CollectionLogItem> pageItems = Collections.emptyList();

        CollectionLogPage mockPage = new CollectionLogPage("some page", pageItems, killCounts, true);
        Map<String, CollectionLogPage> pages = ImmutableMap.of(mockPage.getName(), mockPage);

        CollectionLogTab mockTab = new CollectionLogTab("some page", pages);
        Map<String, CollectionLogTab> tabs = ImmutableMap.of(mockTab.getName(), mockTab);

        return new CollectionLog("someusername", 0, 0, 0, 0, tabs);
    }

    private CollectionLog getMockCollectionLogWithKc(String itemSourceName, int kc) {
        return getMockCollectionLogWithKcs(ImmutableMap.of(itemSourceName, kc));
    }

    @Test
    public void testBasicBinomial() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        BinomialDrop drop = new BinomialDrop(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testBinomial_withMultipleSources() {
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
        CollectionLog mockCollectionLog = getMockCollectionLogWithKcs(kcs);

        List<LogItemSourceInfo> dropSources = ImmutableList.of(LogItemSourceInfo.ARTIO_KILLS, LogItemSourceInfo.CALLISTO_KILLS);
        BinomialDrop drop = new BinomialDrop(dropSources, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    @Test
    public void testBinomial_0Obtained() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0.63397;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        BinomialDrop drop = new BinomialDrop(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, false, 0);

        CollectionLog mockCollectionLog = getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    public void testBinomial_0Trials() {
        double dropChance = 0.5;
        int kc = 0;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 0;
        double tolerance = 0.00001;

        BinomialDrop drop = new BinomialDrop(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    public void testBinomial_spooooooooooooooooned() {
        double dropChance = 0.5;
        int kc = 100000000;
        int numObtained = 100000000;
        double expectedLuck = 1;
        double expectedDryness = 0;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.000000001;

        BinomialDrop drop = new BinomialDrop(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, true, 0);

        CollectionLog mockCollectionLog = getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        double actualLuck = drop.calculateLuck(mockItem, mockCollectionLog);
        assertEquals(expectedLuck, actualLuck, tolerance);

        double actualDryness = drop.calculateDryness(mockItem, mockCollectionLog);
        assertEquals(expectedDryness, actualDryness, tolerance);
    }

    public void testBinomial_dryyyyyyyyyyyyyyyyyyyyy() {
        double dropChance = 0.5;
        int kc = 100000;
        int numObtained = 0;
        double expectedLuck = 0;
        double expectedDryness = 1;
        double tolerance = 0.000000001;

        BinomialDrop drop = new BinomialDrop(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance);

        CollectionLogItem mockItem = new CollectionLogItem(1234, "some item name", numObtained, false, 0);

        CollectionLog mockCollectionLog = getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

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

        BinomialDrop drop = new BinomialDrop(LogItemSourceInfo.ABYSSAL_SIRE_KILLS, dropChance);

        CollectionLog mockCollectionLog = getMockCollectionLogWithKc(LogItemSourceInfo.ABYSSAL_SIRE_KILLS.getName(), kc);

        String actualKcString = drop.getKillCountDescription(mockCollectionLog);
        assertEquals(expectedKcString, actualKcString);
    }

    @Test
    public void testKcString_multipleDropSources() {
        double dropChance = 0.01;
        int kc1 = 100;
        int kc2 = 200;
        int kc3 = 150;
        String expectedKcString = "200x Calvar'ion kills, 150x Spindel kills, 100x Artio kills";

        List<LogItemSourceInfo> dropSources = ImmutableList.of(
                LogItemSourceInfo.ARTIO_KILLS,
                LogItemSourceInfo.CALVARION_KILLS,
                LogItemSourceInfo.SPINDEL_KILLS);
        Map<String, Integer> kcs = ImmutableMap.of(
                LogItemSourceInfo.ARTIO_KILLS.getName(), kc1,
                LogItemSourceInfo.CALVARION_KILLS.getName(), kc2,
                LogItemSourceInfo.SPINDEL_KILLS.getName(), kc3
        );

        BinomialDrop drop = new BinomialDrop(dropSources, dropChance);

        CollectionLog mockCollectionLog = getMockCollectionLogWithKcs(kcs);

        String actualKcString = drop.getKillCountDescription(mockCollectionLog);
        assertEquals(expectedKcString, actualKcString);
    }
}