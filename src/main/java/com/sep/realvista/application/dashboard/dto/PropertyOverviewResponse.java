package com.sep.realvista.application.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyOverviewResponse {
    private Long total;
    private Long listed;
    private BigDecimal listedPercent;
    private Long sold;
    private BigDecimal soldPercent;
    private List<ActiveListingItemDTO> activeListings;
}
