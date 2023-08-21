package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;

public interface DropProbabilityDistribution {

    // Return the percent chance of having received fewer drops in the same KC than the player has. In other words,
    // return the percent of players that would be drier than this player by this point.
    default double calculateLuck(CollectionLogItem item, CollectionLog collectionLog) {
        // TODO: return special value if numObtained is >= max number tracked by the collection log, per drop source?
        return -1;
    }

    // Return the percent chance of having received more drops in the same KC than the player has. In other words,
    // return the percent of players that would be luckier than this player by this point.
    default double calculateDryness(CollectionLogItem item, CollectionLog collectionLog) {
        return -1;
    }

    default String getKillCountDescription(CollectionLog collectionLog) {
        return "UNIMPLEMENTED";
    };

    // If this probability distribution cannot be calculated, return the reason why, otherwise return null.
    default String getIncalculableReason() {
        return null;
    }


}
