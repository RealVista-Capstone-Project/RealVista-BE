package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for a single price history entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryDTO {

    @JsonProperty("price_history_id")
    private UUID priceHistoryId;

    private BigDecimal price;

    @JsonProperty("min_price")
    private BigDecimal minPrice;

    @JsonProperty("max_price")
    private BigDecimal maxPrice;

    @JsonProperty("changed_at")
    private LocalDateTime changedAt;

    @JsonProperty("price_change")
    private BigDecimal priceChange;

    @JsonProperty("price_change_percent")
    private Double priceChangePercent;

    @JsonProperty("change_type")
    private PriceChangeType changeType;
}
