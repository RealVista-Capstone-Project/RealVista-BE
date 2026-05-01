package com.sep.realvista.application.dashboard.dto;

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
public class DashboardPropertyItemDTO {
    private UUID listingId;
    private String name;
    private String type;
    private BigDecimal cost;
    private Long activeLeads;
    private Long views;
    private String status;
    private String listingType;
}
