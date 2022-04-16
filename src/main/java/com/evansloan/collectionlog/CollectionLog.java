package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;

@AllArgsConstructor
public class CollectionLog
{
    @Getter
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
                if (pageName.equals(page.getName()))
                {
                    return page;
                }
            }
        }
        return null;
    }
}
