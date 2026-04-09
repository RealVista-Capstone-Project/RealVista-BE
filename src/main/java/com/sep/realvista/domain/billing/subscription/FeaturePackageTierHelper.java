package com.sep.realvista.domain.billing.subscription;

/**
 * Maps feature package codes to a linear tier (0 = free … 4 = top) per product line.
 * Used to block downgrades and to suggest the next upgrade package.
 */
public final class FeaturePackageTierHelper {

    private FeaturePackageTierHelper() {
    }

    public static int tierLevel(FeaturePackage pkg) {
        if (pkg == null) {
            return 0;
        }
        return tierLevelFromCode(pkg.getCode());
    }

    public static int tierLevelFromCode(String code) {
        if (code == null || code.isBlank()) {
            return 0;
        }
        return switch (code) {
            case "LISTING_FREE", "3D_TOUR_FREE", "AI_FREE" -> 0;
            case "LISTING_10", "3D_TOUR_5", "AI_50" -> 1;
            case "LISTING_25", "3D_TOUR_15", "AI_100" -> 2;
            case "LISTING_50", "3D_TOUR_30", "AI_200" -> 3;
            case "LISTING_UNLIMITED", "3D_TOUR_UNLIMITED", "AI_UNLIMITED" -> 4;
            default -> 0;
        };
    }
}
