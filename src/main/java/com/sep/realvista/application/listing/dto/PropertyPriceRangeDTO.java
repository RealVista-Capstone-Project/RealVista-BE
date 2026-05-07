package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors {@link com.sep.realvista.domain.common.value.PriceRangeVO} for listing detail JSON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPriceRangeDTO {

    @JsonProperty("rent")
    private MoneyRangeDTO rent;

    @JsonProperty("buy")
    private MoneyRangeDTO buy;
}
