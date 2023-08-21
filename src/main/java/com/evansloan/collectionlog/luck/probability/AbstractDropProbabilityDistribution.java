package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogKillCount;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Describes the probability distribution for a drop
public abstract class AbstractDropProbabilityDistribution implements DropProbabilityDistribution {

    private final Map<LogItemSourceInfo, Double> logSourceDropRates;

    public AbstractDropProbabilityDistribution(Map<LogItemSourceInfo, Double> logSourceDropRates) {
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

    protected int getTotalDropsObtained(CollectionLog collectionLog) {
        return logSourceDropRates.keySet().stream()
                .map(s -> collectionLog.searchForKillCount(s.getName()))
                // filter out nulls just in case
                .filter(Objects::nonNull)
                .mapToInt(CollectionLogKillCount::getAmount)
                .sum();
    }

}
