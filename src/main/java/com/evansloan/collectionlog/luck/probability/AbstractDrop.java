package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.CollectionLogKillCount;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Describes the probability distribution for a drop
public abstract class AbstractDrop implements DropLuck {

    protected final Map<LogItemSourceInfo, Double> logSourceDropRates;

    public AbstractDrop(Map<LogItemSourceInfo, Double> logSourceDropRates) {
        this.logSourceDropRates = logSourceDropRates;
    }

    @Override
    public String getKillCountDescription(CollectionLog collectionLog) {
        return logSourceDropRates.keySet().stream()
                .map(s -> collectionLog.searchForKillCount(s.getName()))
                // filter out nulls just in case
                .filter(Objects::nonNull)
                // sort by kc, descending
                .sorted(Comparator.comparing(CollectionLogKillCount::getAmount).reversed())
                .map(kc -> kc.getAmount() + "x " + kc.getName())
                .collect(Collectors.joining(", "));
    }

    protected int getNumTrials(CollectionLog collectionLog) {
        return logSourceDropRates.keySet().stream()
                .map(s -> collectionLog.searchForKillCount(s.getName()))
                // filter out nulls just in case
                .filter(Objects::nonNull)
                .mapToInt(CollectionLogKillCount::getAmount)
                .sum();
    }

    protected int getNumSuccesses(CollectionLogItem item, CollectionLog collectionLog) {
        return item.getQuantity();
    }

    // the max number of successes that a player could have and still be considered "in the same boat" as you, luck-wise
    // In the vast majority of cases, this is equal to getNumSuccesses.
    protected int getMaxEquivalentNumSuccesses(CollectionLogItem item, CollectionLog collectionLog) {
        return getNumSuccesses(item, collectionLog);
    }

}
