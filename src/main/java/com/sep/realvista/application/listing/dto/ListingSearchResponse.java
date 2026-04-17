package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.shared.util.AddressFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.sep.realvista.application.listing.dto.map.PropertyMapMarker.CoordinatesDTO;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ListingSearchResponse {
    @JsonProperty("listing_id")
    private UUID listingId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("listing_type")
    private ListingType listingType;

    @JsonProperty("status")
    private ListingStatus status;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("area")
    private Double area;

    @JsonProperty("area_sqft")
    private BigDecimal areaSqft;

    @JsonProperty("content")
    private String content;

    @JsonProperty("is_negotiable")
    private Boolean isNegotiable;

    @JsonProperty("coordinates")
    private CoordinatesDTO coordinates;

    @JsonProperty("street_address")
    private String streetAddress;

    @JsonProperty("ward_name")
    private String wardName;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("bedrooms")
    private Integer bedrooms;

    @JsonProperty("bathrooms")
    private Integer bathrooms;

    @JsonProperty("attributes")
    private List<PropertyAttributeDTO> attributes;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    @JsonProperty("is_boosted")
    private Boolean isBoosted;

    @JsonProperty("boost_packages")
    private List<String> boostPackages;

    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("is_favorite")
    private Boolean isFavorite;

    @JsonProperty("bookmarked_at")
    private LocalDateTime bookmarkedAt;

    @JsonProperty("property_type_name")
    private String propertyTypeName;

    @JsonProperty("property_category_name")
    private String propertyCategoryName;

    @JsonProperty("full_address")
    public String getFullAddress() {
        return AddressFormatter.formatFullAddress(streetAddress, wardName, districtName, cityName);
    }
}
