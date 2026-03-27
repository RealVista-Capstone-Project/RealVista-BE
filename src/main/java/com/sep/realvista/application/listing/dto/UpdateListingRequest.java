package com.sep.realvista.application.listing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for updating an existing listing.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateListingRequest {

    @Size(max = 500, message = "Listing name must not exceed 500 characters")
    private String name;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, 
            message = "Minimum price must be greater than 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = false, 
            message = "Maximum price must be greater than 0")
    private BigDecimal maxPrice;

    private Boolean isNegotiable;

    private String content;

    /**
     * Available from date - only for RENT listings.
     * NULL or past/today date = Available immediately.
     * Future date = Available from that specific date.
     */
    private LocalDate availableFrom;

    /**
     * Ordered list of property media IDs selected for this listing.
     * The order determines display_order (0-indexed).
     */
    private List<UUID> mediaIds;

    /**
     * The property media ID that should be marked as primary (is_primary = true).
     * Must be one of the items in mediaIds.
     */
    private UUID primaryMediaId;

    /**
     * List of new media to be added specifically for this listing.
     * These will be created as listing-only property media.
     */
    private List<MediaRequest> newMedias;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaRequest {
        private String url;
        private com.sep.realvista.domain.property.MediaType type;
        private String thumbnailUrl;
        private Boolean isPrimary;
    }
}
