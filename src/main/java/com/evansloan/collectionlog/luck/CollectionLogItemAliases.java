package com.evansloan.collectionlog.luck;

import jdk.internal.net.http.common.Log;

// TODO: Consider storing this mapping on GitHub so new items in future updates can be added without a plugin update.
public final class CollectionLogItemAliases
{

    // TODO: add any item aliases here.
    public static String aliasItemName(String itemName)
    {
        switch (itemName.toLowerCase())
        {
            // pets
            case "abyssal sire pet":
                return LogItemInfo.ABYSSAL_ORPHAN_13262.getItemName();

            case "enhanced weapon seed":
                return LogItemInfo.ENHANCED_CRYSTAL_WEAPON_SEED_25859.getItemName();

            case "enhanced teleport seed":
                return LogItemInfo.ENHANCED_CRYSTAL_TELEPORT_SEED_23959.getItemName();

            default:
                return itemName;
        }
    }

}
