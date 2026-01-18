package com.sep.realvista.infrastructure.config.map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cache {
    /**
     * Enable caching for geocoding results.
     */
    private Boolean enabled = true;

    /**
     * Cache TTL in seconds (default: 7 days).
     */
    @NotNull
    @Positive
    private Long ttlSeconds = 604800L;

    /**
     * Maximum cache size.
     */
    @NotNull
    @Positive
    private Integer maxSize = 10000;

    public Cache copy() {
        Cache c = new Cache();
        c.enabled = this.enabled;
        c.ttlSeconds = this.ttlSeconds;
        c.maxSize = this.maxSize;
        return c;
    }
}