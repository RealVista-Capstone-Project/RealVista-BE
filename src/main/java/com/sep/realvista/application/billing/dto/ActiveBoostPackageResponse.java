package com.sep.realvista.application.billing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ActiveBoostPackageResponse {
    private UUID boostPackageId;
    private String code;
    private String name;
    private String description;
    private Integer featuredQuota;
    private Integer hotBadgeQuota;
    private Integer durationDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer remainingFeaturedQuota;
    private Integer remainingHotBadgeQuota;
    private String status;
}
