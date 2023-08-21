package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Map;

// This is identical to BinomialDrop, but for activities that give multiple rolls per KC for some single-source item.
// For example, Zulrah gives 2 rolls per kill, or clues give multiple rolls per "KC".
// Note: This class supports multiple item sources, like BinomialDrop, but requires the drop chance for the item to be
// the same across all item sources.
public class MultiRollBinomialDrop extends BinomialDrop {

    private final Map<LogItemSourceInfo, Integer> rollsPerKc;

    public MultiRollBinomialDrop(LogItemSourceInfo logItemSourceInfo, int rollsPerKc, double dropChancePerRoll) {
        super(logItemSourceInfo, dropChancePerRoll);
        this.rollsPerKc = ImmutableMap.of(logItemSourceInfo, rollsPerKc);
    }

    public MultiRollBinomialDrop(Map<LogItemSourceInfo, Integer> rollsPerKc, double dropChancePerRoll) {
        super(new ArrayList<>(rollsPerKc.keySet()), dropChancePerRoll);
        this.rollsPerKc = rollsPerKc;
    }

    @Override
    protected int getNumTrials(CollectionLog collectionLog) {
        return logSourceDropRates.keySet().stream()
                .map(sourceInfo -> new Pair<>(
                        collectionLog.searchForKillCount(sourceInfo.getName()),
                        rollsPerKc.get(sourceInfo)))
                // filter out nulls just in case
                .filter(pair -> pair.getKey() != null && pair.getValue() != null)
                .mapToInt(pair -> pair.getKey().getAmount() * pair.getValue())
                .sum();
    }
}
