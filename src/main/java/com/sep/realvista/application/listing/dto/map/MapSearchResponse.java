package com.sep.realvista.application.listing.dto.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for map-based property search.
 * Contains property markers and metadata about the search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapSearchResponse {

    /**
     * List of property markers within the map bounds.
     */
    private List<PropertyMapMarker> markers;

    /**
     * Total count of properties matching the criteria (may exceed markers size if limited).
     */
    private Long totalCount;

    /**
     * Echoed map bounds for client validation.
     */
    private MapBoundsDTO bounds;

    /**
     * Indicates if there are more results than returned (result was truncated by limit).
     */
    private Boolean hasMore;

    /**
     * Nested DTO for map bounds.
     */
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
}
