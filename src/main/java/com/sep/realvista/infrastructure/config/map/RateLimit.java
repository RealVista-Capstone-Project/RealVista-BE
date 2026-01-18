package com.sep.realvista.infrastructure.config.map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

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

    public RateLimit copy() {
        RateLimit r = new RateLimit();
        r.queriesPerSecond = this.queriesPerSecond;
        r.monthlyQuotaLimit = this.monthlyQuotaLimit;
        r.enableMonitoring = this.enableMonitoring;
        return r;
    }
}
