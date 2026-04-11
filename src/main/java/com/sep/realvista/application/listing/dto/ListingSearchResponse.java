package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ListingSearchResponse {
    private UUID listingId;
    private String name;
    private String slug;
    private ListingType listingType;
    private ListingStatus status;
    private BigDecimal price;
    private Double area; // Usable size
    private BigDecimal areaSqft;
    private String content;
    private Boolean isNegotiable;
    private CoordinatesDTO coordinates;
    private String streetAddress;
    private String wardName;
    private String districtName;
    private String cityName;
    private Integer bedrooms;
    private Integer bathrooms;

    private List<PropertyAttributeDTO> attributes;
    private String thumbnail; // Main image
    private LocalDateTime publishedAt;
    // Boost info
    private Boolean isBoosted;
    private List<String> boostPackages;
    // User info (for display/sorting context)
    private String userType; // AGENT or USER
    // Bookmark status for the requesting user (false for anonymous)
    private Boolean isFavorite;
    private LocalDateTime bookmarkedAt;
    private String propertyTypeName;
    private String propertyCategoryName;

    public String getFullAddress() {
        return AddressFormatter.formatFullAddress(streetAddress, wardName, districtName, cityName);
    }
}
