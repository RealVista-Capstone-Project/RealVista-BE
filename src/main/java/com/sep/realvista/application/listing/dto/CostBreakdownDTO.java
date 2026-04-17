package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("base_price")
    private Long basePrice;

    @JsonProperty("base_price_unit")
    private String basePriceUnit;

    @JsonProperty("required_fees")
    private List<PropertyFeeDTO> requiredFees;

    @JsonProperty("required_fees_subtotal")
    private Long requiredFeesSubtotal;

    @JsonProperty("optional_fees")
    private List<PropertyFeeDTO> optionalFees;

    @JsonProperty("optional_fees_subtotal")
    private Long optionalFeesSubtotal;

    @JsonProperty("total_cost")
    private Long totalCost;

    @JsonProperty("disclaimer")
    private String disclaimer;
}
