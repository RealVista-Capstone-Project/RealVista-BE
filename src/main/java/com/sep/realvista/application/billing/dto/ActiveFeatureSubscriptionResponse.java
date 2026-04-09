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
public class ActiveFeatureSubscriptionResponse {
    private UUID subscriptionId;
    private String packageCode;
    private String packageName;
    private String featureType;
    private Integer quotaLimit;
    private Integer remainingQuota;
    private boolean unlimited;
    private int tierLevel;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
