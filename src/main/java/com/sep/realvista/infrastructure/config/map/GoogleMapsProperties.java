package com.sep.realvista.infrastructure.config.map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Google Maps API integration.
 * Maps to Google Maps.* properties in application.yml
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "google.maps")
public class GoogleMapsProperties {

    /**
     * Google Maps API key for backend services.
     * This key should have IP restrictions applied in Google Cloud Console.
     */
    @NotBlank(message = "Google Maps API key is required")
    private String apiKey;

    /**
     * Timeout for HTTP requests to Google Maps API in milliseconds.
     */
    @NotNull
    @Positive
    private Integer connectTimeoutMs = 10000;

    /**
     * Read timeout for HTTP requests in milliseconds.
     */
    @NotNull
    @Positive
    private Integer readTimeoutMs = 10000;

    /**
     * Maximum number of retries for failed requests.
     */
    @NotNull
    @Positive
    private Integer maxRetries = 3;

    /**
     * Rate limiting configuration.
     */
    private final RateLimit rateLimit = new RateLimit();

    /**
     * Cache configuration for geocoding results.
     */
    private final Cache cache = new Cache();

    public RateLimit getRateLimit() {
        return rateLimit.copy();
    }

    public Cache getCache() {
        return cache.copy();
    }
}