package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAttributeRangeDTO {

    @JsonProperty("range_id")
    private UUID propertyAttributeRangeId;

    private String label;

    @JsonProperty("min_value")
    private BigDecimal minValue;

    @JsonProperty("max_value")
    private BigDecimal maxValue;

    @JsonProperty("display_order")
    private Integer displayOrder;
}
