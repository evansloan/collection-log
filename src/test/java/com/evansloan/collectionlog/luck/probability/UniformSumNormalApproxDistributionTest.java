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
    public void cumulativeProbability_usageExample_uniformDiscrete() {
        // a player rolls anywhere from 5 to 8, inclusive.
        // 5 = [0,0.25] | 6 = [0.25,0.5] | 7 = [0.5,0.75] | 8 = [0.75,1]

        UniformSumNormalApproxDistribution dist = new UniformSumNormalApproxDistribution(1);

        // Approximate a discrete distribution by using the top of the range for each possibility
        assertEquals(0.25, dist.cumulativeProbability(0.25), 0.000000001);
        assertEquals(0.5, dist.cumulativeProbability(0.5), 0.000000001);
        assertEquals(0.75, dist.cumulativeProbability(0.75), 0.000000001);
        assertEquals(1, dist.cumulativeProbability(1), 0.000000001);
    }

    // test approximating a discrete uniform distribution with a basic continuity correction
    @Test
    public void cumulativeProbability_usageExample_triangularDiscrete() {
        // a player rolls anywhere from 5 to 8, inclusive, twice.
        // 5 = [0,0.25] | 6 = [0.25,0.5] | 7 = [0.5,0.75] | 8 = [0.75,1]
        //
        // for n = 1, there are 4 possibilities. For n = 2, there are 7 possibilities
        // In general, there are n * max - n * min + 1 buckets, or n * range + 1 buckets.

        double n = 2;
        UniformSumNormalApproxDistribution dist = new UniformSumNormalApproxDistribution(2);

        // Approximate a discrete distribution by using the top of the range for each possibility
        // total = 10
        assertEquals(1.0/16, dist.cumulativeProbability(1.0/7 * n), 0.025);
        // total = 11
        assertEquals(3.0/16, dist.cumulativeProbability(2.0/7 * n), 0.03);
        // total = 12
        assertEquals(6.0/16, dist.cumulativeProbability(3.0/7 * n), 0.02);
        // total = 13
        assertEquals(10.0/16, dist.cumulativeProbability(4.0/7 * n), 0.02);
        // total = 14
        assertEquals(13.0/16, dist.cumulativeProbability(5.0/7 * n), 0.03);
        // total = 15
        assertEquals(15.0/16, dist.cumulativeProbability(6.0/7 * n), 0.03);
        // total = 16
        assertEquals(16.0/16, dist.cumulativeProbability(1 * n), 0);

        // From this, we can derive a general formula for approximating the discrete uniform sum distribution using the
        // continuous uniform sum distribution. We would use the fraction of the way from n * minRoll to n * maxRoll,
        // plus 1 because the range is inclusive, and using numReceived + 1 since it would be the top of the continuous
        // equivalent of the discrete numReceived.
        //
        // double approxDiscreteCumProb = dist.cumulativeProbability(
        //      (numReceived - minRoll * n + 1) / ((maxRoll - minRoll) * n + 1)
        // );
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