package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Property information nested DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInfoDTO {
    @JsonProperty("property_id")
    private UUID propertyId;
    @JsonProperty("street_address")
    private String streetAddress;
    @JsonProperty("land_size_m2")
    private BigDecimal landSizeM2;
    @JsonProperty("usable_size_m2")
    private BigDecimal usableSizeM2;
    @JsonProperty("width_m")
    private BigDecimal widthM;
    @JsonProperty("length_m")
    private BigDecimal lengthM;
    private String description;

    // Property details for UI display (bedrooms, bathrooms, area)
    @JsonProperty("bedrooms")
    private Integer bedrooms;
    @JsonProperty("bathrooms")
    private Integer bathrooms;
    @JsonProperty("area_sqft")
    private BigDecimal areaSqft;
}
