package com.sep.realvista.application.listing.dto.map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sep.realvista.application.common.dto.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for map-based property search.
 * Extends PageResponse to include pagination and adds map-specific metadata.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MapSearchResponse extends PageResponse<ListingSearchResponse> {

    /**
     * Echoed map bounds for client validation.
     */
    private MapBoundsDTO bounds;

    /**
     * Indicates if there are more results than returned (result was truncated by limit).
     * @deprecated Use pagination fields from PageResponse instead
     */
    @Deprecated
    private Boolean hasMore = false;

    /**
     * Filter metadata including applied filters and price statistics.
     */
    private FilterMetadataDTO filterMetadata;

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapBoundsDTO {
        private BigDecimal northLat;
        private BigDecimal southLat;
        private BigDecimal eastLng;
        private BigDecimal westLng;
    }

    /**
     * Nested DTO for filter metadata.
     */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterMetadataDTO {
        private AppliedFiltersDTO appliedFilters;
        private PriceRangeDTO availablePriceRange;
        private List<Integer> priceHistogram;
    }

    /**
     * Nested DTO for applied filters.
     */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedFiltersDTO {
        @Deprecated
        private String category;
        private String propertyCategory;
        private String propertyType;
        private PriceRangeDTO priceRange;
        private Integer bedrooms;
        private Integer bathrooms;
        private BigDecimal area;
        private String rentalPeriod;
        private String listingType;
        private String searchText;
    }

    /**
     * Nested DTO for price range.
     */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRangeDTO {
        private BigDecimal min;
        private BigDecimal max;
    }
}
