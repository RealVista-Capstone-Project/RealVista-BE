package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.shared.util.AddressFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for listing response (summary view).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingResponse {

    @JsonProperty("listing_id")
    private UUID listingId;

    @JsonProperty("property_id")
    private UUID propertyId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("listing_type")
    private ListingType listingType;

    @JsonProperty("status")
    private ListingStatus status;

    @JsonProperty("name")
    private String name;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("price")
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

    @JsonProperty("street_address")
    private String streetAddress;

    @JsonProperty("ward_name")
    private String wardName;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("full_address")
    public String getFullAddress() {
        return AddressFormatter.formatFullAddress(streetAddress, wardName, districtName, cityName);
    }
}
