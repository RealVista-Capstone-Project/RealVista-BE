package com.sep.realvista.application.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for property fee information in cost breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyFeeDTO {
    /**
     * Vietnamese display name of the fee (e.g., "Phí quản lý")
     */
    private String name;

    /**
     * Fee amount in VND (number, not formatted)
     */
    private Long amount;

    /**
     * Fee type enum value for frontend identification
     */
    private String feeType;
}
