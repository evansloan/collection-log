package com.evansloan.collectionlog.luck;

import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

public class CollectionLogLuckUtilsTest {

    @Test
    public void formatLuckSigDigits_roundsToTwoDigits() {
        assertEquals("50", CollectionLogLuckUtils.formatLuckSigDigits(0.499999999));
        assertEquals("50", CollectionLogLuckUtils.formatLuckSigDigits(0.5));
        assertEquals("50", CollectionLogLuckUtils.formatLuckSigDigits(0.500000001));
    }

    @Test
    public void formatLuckSigDigits_nearBoundary() {
        assertEquals("99.999999999", CollectionLogLuckUtils.formatLuckSigDigits(0.99999999999));
        // For some weird reason, powers of 100 have an extra 0 at the end. This is a small bug.
        assertEquals("99.99999999999990", CollectionLogLuckUtils.formatLuckSigDigits(0.999999999999999));

        // max 2 final digits, excluding the 9s
        assertEquals("99.99998", CollectionLogLuckUtils.formatLuckSigDigits(0.99999979999));
        assertEquals("99.9999997", CollectionLogLuckUtils.formatLuckSigDigits(0.9999999966666));

        // max 2 digits, even if luck is higher precision
        assertEquals("0.010", CollectionLogLuckUtils.formatLuckSigDigits(0.0001));
        assertEquals("0.012", CollectionLogLuckUtils.formatLuckSigDigits(0.00012));
        assertEquals("0.012", CollectionLogLuckUtils.formatLuckSigDigits(0.000123));

        // This seems to put it in scientific notation... eh... I guess that's okay. It has 2 sig digits, at least.
        assertEquals("1.2E-13", CollectionLogLuckUtils.formatLuckSigDigits(0.00000000000000123));
    }

    @Test
    public void formatLuckSigDigits_clampsIfOutOfRange() {
        assertEquals("100", CollectionLogLuckUtils.formatLuckSigDigits(1.1));
        assertEquals("100", CollectionLogLuckUtils.formatLuckSigDigits(1));
        assertEquals("0", CollectionLogLuckUtils.formatLuckSigDigits(0));
        assertEquals("0", CollectionLogLuckUtils.formatLuckSigDigits(-0.1));
    }

    @Test
    public void getOverallLuckColor_returnsGreenAtMaxLuck() {
        final Color expectedColor = Color.GREEN.darker();

        final Color actualColor = CollectionLogLuckUtils.getOverallLuckColor(1);

        assertEquals(expectedColor, actualColor);
    }

    @Test
    public void getOverallLuckColor_returnsYellowAtAverageLuck() {
        final Color expectedColor = Color.YELLOW.darker();

        final Color actualColor = CollectionLogLuckUtils.getOverallLuckColor(0.5);

        assertEquals(expectedColor, actualColor);
    }

    @Test
    public void getOverallLuckColor_returnsRedAtMinLuck() {
        final Color expectedColor = Color.RED.darker();

        final Color actualColor = CollectionLogLuckUtils.getOverallLuckColor(0);

        assertEquals(expectedColor, actualColor);
    }

    @Test
    public void getOverallLuck_averageLuck() {
        assertEquals(0.5,  CollectionLogLuckUtils.getOverallLuck(0, 0), 0.0000001);
        assertEquals(0.5,  CollectionLogLuckUtils.getOverallLuck(0.000001, 0.000001), 0.0000001);
        assertEquals(0.5,  CollectionLogLuckUtils.getOverallLuck(0.1, 0.1), 0.0000001);
        assertEquals(0.5,  CollectionLogLuckUtils.getOverallLuck(0.499999, 0.499999), 0.0000001);
        assertEquals(0.5,  CollectionLogLuckUtils.getOverallLuck(0.5, 0.5), 0.0000001);
    }

    @Test
    public void getOverallLuck_nonAverageLuck() {
        // better than 50% of players, worse than 20% -> roughly in the middle of your [0.5,0.8] range.
        assertEquals(0.65,  CollectionLogLuckUtils.getOverallLuck(0.5, 0.2), 0.0000001);
        // If 50% of players have already gotten 1+ drops, but you have had 0, consider your simplified luck to be
        // in the middle of your potential [0,0.5] range.
        assertEquals(0.25,  CollectionLogLuckUtils.getOverallLuck(0, 0.5), 0.0000001);
    }

    @Test
    public void getOverallLuck_edgeCases() {
        assertEquals(1,  CollectionLogLuckUtils.getOverallLuck(1, 0), 0.0000001);
        assertEquals(0,  CollectionLogLuckUtils.getOverallLuck(0, 1), 0.0000001);
    }

    @Test
    public void getOverallLuck_clampsOutOfRange() {
        assertEquals(1,  CollectionLogLuckUtils.getOverallLuck(1.1, 0), 0.0000001);
        assertEquals(1,  CollectionLogLuckUtils.getOverallLuck(1, -0.1), 0.0000001);
        assertEquals(0,  CollectionLogLuckUtils.getOverallLuck(-0.1, 1), 0.0000001);
        assertEquals(0,  CollectionLogLuckUtils.getOverallLuck(0, 1.1), 0.0000001);
    }

}