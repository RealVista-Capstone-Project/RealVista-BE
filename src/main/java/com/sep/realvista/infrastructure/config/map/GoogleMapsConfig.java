package com.sep.realvista.infrastructure.config.map;

import com.google.maps.GeoApiContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for Google Maps API integration.
 * Sets up GeoApiContext with proper timeouts, retry logic, and rate limiting.
 */
@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(GoogleMapsProperties.class)
@RequiredArgsConstructor
public class GoogleMapsConfig {

    private final GoogleMapsProperties properties;

    /**
     * Creates and configures GeoApiContext for Google Maps API calls.
     * This context is thread-safe and should be reused across the application.
     *
     * Only created when google.maps.api-key property is set and not empty.
     *
     * @return configured GeoApiContext instance or null if API key is not configured
     */
    @Bean
    @ConditionalOnProperty(name = "google.maps.api-key")
    public GeoApiContext geoApiContext() {
        String apiKey = properties.getApiKey();

        if (!StringUtils.hasText(apiKey)) {
            log.warn("Google Maps API key is configured but empty. Google Maps features will be disabled.");
            log.warn("Set GOOGLE_MAPS_API_KEY environment variable to enable Google Maps integration.");
            return null;
        }
        log.info("Initializing Google Maps API context");
        log.debug("API Key configured: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        log.info("Rate limit: {} queries per second", properties.getRateLimit().getQueriesPerSecond());
        log.info("Monthly quota limit: {}", properties.getRateLimit().getMonthlyQuotaLimit());

        GeoApiContext.Builder builder = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .maxRetries(properties.getMaxRetries());

        // Configure rate limiting to prevent quota overages
        Integer qps = properties.getRateLimit().getQueriesPerSecond();
        if (qps != null && qps > 0) {
            builder.queryRateLimit(qps);
            log.info("Rate limiting enabled: {} QPS", qps);
        }

        GeoApiContext context = builder.build();

        log.info("Google Maps API context initialized successfully");
        return context;
    }

    /**
     * Gracefully shutdown GeoApiContext on application shutdown.
     * Only created when GeoApiContext bean exists.
     */
    @Bean
    @ConditionalOnBean(GeoApiContext.class)
    public GoogleMapsShutdownHook googleMapsShutdownHook(GeoApiContext geoApiContext) {
        return new GoogleMapsShutdownHook(geoApiContext);
    }

    /**
     * Hook to ensure proper cleanup of GeoApiContext resources.
     */
    @RequiredArgsConstructor
    private static class GoogleMapsShutdownHook {
        private final GeoApiContext geoApiContext;

        @jakarta.annotation.PreDestroy
        public void shutdown() {
            if (geoApiContext != null) {
                log.info("Shutting down Google Maps API context");
                geoApiContext.shutdown();
            }
        }
    }
}