package com.evansloan.collectionlog.luck.probability;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PoissonBinomialDistributionTest {

    @Test
    public void probability_oneCoinFlip() {
        List<Double> probabilities = ImmutableList.of(0.5);

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(0.5, dist.probability(0), 0.00001);
        assertEquals(0.5, dist.probability(1), 0.00001);
    }

    @Test
    public void probability_twoCoinFlips() {
        List<Double> probabilities = ImmutableList.of(0.5, 0.5);

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        // two equal coin flips have a 50% chance of having 1 success, and 25% of 0 or 2 heads
        assertEquals(0.25, dist.probability(0), 0.00001);
        assertEquals(0.5, dist.probability(1), 0.00001);
        assertEquals(0.25, dist.probability(2), 0.00001);
    }

    @Test
    public void probability_zeroCoinFlip() {
        List<Double> probabilities = ImmutableList.of();

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        // The probability of 0 coins summing to 0 is 1
        assertEquals(1, dist.probability(0), 0.00001);
    }

    @Test
    public void probability_outOfRangeReturns0() {
        List<Double> probabilities = ImmutableList.of(0.5, 0.5);

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(0, dist.probability(3), 0.00001);
    }

    @Test
    public void probability_manyKc_handlesHighPrecision() {
        // This test only passes using BigDecimal but fails using Double.
        List<Double> probabilities = new ArrayList<>(Collections.nCopies(1000, 0.5));

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(0.025225, dist.probability(500), 0.000001);
        assertEquals( 9.33264E-302, dist.probability(1000), 1e-307);
    }

    @Test
    public void probability_manyKc_handlesHighPrecisionForRareItems() {
        // For example, 1K master clues calculating the 3rd age pickaxe rate
        List<Double> probabilities = new ArrayList<>(Collections.nCopies(10000, 1.0/313168));

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(0.03, dist.probability(1), 0.01);
    }

    @Test
    public void probability_manyKc_handlesHighPrecisionForVeryCommonItems() {
        // This test only passes using BigDecimal but fails using Double.
        List<Double> probabilities = new ArrayList<>(Collections.nCopies(1000, 0.99));

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(0.000043, dist.probability(1000), 0.000001);
    }

    @Test
    public void probability_onlyComputesNecessaryProbabilities() {
        List<Double> probabilities = new ArrayList<>(Collections.nCopies(1000000, 0.00001));

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        // This test would never finish if the class didn't properly shortcut, limiting to only 5 probabilities
        assertEquals(0.037832, dist.probability(5), 0.000001);
    }

    @Test
    public void probability_cachesPmf() {
        List<Double> probabilities = new ArrayList<>(Collections.nCopies(100000, 0.00001));

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(0.003065493, dist.probability(5), 0.000000001);
        // This test would REALLY never finish if the class didn't cache properly
        for (int i = 0; i < 100000; i++) {
            assertEquals(0.003065493, dist.probability(5), 0.000000001);
        }
    }

    @Test
    public void cumulativeProbability_equalToBinomialForEqualProbabilities() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropChance));

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(expectedLuck, dist.cumulativeProbability(numObtained - 1), tolerance);

        assertEquals(expectedDryness, 1 - dist.cumulativeProbability(numObtained), tolerance);
    }

    @Test
    public void cumulativeProbability_indifferentToTinyExtraProbability() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.00001;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropChance));
        // This probability shouldn't matter enough to change the answer, wherever it's added into the list
        probabilities.add(0.00000001);
        probabilities.addAll(0, ImmutableList.of(0.00000001));
        probabilities.addAll(kc / 2, ImmutableList.of(0.00000001));

        PoissonBinomialDistribution dist = new PoissonBinomialDistribution(probabilities);

        assertEquals(expectedLuck, dist.cumulativeProbability(numObtained - 1), tolerance);

        assertEquals(expectedDryness, 1 - dist.cumulativeProbability(numObtained), tolerance);
    }
}