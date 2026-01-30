package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ListingDetailResponse {

    // Basic Listing Information
    @JsonProperty("listing_id")
    private UUID listingId;
    @JsonProperty("property_id")
    private UUID propertyId;
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("listing_type")
    private ListingType listingType;
    private ListingStatus status;
    private BigDecimal price;
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    @JsonProperty("max_price")
    private BigDecimal maxPrice;
    @JsonProperty("is_negotiable")
    private Boolean isNegotiable;
    @JsonProperty("available_from")
    private LocalDate availableFrom;
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
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

    // Property Attributes/Features (amenities, facilities, etc.)
    private List<PropertyAttributeDTO> attributes;

    // Statistics
    @JsonProperty("total_photos")
    private Integer totalPhotos;
    @JsonProperty("total_videos")
    private Integer totalVideos;
    @JsonProperty("total_3d_tours")
    private Integer total3DTours;
}
