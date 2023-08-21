package com.evansloan.collectionlog.luck.probability;

import com.evansloan.collectionlog.luck.LogItemSourceInfo;

import java.util.Map;

// A drop that follows the Poisson binomial distribution (used for drops that are obtained from multiple activities
// or bosses where the drop chances are not necessarily equal).
public class PoissonBinomialDrop extends AbstractDropProbabilityDistribution {

    public PoissonBinomialDrop(Map<LogItemSourceInfo, Double> logSourceDropRates) {
        super(logSourceDropRates);
    }

}
