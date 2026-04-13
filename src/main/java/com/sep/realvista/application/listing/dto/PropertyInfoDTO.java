package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PropertyInfoDTO {
    private UUID propertyId;
    private String streetAddress;
    private BigDecimal landSizeM2;
    private BigDecimal usableSizeM2;
    private BigDecimal widthM;
    private BigDecimal lengthM;
    private String description;

    // Property details for UI display (bedrooms, bathrooms, area)
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal areaSqft;
}
