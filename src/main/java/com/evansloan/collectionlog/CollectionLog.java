package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CollectionLog
{
    private final String username;

    @Setter
    private int totalObtained;

    @Setter
    private int totalItems;

    @Setter
    private int uniqueObtained;

    @Setter
    private int uniqueItems;

    private final Map<String, CollectionLogTab> tabs;

    public CollectionLogPage searchForPage(String pageName)
    {
        if (StringUtils.isEmpty(pageName))
        {
            return null;
        }

        for (CollectionLogTab tab : tabs.values())
        {
            for (CollectionLogPage page : tab.getPages().values())
            {
                if (pageName.equalsIgnoreCase(page.getName()))
                {
                    return page;
                }
            }
        }
        return null;
    }

    public CollectionLogPage randomPage()
    {
        int pageCount = 0;
        for (CollectionLogTab tab : tabs.values())
        {
            pageCount += tab.getPages().size();
        }

        int randomIndex = (int) (Math.random() * pageCount);
        int index = 0;
        for (CollectionLogTab tab : tabs.values())
        {
            for (CollectionLogPage page : tab.getPages().values())
            {
                if (index == randomIndex)
                {
                    return page;
                }
                index++;
            }
        }
        return null;
    }

    public CollectionLog merge(CollectionLog collectionLogToMerge)
    {
        Map<String, CollectionLogTab> mergedTabs = new HashMap<>();
        for (Map.Entry<String, CollectionLogTab> tabEntry : tabs.entrySet())
        {
            String tabName = tabEntry.getKey();
            CollectionLogTab tab = tabEntry.getValue();
            Map<String, CollectionLogPage> mergedPages = new HashMap<>();
            for (Map.Entry<String, CollectionLogPage> pageEntry : tab.getPages().entrySet())
            {
                String pageName = pageEntry.getKey();
                CollectionLogPage page = pageEntry.getValue();
                List<CollectionLogItem> pageItems = page.getItems();
                List<CollectionLogKillCount> pageKillCounts = page.getKillCounts();
                CollectionLogPage pageToMerge = collectionLogToMerge.searchForPage(pageName);
                boolean pageIsUpdated = pageToMerge != null;

                List<CollectionLogItem> mergedItems = new ArrayList<>();
                for (int i = 0; i < pageItems.size(); i++)
                {
                    CollectionLogItem item = pageItems.get(i);
                    try
                    {
                        mergedItems.add(item.merge(pageToMerge.getItems().get(i)));
                    }
                    catch (IndexOutOfBoundsException | NullPointerException e)
                    {
                        pageIsUpdated = false;
                        mergedItems.add(item);
                    }
                }

                List<CollectionLogKillCount> mergedKillCounts = new ArrayList<>();
                for (int i = 0; i < pageKillCounts.size(); i++)
                {
                    CollectionLogKillCount killCount = pageKillCounts.get(i);
                    try
                    {
                        mergedKillCounts.add(killCount.merge(pageToMerge.getKillCounts().get(i)));
                    }
                    catch (IndexOutOfBoundsException | NullPointerException e)
                    {
                        pageIsUpdated = false;
                        mergedKillCounts.add(killCount);
                    }
                }

                CollectionLogPage mergedPage = new CollectionLogPage(pageName, mergedItems, mergedKillCounts);
                mergedPage.setUpdated(pageIsUpdated);
                mergedPages.put(pageName, mergedPage);
            }

            CollectionLogTab mergedTab = new CollectionLogTab(tabName, mergedPages);
            mergedTabs.put(tabName, mergedTab);
        }

        return new CollectionLog(
            username,
            totalObtained,
            totalItems,
            uniqueObtained,
            uniqueItems,
            mergedTabs
        );
    }
}