package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.CollectionLog;
import com.evansloan.collectionlog.CollectionLogConfig;
import com.evansloan.collectionlog.CollectionLogItem;
import com.google.common.collect.ImmutableList;

import java.util.List;

// This class is identical to BinomialDrop, except it may make minor modifications specified by a field
// plugin configuration. This helps correct inflated KC for various reasons, correct inflated # items received, etc.
// Unfortunately, configs are client-side only, so this class also needs a default behavior when looking up another
// player's luck and the corresponding correction is unavailable.
public class ConfiguredBinomialDrop extends BinomialDrop {

    private final String configOptionName;

    public ConfiguredBinomialDrop(List<RollInfo> rollInfos, String configOptionName) {
        super(rollInfos);
        this.configOptionName = configOptionName;
    }

    public ConfiguredBinomialDrop(RollInfo rollInfo, String configOptionName) {
        this(ImmutableList.of(rollInfo), configOptionName);
    }

    @Override
    public String getIncalculableReason(CollectionLogItem item, CollectionLogConfig config) {
        if (config == null) {
            return "Luck calculation for this drop is only available for your own character.";
        }
        return null;
    }

    @Override
    protected int getNumSuccesses(CollectionLogItem item, CollectionLog collectionLog, CollectionLogConfig config) {
        int modifier = 0;
        // Note: The Abyssal Lantern is now purchasable from the shop, and it triggers the collection log unlock, but
        // purchased lanterns shouldn't count towards the luck of lanterns received from the Rewards Guardian
        if (configOptionName.equals(CollectionLogConfig.NUM_ABYSSAL_LANTERNS_PURCHASED_KEY)) {
            modifier = -config.numAbyssalLanternsPurchased();
        }
        return super.getNumSuccesses(item, collectionLog, config) + modifier;
    }

    @Override
    protected int getNumTrials(CollectionLog collectionLog, CollectionLogConfig config) {
        int modifier = 0;
        if (configOptionName.equals(CollectionLogConfig.NUM_INVALID_BARROWS_KC_KEY)) {
            modifier = -config.numInvalidBarrowsKc() * rollInfos.get(0).getRollsPerKc();
        }
        return super.getNumTrials(collectionLog, config) + modifier;
    }

}
