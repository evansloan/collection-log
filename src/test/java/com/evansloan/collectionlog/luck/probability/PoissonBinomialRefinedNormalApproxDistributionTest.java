package com.evansloan.collectionlog.luck.probability;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class PoissonBinomialRefinedNormalApproxDistributionTest {

    @Test
    public void cumulativeProbability_outOfRange() {
        List<Double> probabilities = ImmutableList.of(0.5, 0.5);

        PoissonBinomialRefinedNormalApproxDistribution dist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);

        assertEquals(0, dist.cumulativeProbability(-1), 0.0000000001);
        assertEquals(1, dist.cumulativeProbability(3), 0.0000000001);
    }

    @Test
    public void cumulativeProbability_equalToBinomialForEqualProbabilities() {
        double dropChance = 0.01;
        int kc = 100;
        int numObtained = 1;
        double expectedLuck = 0.36603;
        double expectedDryness = 0.26424;
        // expected probabilities calculated online, with the following sig digits
        double tolerance = 0.02;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropChance));

        PoissonBinomialRefinedNormalApproxDistribution dist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);

        // probability x < X
        assertEquals(expectedLuck, dist.cumulativeProbability(numObtained - 1), tolerance);

        // probability x > X
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
        double tolerance = 0.02;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropChance));
        // This cumulativeProbability shouldn't matter enough to change the answer, wherever it's added into the list
        probabilities.add(0.00000001);
        probabilities.addAll(0, ImmutableList.of(0.00000001));
        probabilities.addAll(kc / 2, ImmutableList.of(0.00000001));

        PoissonBinomialRefinedNormalApproxDistribution dist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);

        assertEquals(expectedLuck, dist.cumulativeProbability(numObtained - 1), tolerance);

        assertEquals(expectedDryness, 1 - dist.cumulativeProbability(numObtained), tolerance);
    }

    @Test
    public void cumulativeProbability_compareToExact_manyEqualProbabilities() {
        List<Double> probabilities = new ArrayList<>(Collections.nCopies(1000, 0.5));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // establish a baseline by computing with the exact distribution
        assertEquals(0, exactDist.cumulativeProbability(0), 0.000001);
        assertEquals(0, exactDist.cumulativeProbability(200), 0.000001);
        assertEquals(0.000865, exactDist.cumulativeProbability(450), 0.000001);
        assertEquals(0.108724, exactDist.cumulativeProbability(480), 0.000001);
        assertEquals(0.512612, exactDist.cumulativeProbability(500), 0.000001);
        assertEquals(0.902616, exactDist.cumulativeProbability(520), 0.000001);
        assertEquals(0.999304, exactDist.cumulativeProbability(550), 0.000001);
        assertEquals(1, exactDist.cumulativeProbability(800), 0.000001);
        assertEquals(1, exactDist.cumulativeProbability(1000), 0.000001);

        // It seems that, at worst, the approximate solution is about 0.01% away from the "exact" solution in this case.
        assertEquals(exactDist.cumulativeProbability(0), approxDist.cumulativeProbability(0), 0.000001);
        assertEquals(exactDist.cumulativeProbability(200), approxDist.cumulativeProbability(200), 0.000001);
        assertEquals(exactDist.cumulativeProbability(450), approxDist.cumulativeProbability(450), 0.0001);
        assertEquals(exactDist.cumulativeProbability(480), approxDist.cumulativeProbability(480), 0.00001);
        assertEquals(exactDist.cumulativeProbability(500), approxDist.cumulativeProbability(500), 0.00001);
        assertEquals(exactDist.cumulativeProbability(520), approxDist.cumulativeProbability(520), 0.0001);
        assertEquals(exactDist.cumulativeProbability(550), approxDist.cumulativeProbability(550), 0.00001);
        assertEquals(exactDist.cumulativeProbability(800), approxDist.cumulativeProbability(800), 0.000001);
        assertEquals(exactDist.cumulativeProbability(1000), approxDist.cumulativeProbability(1000), 0.000001);
    }

    @Test
    public void cumulativeProbability_compareToExact_lowKc_lowProbability() {
        int kc = 50;
        double dropRate = 0.000001;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes >= 0; successes -= 1) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.0001);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_midKc_lowProbability() {
        int kc = 1000;
        double dropRate = 0.000001;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes >= 0; successes -= 5) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.001);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_highKc_lowProbability() {
        int kc = 1_000_000;
        double dropRate = 0.000_001;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        // Exact Poisson is too slow - could use Binomial instead.
        BinomialDistribution exactDist = new BinomialDistribution(kc, dropRate);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes /= 2) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.003);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_lowKc_midProbability() {
        int kc = 50;
        double dropRate = 0.01;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes -= 1) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.03);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_midKc_midProbability() {
        int kc = 1000;
        double dropRate = 0.01;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes /= 2) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.003);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_highKc_midProbability() {
        int kc = 1_000_000;
        double dropRate = 0.01;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        // Exact Poisson is too slow - could use Binomial instead.
        BinomialDistribution exactDist = new BinomialDistribution(kc, dropRate);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes /= 2) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.003);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_lowKc_highProbability() {
        int kc = 50;
        double dropRate = 0.5;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes -= 1) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.03);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_midKc_highProbability() {
        int kc = 1000;
        double dropRate = 0.5;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes /= 2) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.003);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_highKc_highProbability() {
        // BinomialDistribution has numerical instability around 1M kc and high dropRate...
        int kc = 100_000;
        double dropRate = 0.5;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, dropRate));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        // Exact Poisson is too slow - could use Binomial instead.
        BinomialDistribution exactDist = new BinomialDistribution(kc, dropRate);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes -= kc/1000) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.001);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_lowKc_randomProbability() {
        // To make test deterministic
        Random r = new Random(1234);
        int kc = 50;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, r.nextDouble()));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes -= 1) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.001);
        }
    }

    @Test
    public void cumulativeProbability_compareToExact_midKc_randomProbability() {
        // To make test deterministic
        Random r = new Random(2345);
        int kc = 1000;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, r.nextDouble()));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes -= kc / 100) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.003);
        }
    }

    @Test
//    @Ignore("Expensive - manual run only.")
    public void cumulativeProbability_compareToExact_highKc_randomProbability() {
        // To make test deterministic
        Random r = new Random(2345);
        int kc = 10000;

        List<Double> probabilities = new ArrayList<>(Collections.nCopies(kc, r.nextDouble()));

        PoissonBinomialRefinedNormalApproxDistribution approxDist = new PoissonBinomialRefinedNormalApproxDistribution(probabilities);
        PoissonBinomialDistribution exactDist = new PoissonBinomialDistribution(probabilities);

        // Test the entire approximate distribution is within a small error of the exact distribution
        for (int successes = kc; successes > 0; successes -= kc / 100) {
            assertEquals(exactDist.cumulativeProbability(successes), approxDist.cumulativeProbability(successes), 0.003);
        }
    }

}