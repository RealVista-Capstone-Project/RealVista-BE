package com.sep.realvista.infrastructure.config.map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Cache configuration for Google Maps API geocoding results.
 */
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
}