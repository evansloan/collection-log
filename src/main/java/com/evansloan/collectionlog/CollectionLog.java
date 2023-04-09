package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
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
}