package com.sep.realvista.application.listing.dto.map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sep.realvista.domain.listing.ListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for map-based property search.
 * Contains geographical bounding box and optional filters.
 */
@Schema(description = "Map-based property search request with geographical bounds and filters")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapSearchRequest {

    @Schema(description = "Northern latitude boundary", example = "10.85", required = true)
    @NotNull(message = "North latitude is required")
    @DecimalMin(value = "-90.0", message = "North latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "North latitude must be between -90 and 90")
    private BigDecimal northLat;

    @Schema(description = "Southern latitude boundary", example = "10.70", required = true)
    @NotNull(message = "South latitude is required")
    @DecimalMin(value = "-90.0", message = "South latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "South latitude must be between -90 and 90")
    private BigDecimal southLat;

    @Schema(description = "Eastern longitude boundary", example = "106.75", required = true)
    @NotNull(message = "East longitude is required")
    @DecimalMin(value = "-180.0", message = "East longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "East longitude must be between -180 and 180")
    private BigDecimal eastLng;

    @Schema(description = "Western longitude boundary", example = "106.60", required = true)
    @NotNull(message = "West longitude is required")
    @DecimalMin(value = "-180.0", message = "West longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "West longitude must be between -180 and 180")
    private BigDecimal westLng;

    /**
     * Optional filter for listing type (RENT or SALE).
     * If null, returns all listing types.
     */
    @Schema(description = "Filter by listing type", example = "RENT", allowableValues = {"RENT", "SALE"})
    private ListingType listingType;

    /**
     * Optional minimum price filter.
     */
    @Schema(description = "Minimum price filter (VND)", example = "5000000")
    @Min(value = 0, message = "Minimum price must be non-negative")
    private BigDecimal minPrice;

    /**
     * Optional maximum price filter.
     */
    @Schema(description = "Maximum price filter (VND)", example = "50000000")
    private BigDecimal maxPrice;

    /**
     * Maximum number of markers to return.
     * Defaults to 50, max 100 to prevent performance issues.
     */
    @Schema(description = "Maximum results to return", example = "50", minimum = "1", maximum = "100")
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    @Builder.Default
    private Integer limit = 50;

    /**
     * Sort field for results.
     * Supported values: price, createdAt, publishedAt
     */
    @Schema(description = "Sort field", example = "publishedAt", 
            allowableValues = {"price", "createdAt", "publishedAt"})
    @Builder.Default
    private String sortBy = "publishedAt";

    /**
     * Sort direction: asc or desc.
     */
    @Schema(description = "Sort direction", example = "desc", allowableValues = {"asc", "desc"})
    @Builder.Default
    private String sortDirection = "desc";
}
