package com.sep.realvista.infrastructure.config.map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Rate limiting configuration for Google Maps API.
 */
@Getter
@Setter
public class RateLimit {
    /**
     * Maximum queries per second to prevent quota overages.
     */
    @NotNull
    @Positive
    private Integer queriesPerSecond = 50;

    /**
     * Monthly quota limit (15K map loads as per requirement).
     */
    @NotNull
    @Positive
    private Long monthlyQuotaLimit = 15000L;

    /**
     * Enable quota monitoring and alerting.
     */
    private Boolean enableMonitoring = true;
}