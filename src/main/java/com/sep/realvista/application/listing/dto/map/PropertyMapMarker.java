package com.sep.realvista.application.listing.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.listing.ListingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight DTO for property markers on map.
 * Contains minimal data needed to render markers and show basic info on hover/click.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMapMarker {

    /**
     * Listing ID for fetching full details.
     */
    private UUID listingId;

    /**
     * Property coordinates for map marker placement.
     */
    private CoordinatesDTO coordinates;

    /**
     * Street address of the property.
     */
    @JsonProperty("street_address")
    private String streetAddress;

    @JsonProperty("ward_name")
    private String wardName;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("full_address")
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetAddress != null && !streetAddress.isBlank()) {
            address.append(streetAddress);
        }
        if (wardName != null && !wardName.isBlank()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(wardName);
        }
        if (districtName != null && !districtName.isBlank()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(districtName);
        }
        if (cityName != null && !cityName.isBlank()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(cityName);
        }
        return address.toString();
    }

    /**
     * Price and listing type for display.
     */
    private BigDecimal price;
    private ListingType listingType;

    /**
     * Property name/title.
     */
    private String name;

    /**
     * Thumbnail URL for marker popup/card.
     */
    private String thumbnailUrl;

    /**
     * Basic property attributes for quick preview.
     */
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal sizeM2;

    /**
     * Property type (e.g., "Apartment", "House", "Condo").
     */
    private String propertyType;

    /**
     * Indicates if this property is marked as favorite by the current user.
     */
    private Boolean isFavorite;

    /**
     * Dynamic property attributes for card display.
     */
    private List<PropertyAttributeDTO> attributes;

    /**
     * Nested DTO for coordinates.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesDTO {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}
