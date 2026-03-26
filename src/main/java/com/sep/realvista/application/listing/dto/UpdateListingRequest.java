package com.sep.realvista.application.listing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
}
