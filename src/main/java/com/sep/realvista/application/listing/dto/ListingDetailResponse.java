package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Complete listing detail response for UI.
 * Contains all information needed to populate the listing detail page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ListingDetailResponse {

    // Basic Listing Information
    private UUID listingId;
    private UUID propertyId;
    private UUID userId;
    private ListingType listingType;
    private ListingStatus status;
    private String slug;
    private String name;
    private String content;
    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isNegotiable;
    private LocalDate availableFrom;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Property Information
    private PropertyInfoDTO property;

    // Location Information
    private LocationInfoDTO location;

    // Property Type & Category
    private PropertyTypeInfoDTO propertyType;

    // Media (Photos, Videos, 3D Tours)
    private List<MediaDTO> media;

    // Owner/Agent Information
    private AgentInfoDTO agent;

    // Property Attributes/Features (bedrooms, bathrooms, etc.)
    private List<PropertyAttributeDTO> attributes;

    // Property Amenities (gym, pool, security, etc.) - dynamic based on property type
    private List<AmenityDTO> amenities;

    // Statistics
    private Integer totalPhotos;
    private Integer totalVideos;
    private Integer total3DTours;

    // Cost Breakdown (for RENT listings)
    private CostBreakdownDTO costBreakdown;

    // Bookmark status for the requesting user (null for anonymous)
    private Boolean isFavorite;

    // Indicates if the listing creator is the property owner
    private Boolean isCreatedByOwner;
}
