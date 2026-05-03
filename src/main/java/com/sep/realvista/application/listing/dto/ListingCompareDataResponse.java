package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
 * DTO for listing compare data.
 * Contains all information needed for the compare page UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingCompareDataResponse {

    // Basic Info
    @JsonProperty("listing_id")
    private UUID listingId;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("name")
    private String name;
    @JsonProperty("price")
    private BigDecimal price;
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    @JsonProperty("max_price")
    private BigDecimal maxPrice;
    @JsonProperty("listing_type")
    private String listingType;
    @JsonProperty("is_negotiable")
    private Boolean isNegotiable;

    // Featured/Hot badges
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    @JsonProperty("is_hot")
    private Boolean isHot;

    // Media
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;
    @JsonProperty("media_count")
    private Integer mediaCount;

    // Property Type
    @JsonProperty("property_type")
    private PropertyTypeInfoDTO propertyType;

    // Location
    @JsonProperty("location")
    private LocationInfoDTO location;
    @JsonProperty("full_address")
    private String fullAddress;

    // Property Details
    @JsonProperty("usable_size_m2")
    private BigDecimal usableSizeM2;
    @JsonProperty("land_size_m2")
    private BigDecimal landSizeM2;
    @JsonProperty("width_m")
    private BigDecimal widthM;
    @JsonProperty("length_m")
    private BigDecimal lengthM;

    // Key Attributes (extracted for quick access)
    @JsonProperty("bedrooms")
    private Integer bedrooms;
    @JsonProperty("bathrooms")
    private Integer bathrooms;
    @JsonProperty("floor")
    private Integer floor;
    @JsonProperty("total_floors")
    private Integer totalFloors;
    @JsonProperty("direction")
    private String direction;

    // All Attributes (for detailed comparison)
    @JsonProperty("attributes")
    private List<PropertyAttributeDTO> attributes;

    // Amenities
    @JsonProperty("amenities")
    private List<AmenityDTO> amenities;

    // Dates
    @JsonProperty("available_from")
    private LocalDate availableFrom;
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    // Description
    @JsonProperty("content")
    private String content;
}
