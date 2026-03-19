package com.sep.realvista.application.listing.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.shared.util.AddressFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for bookmarked listing card display.
 * Contains essential information to display a favorite/bookmarked listing card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkListingCardDTO {

    @JsonProperty("listing_id")
    private UUID listingId;

    private String slug;

    private String title;

    private BigDecimal price;

    @JsonProperty("listing_type")
    private ListingType listingType;

    @JsonProperty("is_negotiable")
    private Boolean isNegotiable;

    @JsonProperty("primary_image_url")
    private String primaryImageUrl;

    @JsonProperty("street_address")
    private String streetAddress;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("ward_name")
    private String wardName;

    @JsonProperty("full_address")
    public String getFullAddress() {
        return AddressFormatter.formatFullAddress(streetAddress, wardName, districtName, cityName);
    }

    @JsonProperty("property_type_name")
    private String propertyTypeName;

    @JsonProperty("property_category_name")
    private String propertyCategoryName;

    private List<PropertyAttributeDTO> attributes;

    @JsonProperty("bookmarked_at")
    private LocalDateTime bookmarkedAt;

    @JsonProperty("area_sqft")
    private BigDecimal areaSqft;

    @JsonProperty("usable_size_m2")
    private BigDecimal usableSizeM2;

    private ListingStatus status;
}
