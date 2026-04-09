package com.sep.realvista.application.billing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
