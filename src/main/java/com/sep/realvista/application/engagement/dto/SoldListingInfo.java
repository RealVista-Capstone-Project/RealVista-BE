package com.sep.realvista.application.engagement.dto;

import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Nested DTO representing the listing associated with an engagement.
 * Serialized as {@code sold_listing} in the API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldListingInfo {

    private UUID listingId;
    private String title;
    private BigDecimal price;
    private String imageUrl;
    private String status;
    private String listingType;
    private String address;
    private List<PropertyAttributeDTO> attributes;
}
