package com.sep.realvista.infrastructure.config.map;

import com.google.maps.GeoApiContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     * @return configured GeoApiContext instance
     */
    @Bean
    public GeoApiContext geoApiContext() {
        log.info("Initializing Google Maps API context");
        log.info("Rate limit: {} queries per second",
                properties.getRateLimit().getQueriesPerSecond());
        log.info("Monthly quota limit: {}",
                properties.getRateLimit().getMonthlyQuotaLimit());

        GeoApiContext.Builder builder = new GeoApiContext.Builder()
                .apiKey(properties.getApiKey())
                .connectTimeout(properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .maxRetries(properties.getMaxRetries());

        // Configure rate limiting to prevent quota overages
        if (properties.getRateLimit().getQueriesPerSecond() > 0) {
            builder.queryRateLimit(properties.getRateLimit().getQueriesPerSecond());
            log.info("Rate limiting enabled: {} QPS",
                    properties.getRateLimit().getQueriesPerSecond());
        }

        GeoApiContext context = builder.build();

        log.info("Google Maps API context initialized successfully");
        return context;
    }

    /**
     * Gracefully shutdown GeoApiContext on application shutdown.
     */
    @Bean
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
            log.info("Shutting down Google Maps API context");
            if (geoApiContext != null) {
                geoApiContext.shutdown();
            }
        }
    }
}