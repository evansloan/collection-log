package com.evansloan.collectionlog.luck.drop;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.evansloan.collectionlog.CollectionLogKillCount;
import com.evansloan.collectionlog.luck.LogItemSourceInfo;
import com.evansloan.collectionlog.luck.RollInfo;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Describes the probability distribution for a drop
public abstract class AbstractDrop implements DropLuck {

    protected final List<RollInfo> rollInfos;

    protected List<String> configOptions;

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

        this.configOptions = new ArrayList<>();
    }

    /**
     * Any subclass may make minor modifications to its calculations by using a plugin configuration setting.
     * This helps correct inflated KC for various reasons, correct inflated # items received, etc.
     */
    public AbstractDrop withConfigOption(String configOption) {
        this.configOptions.add(configOption);
        return this;
    }

    @Override
    public String getIncalculableReason(CollectionLogItem item, CollectionLogConfig config) {
        // This drop needs custom behavior defined in client configs, but these are only available client-side.
        if (config == null && !configOptions.isEmpty()) {
            return "Luck calculation for this drop is only available for your own character.";
        }
        if (configOptions.contains(CollectionLogConfig.BARROWS_BOLT_RACKS_ENABLED_KEY)
                && !config.barrowsBoltRacksEnabled()
        ) {
            return "Barrows bolt racks are disabled in the config settings.";
        }
        return null;
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
        int numTrials = rollInfos.stream()
                .map(rollInfos -> new Pair<>(
                        collectionLog.searchForKillCount(rollInfos.getDropSource().getName()),
                        rollInfos.getRollsPerKc()))
                // filter out nulls just in case
                .filter(pair -> pair.getKey() != null && pair.getValue() != null)
                .mapToInt(pair -> pair.getKey().getAmount() * pair.getValue())
                .sum();

        if (configOptions.contains(CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY)) {
            numTrials -= config.numInvalidBarrowsKc() * rollInfos.get(0).getRollsPerKc();
        }

        return numTrials;
    }

    protected int getNumSuccesses(CollectionLogItem item, CollectionLog collectionLog, CollectionLogConfig config) {
        int numSuccesses = item.getQuantity();

        // Note: The Abyssal Lantern is now purchasable from the shop, and it triggers the collection log unlock, but
        // purchased lanterns shouldn't count towards the luck of lanterns received from the Rewards Guardian
        if (configOptions.contains(CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY)) {
            numSuccesses -= config.numAbyssalLanternsPurchased();
        }

        return numSuccesses;
    }

    // the max number of successes that a player could have and still be considered "in the same boat" as you, luck-wise
    // In the vast majority of cases, this is equal to getNumSuccesses.
    protected int getMaxEquivalentNumSuccesses(CollectionLogItem item, CollectionLog collectionLog, CollectionLogConfig config) {
        return getNumSuccesses(item, collectionLog, config);
    }

    protected double getDropChance(RollInfo rollInfo, CollectionLogConfig config) {
        double dropChance = rollInfo.getDropChancePerRoll();

        if (rollInfo.getDropSource().equals(LogItemSourceInfo.CHAMBERS_OF_XERIC_COMPLETIONS)
            && configOptions.contains(CollectionLogConfig.AVG_PERSONAL_COX_POINTS_KEY)) {
            dropChance *= getCoxUniqueChanceFromPoints(config.avgPersonalCoxPoints());
        } else if (rollInfo.getDropSource().equals(LogItemSourceInfo.CHAMBERS_OF_XERIC_CM_COMPLETIONS)
            && configOptions.contains(CollectionLogConfig.AVG_PERSONAL_COX_CM_POINTS_KEY)) {
            dropChance *= getCoxUniqueChanceFromPoints(config.avgPersonalCoxCmPoints());
        }

        return dropChance;
    }

    private double getCoxUniqueChanceFromPoints(int points) {
        // max point cap
        int effectivePoints = Math.min(570_000, points);
        return effectivePoints / 867_600.0;
    }

}
