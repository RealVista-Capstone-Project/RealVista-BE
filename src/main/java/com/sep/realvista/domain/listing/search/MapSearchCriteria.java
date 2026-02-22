package com.sep.realvista.domain.listing.search;

import com.sep.realvista.domain.listing.ListingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Encapsulates all search criteria for map-based listing searches.
 * Used to avoid excessive parameter counts in repository methods.
 */
@Getter
@Builder
public class MapSearchCriteria {

    private final MapBounds bounds;
    private final ListingType listingType;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final String searchText;
    private final List<String> categories;
    private final Integer bedrooms;
    private final Integer bathrooms;
    private final BigDecimal area;
    @Builder.Default
    private final String sortBy = "publishedAt";
    @Builder.Default
    private final String sortDirection = "desc";
    private final int page;
    private final int size;

    /**
     * Get listing type as string for native queries.
     */
    public String getListingTypeStr() {
        return listingType != null ? listingType.name() : null;
    }

    /**
     * Calculate offset from page and size.
     */
    public int getOffset() {
        return (page - 1) * size;
    }
}
