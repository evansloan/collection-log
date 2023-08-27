package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.CollectionLogKillCount;
import org.apache.commons.math3.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Describes the probability distribution for a drop
public abstract class AbstractDrop implements DropLuck {

    protected final List<RollInfo> rollInfos;

    public AbstractDrop(List<RollInfo> rollInfos) {
        this.rollInfos = rollInfos;

        if (rollInfos.isEmpty()) {
            throw new IllegalArgumentException("At least one RollInfo is required.");
        }

        // the number of rolls per source and the drop rate per source cannot both be non-uniform across all drops,
        // otherwise it's impossible to tell which (different) drop rate led to the success, and luck cannot be
        // calculated
        boolean hasNonUniformDropChances = rollInfos.stream().mapToDouble(RollInfo::getDropChancePerRoll).distinct().count() != 1;
        boolean hasNonUniformRollsPerKc = rollInfos.stream().mapToInt(RollInfo::getRollsPerKc).distinct().count() != 1;
        if (hasNonUniformDropChances && hasNonUniformRollsPerKc) {
            throw new IllegalArgumentException("Probabilities for multiple drop sources cannot be unequal while having " +
                    "a different number of rolls per drop source.");
        }
    }

    @Override
    public String getKillCountDescription(CollectionLog collectionLog) {
        return rollInfos.stream()
                .map(roll -> collectionLog.searchForKillCount(roll.getDropSource().getName()))
                // filter out nulls just in case
                .filter(Objects::nonNull)
                // sort by kc, descending
                .sorted(Comparator.comparing(CollectionLogKillCount::getAmount).reversed())
                .map(kc -> kc.getAmount() + "x " + kc.getName())
                .collect(Collectors.joining(", "));
    }

    protected int getNumTrials(CollectionLog collectionLog, CollectionLogConfig config) {
        return rollInfos.stream()
                .map(rollInfos -> new Pair<>(
                        collectionLog.searchForKillCount(rollInfos.getDropSource().getName()),
                        rollInfos.getRollsPerKc()))
                // filter out nulls just in case
                .filter(pair -> pair.getKey() != null && pair.getValue() != null)
                .mapToInt(pair -> pair.getKey().getAmount() * pair.getValue())
                .sum();
    }

    protected int getNumSuccesses(CollectionLogItem item, CollectionLog collectionLog, CollectionLogConfig config) {
        return item.getQuantity();
    }

    // the max number of successes that a player could have and still be considered "in the same boat" as you, luck-wise
    // In the vast majority of cases, this is equal to getNumSuccesses.
    protected int getMaxEquivalentNumSuccesses(CollectionLogItem item, CollectionLog collectionLog, CollectionLogConfig config) {
        return getNumSuccesses(item, collectionLog, config);
    }

}
