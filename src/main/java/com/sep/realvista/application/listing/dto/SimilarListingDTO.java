package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.listing.ListingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight DTO for similar listings in listing detail.
 * Contains essential information for displaying similar listings cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarListingDTO {

    @JsonProperty("listing_id")
    private UUID listingId;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("name")
    private String name;

    @JsonProperty("listing_type")
    private ListingType listingType;

    @JsonProperty("property_type_name")
    private String propertyTypeName;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("area")
    private BigDecimal area;

    @JsonProperty("location_name")
    private String locationName;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("similarity_score")
    private Integer similarityScore;

    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    /**
     * Top required attributes for the property (e.g., bedrooms, bathrooms).
     * Limited to 3 attributes based on the property type's required attributes.
     */
    @JsonProperty("attributes")
    private List<PropertyAttributeDTO> attributes;
}
