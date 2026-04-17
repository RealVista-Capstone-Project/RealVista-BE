package com.sep.realvista.application.listing.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedListingSearchCriteria {
    @Parameter(description = "Search query for name or address")
    private String search;

    @Parameter(description = "Filter by listing type: RENT, SALE")
    private String listingType;

    @Parameter(description = "Filter by listing status")
    private String status;

    @Parameter(description = "Sort criteria: newest, oldest, priceAsc, priceDesc")
    private String sortBy;

    @Parameter(description = "Filter by property ID")
    private UUID propertyId;
}
