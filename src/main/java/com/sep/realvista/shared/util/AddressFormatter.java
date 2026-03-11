package com.sep.realvista.shared.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for formatting Vietnamese addresses.
 * Provides consistent address formatting across all DTOs.
 */
public final class AddressFormatter {

    private AddressFormatter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Formats a full Vietnamese address from components.
     * Components are joined with comma-space separator, null/blank values are filtered out.
     *
     * Format: {street}, {ward}, {district}, {city}
     * Example: "123 Nguyen Hue, Ward 1, District 1, Ho Chi Minh City"
     *
     * @param streetAddress street number and name
     * @param wardName ward/commune name
     * @param districtName district name
     * @param cityName city/province name
     * @return formatted address string, empty string if all components are null/blank
     */
    public static String formatFullAddress(
            String streetAddress,
            String wardName,
            String districtName,
            String cityName) {
        return Stream.of(streetAddress, wardName, districtName, cityName)
                .filter(component -> component != null && !component.isBlank())
                .collect(Collectors.joining(", "));
    }
}
