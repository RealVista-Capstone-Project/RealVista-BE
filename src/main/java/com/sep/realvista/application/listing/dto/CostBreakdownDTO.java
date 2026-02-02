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
     * Base rental price formatted (e.g., "15,000,000 đ/tháng")
     */
    private String basePrice;

    /**
     * Unit for base price (e.g., "đ/tháng")
     */
    private String basePriceUnit;

    /**
     * List of required fees that must be paid
     */
    private List<PropertyFeeDTO> requiredFees;

    /**
     * Subtotal of required fees formatted (e.g., "305,000 đ/tháng")
     */
    private String requiredFeesSubtotal;

    /**
     * List of optional fees that user can choose to pay
     */
    private List<PropertyFeeDTO> optionalFees;

    /**
     * Subtotal of optional fees formatted (e.g., "250,000 đ/tháng")
     */
    private String optionalFeesSubtotal;

    /**
     * Total monthly cost including base price and required fees only (e.g., "15,305,000 đ/tháng")
     */
    private String totalCost;

    /**
     * Disclaimer text about estimated costs
     */
    private String disclaimer;
}
