package com.sep.realvista.application.billing.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BoostPackageResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer featuredQuota;
    private Integer hotBadgeQuota;
    private Integer durationDays;
    private BigDecimal price;
}
