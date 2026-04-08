package com.sep.realvista.application.billing.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ActiveFeatureSubscriptionResponse {
    private UUID subscriptionId;
    private String packageCode;
    private String packageName;
    private String featureType;
    /** Package quota cap when not unlimited (for usage %). Null when unlimited. */
    private Integer quotaLimit;
    private Integer remainingQuota;
    private boolean unlimited;
    /** Tier 0–4: Free, Basic, Premium, Pro, Pro+ */
    private int tierLevel;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
