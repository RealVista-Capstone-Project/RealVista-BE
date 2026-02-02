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
     * Pre-formatted amount with unit (e.g., "250,000 đ/tháng")
     */
    private String amount;

    /**
     * Fee type enum value for frontend identification
     */
    private String feeType;
}
