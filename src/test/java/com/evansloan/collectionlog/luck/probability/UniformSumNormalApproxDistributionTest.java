package com.evansloan.collectionlog.luck.probability;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UniformSumNormalApproxDistributionTest {

    // Test the entire approximate distribution is within a small error of the normal distribution
    private void test_cumulativeProbability_approximatesNormalDistribution(int n, double tolerance) {
        UniformSumNormalApproxDistribution dist = new UniformSumNormalApproxDistribution(n);

        double mean = n / 2.0;
        double stdDev = Math.sqrt(1.0/12.0 * n);
        NormalDistribution normalDistribution = new NormalDistribution(mean, stdDev);

        for (double x = 0; x <= n; x += n / 128.0) {
            double expectedP = normalDistribution.cumulativeProbability(x);

            double actualP = dist.cumulativeProbability(x);

            assertEquals("Distribution did not match normal distribution "
                            + ", n: " + n,
                    expectedP, actualP, tolerance);
        }
    }

    @Test
    public void cumulativeProbability_EqualsNormalDistributionForHighN() {
        List<Integer> nList = ImmutableList.of(12, 13, 14, 20, 100, 500, 4000, 30_000, 100_000, 1_000_000);
        double tolerance = 0.0000001;

        nList.forEach(n -> {
            test_cumulativeProbability_approximatesNormalDistribution(n, tolerance);
        });
    }

    @Test
    public void cumulativeProbability_isApproximatedByNormalDistributionAsNIncreases() {
        // Uniform distribution is far from normal distribution
        test_cumulativeProbability_approximatesNormalDistribution(1, 0.06);
        // Triangular distribution is a bit closer to Normal
        test_cumulativeProbability_approximatesNormalDistribution(2, 0.02);
        // The tolerance continues decreasing (indicating a better and better approximation by the Normal distribution)
        test_cumulativeProbability_approximatesNormalDistribution(3, 0.014);
        test_cumulativeProbability_approximatesNormalDistribution(4, 0.01);
        test_cumulativeProbability_approximatesNormalDistribution(5, 0.007);
        test_cumulativeProbability_approximatesNormalDistribution(6, 0.005);
        test_cumulativeProbability_approximatesNormalDistribution(7, 0.0045);
        test_cumulativeProbability_approximatesNormalDistribution(8, 0.004);
        test_cumulativeProbability_approximatesNormalDistribution(9, 0.0035);
        test_cumulativeProbability_approximatesNormalDistribution(10, 0.003);
        test_cumulativeProbability_approximatesNormalDistribution(11, 0.0027);
        test_cumulativeProbability_approximatesNormalDistribution(12, 0.002);
    }

    @Test
    public void cumulativeProbability_nEquals0() {
        UniformSumNormalApproxDistribution dist = new UniformSumNormalApproxDistribution(0);

        assertEquals(0, dist.cumulativeProbability(-1), 0.0000000001);
        // There is a 100% chance of 0 rolls summing to 0.
        assertEquals(1, dist.cumulativeProbability(0), 0.0000000001);
        assertEquals(1, dist.cumulativeProbability(1), 0.0000000001);
    }

    @Test
    public void cumulativeProbability_testBoundaryHighN() {
        int n = 1234;
        UniformSumNormalApproxDistribution dist = new UniformSumNormalApproxDistribution(n);

        assertEquals(0, dist.cumulativeProbability(-1), 0.0000000001);
        assertEquals(0, dist.cumulativeProbability(0), 0);
        // CDF equals 0.5 at the mean
        assertEquals(0.5, dist.cumulativeProbability(n/2.0), 0);
        assertEquals(1, dist.cumulativeProbability(n), 0.0000000001);
    }

    @Test
    public void cumulativeProbability_testBoundaryLowN() {
        int n = 10;
        UniformSumNormalApproxDistribution dist = new UniformSumNormalApproxDistribution(n);

        assertEquals(0, dist.cumulativeProbability(-1), 0.0000000001);
        assertEquals(0, dist.cumulativeProbability(0), 0);
        // CDF equals 0.5 at the mean
        assertEquals(0.5, dist.cumulativeProbability(n/2.0), 0);
        assertEquals(1, dist.cumulativeProbability(n), 0.0000000001);
    }

}