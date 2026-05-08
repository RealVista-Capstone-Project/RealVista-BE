package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertySummaryMetricsResponse {
    long totalProperties;
    long currentMonthTotalProperties;
    long previousTotalProperties;
    long availableProperties;
    long reservedProperties;
    long soldProperties;
    long rentedProperties;
    long draftProperties;
    long pendingProperties;
    long verifiedProperties;
    long rejectedProperties;

    /** Sum of land_size_m2 across properties that have it set */
    BigDecimal totalLandAreaM2;
    /** Average land_size_m2 among properties with land size set */
    BigDecimal averageLandAreaM2;
    /** Rough portfolio midpoint estimate from property price_range (buy, else rent) */
    BigDecimal estimatedPortfolioValueVnd;
    /** Year-over-year % change in estimated portfolio value (nullable when not meaningful) */
    Double estimatedPortfolioValueYoyPercent;

    long publishedListingsCount;
    long listingsExpiringSoonCount;

    /** Counts for dashboard showcase chips (keys: HOUSE, VILLA, TOWNHOUSE, SHOPHOUSE) */
    Map<String, Long> showcaseTypeCounts;
}
