package com.sep.realvista.application.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for cost breakdown information in listing detail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostBreakdownDTO {
    /**
     * Base rental price in VND (number)
     */
    private Long basePrice;

    /**
     * Unit for base price (e.g., "đ/tháng")
     */
    private String basePriceUnit;

    /**
     * List of required fees that must be paid
     */
    private List<PropertyFeeDTO> requiredFees;

    /**
     * Subtotal of required fees in VND (number)
     */
    private Long requiredFeesSubtotal;

    /**
     * List of optional fees that user can choose to pay
     */
    private List<PropertyFeeDTO> optionalFees;

    /**
     * Subtotal of optional fees in VND (number)
     */
    private Long optionalFeesSubtotal;

    /**
     * Total monthly cost including base price and required fees only in VND (number)
     */
    private Long totalCost;

    /**
     * Disclaimer text about estimated costs
     */
    private String disclaimer;
}
