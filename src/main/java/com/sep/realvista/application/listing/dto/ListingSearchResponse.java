package com.sep.realvista.application.listing.dto;

import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingSearchResponse {
    private UUID listingId;
    private String name;
    private String slug;
    private ListingType listingType;
    private ListingStatus status;
    private BigDecimal price;
    private Double area; // Usable size
    private String location; // Short address or district
    
    // NOTE: These attributes will be dynamic, not only bedrooms and bathrooms
    private List<PropertyAttributeDTO> attributes;
    private String thumbnail; // Main image
    private LocalDateTime publishedAt;
    
    // Boost info
    @JsonProperty("is_boosted")
    private Boolean isBoosted;
    private String boostPackage; // e.g., "FEATURED", "HOT_BADGE"

    // User info (for display/sorting context)
    private String userType; // AGENT or USER

    // Bookmark status for the requesting user (false for anonymous)
    @JsonProperty("is_favorite")
    private Boolean isFavorite;
}
