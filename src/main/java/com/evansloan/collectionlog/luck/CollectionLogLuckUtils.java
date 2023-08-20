package com.evansloan.collectionlog.luck;

import java.awt.*;

public final class CollectionLogLuckUtils {

    // Format luck as a percentage with at least 2 significant digits, or more if needed for numbers close to 0 or 1.
    // Example: 0.0000145 -> 0.000015; 0.2535 -> 0.25; 0.500000 -> 0.50; 0.9999999 -> 0.9999999
    public static String formatLuckSigDigits(double luck) {
        int sigDigitsIfUnlucky = 2;

        // prevent numbers very close to 1, like 0.999, from being rounded up
        // Also, avoid crash if luck = 1 by clamping sig digits to <= 16, which is the most possible
        int sigDigitsIfLucky = Math.min(16, (int) Math.ceil(-Math.log10(1.0-luck)));

        int sigDigits = Math.max(sigDigitsIfUnlucky, sigDigitsIfLucky);
        return String.format("%."+sigDigits+"G", luck*100);
    }

    // Return green when luck - dryness = 1, red when luck - dryness = -1, and interpolate for values in between
    // TODO: make good/back luck colors configurable in Appearance config tab
    public static Color getOverallLuckColor(double overallFraction) {
        int r = (int)(255 * (1 - overallFraction));
        int g = (int)(255 * overallFraction);
        return new Color(r, g, 0).darker();
    }

    public static double getOverallLuck(double luckFraction, double drynessFraction) {
        double overallFraction = (luckFraction - drynessFraction + 1) / 2.0;
        // double check to make sure the fraction is in the correct range [0,1]
        overallFraction = Math.max(0, Math.min(1, overallFraction));
        return overallFraction;
    }

    // Return green when luck = 1, yellow when luck = 0, and interpolate for values in between.
    // luck = 0 could be a result of low KC, so it is just colored yellow.
    private static Color getLuckColor(double luckFraction) {
        int r = (int)(255 * (1 - luckFraction));
        return new Color(r, 255, 0).darker();
    }

    // Return red when dryness = 1, yellow when dryness = 0, and interpolate for values in between.
    // dryness = 0 could be a result of low KC, so it is just colored yellow.
    private static Color getDrynessColor(double drynessFraction) {
        int g = (int)(255 * (1 - drynessFraction));
        return new Color(255, g, 0).darker();
    }
}
