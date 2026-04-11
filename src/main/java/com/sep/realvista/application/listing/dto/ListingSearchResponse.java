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

@Data
@SuperBuilder
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
    private String content;

    @JsonProperty("street_address")
    private String streetAddress;

    @JsonProperty("ward_name")
    private String wardName;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("city_name")
    private String cityName;
    // NOTE: These attributes will be dynamic, not only bedrooms and bathrooms
    private List<PropertyAttributeDTO> attributes;
    private String thumbnail; // Main image
    private LocalDateTime publishedAt;
    // Boost info
    @JsonProperty("is_boosted")
    private Boolean isBoosted;
    @JsonProperty("boost_packages")
    private List<String> boostPackages;
    // User info (for display/sorting context)
    private String userType; // AGENT or USER
    // Bookmark status for the requesting user (false for anonymous)
    @JsonProperty("is_favorite")
    private Boolean isFavorite;

    @JsonProperty("full_address")
    public String getFullAddress() {
        return AddressFormatter.formatFullAddress(streetAddress, wardName, districtName, cityName);
    }
}
