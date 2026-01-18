package com.sep.realvista.unit.infrastructure.external.maps;

import com.google.maps.GeoApiContext;
import com.sep.realvista.infrastructure.config.map.GoogleMapsProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test to verify Google Maps configuration is loaded correctly.
 * This test runs even without API key to check configuration setup.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class GoogleMapsConnectionTest {

    @Autowired(required = false)
    private GeoApiContext geoApiContext;

    @Autowired
    private GoogleMapsProperties properties;

    @Test
    void googleMapsProperties_shouldBeLoaded() {
        log.info("Testing Google Maps properties configuration...");

        // Assert properties are loaded with defaults
        assertThat(properties).isNotNull();
        assertThat(properties.getConnectTimeoutMs()).isPositive();
        assertThat(properties.getReadTimeoutMs()).isPositive();
        assertThat(properties.getMaxRetries()).isPositive();

        log.info("Properties configuration loaded:");
        log.info("   Connect Timeout: {} ms", properties.getConnectTimeoutMs());
        log.info("   Read Timeout: {} ms", properties.getReadTimeoutMs());
        log.info("   Max Retries: {}", properties.getMaxRetries());
        log.info("   Rate Limit: {} QPS", properties.getRateLimit().getQueriesPerSecond());
        log.info("   Monthly Quota: {}", properties.getRateLimit().getMonthlyQuotaLimit());
        log.info("   Cache Enabled: {}", properties.getCache().getEnabled());

        boolean hasApiKey = properties.getApiKey() != null && !properties.getApiKey().isEmpty();
        log.info("   API Key Set: {}", hasApiKey);

        if (!hasApiKey) {
            log.warn("   ⚠ API key is not configured - Google Maps features will be disabled");
        }
    }

    @Test
    void geoApiContext_whenNoApiKey_shouldShowWarning() {
        boolean hasApiKey = properties.getApiKey() != null && !properties.getApiKey().isEmpty();

        if (!hasApiKey) {
            log.warn("=".repeat(80));
            log.warn("GOOGLE_MAPS_API_KEY is not set!");
            log.warn("   To enable Google Maps integration:");
            log.warn("   1. Get API key from: https://console.cloud.google.com/");
            log.warn("   2. Set environment variable: GOOGLE_MAPS_API_KEY=your-key");
            log.warn("   3. Or add to application-test.yml: google.maps.api-key: your-key");
            log.warn("=".repeat(80));

            // Assert that GeoApiContext is not created when API key is missing
            assertThat(geoApiContext)
                    .as("GeoApiContext should not be created without API key")
                    .isNull();
        } else {
            log.info("✓ API Key is configured");

            // Assert that GeoApiContext IS created when API key is present
            assertThat(geoApiContext)
                    .as("GeoApiContext should be created when API key is present")
                    .isNotNull();
        }
    }
}