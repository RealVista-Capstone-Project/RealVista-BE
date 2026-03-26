package com.sep.realvista.application.listing.dto;

import com.sep.realvista.domain.listing.ListingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * DTO for creating a new listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateListingRequest {

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @NotNull(message = "Listing type is required")
    private ListingType listingType;

    @NotBlank(message = "Listing name is required")
    @Size(max = 500, message = "Listing name must not exceed 500 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, 
            message = "Minimum price must be greater than 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = false, 
            message = "Maximum price must be greater than 0")
    private BigDecimal maxPrice;

    @Builder.Default
    private Boolean isNegotiable = false;

    /**
     * Available from date - only for RENT listings.
     * NULL or past/today date = Available immediately.
     * Future date = Available from that specific date.
     */
    private LocalDate availableFrom;
    
    private String content;

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
}
