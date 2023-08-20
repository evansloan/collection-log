package com.evansloan.collectionlog.luck.probability;

// Base class for all unsupported or unimplemented drops
public abstract class AbstractUnsupportedDrop extends AbstractDropProbabilityDistribution {

    private String reason;

    public AbstractUnsupportedDrop(String reason) {
        this.reason = reason;
    }

    @Override
    public String getIncalculableReason() {
        return reason;
    }

}
