package com.sep.realvista.domain.listing.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Value object representing a geographical bounding box for map-based searches.
 * Immutable and validates that bounds are logically correct (north > south, east > west).
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class MapBounds {

    private final BigDecimal northLat;
    private final BigDecimal southLat;
    private final BigDecimal eastLng;
    private final BigDecimal westLng;

    /**
     * Factory method to create MapBounds with validation.
     *
     * @param northLat northern latitude boundary
     * @param southLat southern latitude boundary
     * @param eastLng eastern longitude boundary
     * @param westLng western longitude boundary
     * @return validated MapBounds instance
     * @throws IllegalArgumentException if bounds are invalid
     */
    public static MapBounds of(BigDecimal northLat, BigDecimal southLat,
                                BigDecimal eastLng, BigDecimal westLng) {
        validateBounds(northLat, southLat, eastLng, westLng);
        return new MapBounds(northLat, southLat, eastLng, westLng);
    }

    /**
     * Validates that the bounding box coordinates are logically correct.
     *
     * @throws IllegalArgumentException if bounds are invalid
     */
    private static void validateBounds(BigDecimal northLat, BigDecimal southLat,
                                        BigDecimal eastLng, BigDecimal westLng) {
        if (northLat == null || southLat == null || eastLng == null || westLng == null) {
            throw new IllegalArgumentException("All map bounds must be non-null");
        }

        if (northLat.compareTo(southLat) <= 0) {
            throw new IllegalArgumentException(
                    "North latitude must be greater than south latitude. "
                            + "Got north=" + northLat + ", south=" + southLat
            );
        }

        if (eastLng.compareTo(westLng) <= 0) {
            throw new IllegalArgumentException(
                    "East longitude must be greater than west longitude. "
                            + "Got east=" + eastLng + ", west=" + westLng
            );
        }

        // Validate latitude range (-90 to 90)
        if (northLat.compareTo(new BigDecimal("90")) > 0
                || southLat.compareTo(new BigDecimal("-90")) < 0) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90. "
                            + "Got north=" + northLat + ", south=" + southLat
            );
        }

        // Validate longitude range (-180 to 180)
        if (eastLng.compareTo(new BigDecimal("180")) > 0
                || westLng.compareTo(new BigDecimal("-180")) < 0) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180. "
                            + "Got east=" + eastLng + ", west=" + westLng
            );
        }
    }

    /**
     * Checks if a coordinate point is within this bounding box.
     *
     * @param latitude the latitude to check
     * @param longitude the longitude to check
     * @return true if the point is within bounds, false otherwise
     */
    public boolean contains(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        return latitude.compareTo(southLat) >= 0
                && latitude.compareTo(northLat) <= 0
                && longitude.compareTo(westLng) >= 0
                && longitude.compareTo(eastLng) <= 0;
    }

    /**
     * Checks if these bounds are valid (already validated in factory method).
     *
     * @return true (always, since invalid bounds cannot be constructed)
     */
    public boolean isValid() {
        return true;
    }
}
