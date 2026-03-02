package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for listing price history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {

    @JsonProperty("listing_id")
    private UUID listingId;

    @JsonProperty("current_price")
    private BigDecimal currentPrice;

    @JsonProperty("price_history")
    private List<PriceHistoryDTO> priceHistory;
}
