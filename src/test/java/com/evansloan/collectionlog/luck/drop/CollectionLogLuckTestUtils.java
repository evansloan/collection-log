package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.*;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionLogLuckTestUtils {
    public static CollectionLog getMockCollectionLogWithKcs(Map<String, Integer> sourceToKcMap) {
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

    public static CollectionLog getMockCollectionLogWithKc(String itemSourceName, int kc) {
        return getMockCollectionLogWithKcs(ImmutableMap.of(itemSourceName, kc));
    }
}
