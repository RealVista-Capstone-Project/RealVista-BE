package com.sep.realvista.application.property.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyFeedCriteria {

    @Parameter(description = "Search keyword for property address or description")
    private String keyword;

    @Parameter(description = "Filter by property type ID")
    private UUID propertyTypeId;

    @Parameter(description = "Filter by location ID (ward/district/city)")
    private UUID locationId;

    @Parameter(description = "Minimum price for rent")
    private BigDecimal minRentPrice;

    @Parameter(description = "Maximum price for rent")
    private BigDecimal maxRentPrice;

    @Parameter(description = "Minimum price for buy")
    private BigDecimal minBuyPrice;

    @Parameter(description = "Maximum price for buy")
    private BigDecimal maxBuyPrice;
}
