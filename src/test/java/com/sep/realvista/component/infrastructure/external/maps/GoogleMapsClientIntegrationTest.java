package com.sep.realvista.component.infrastructure.external.maps;

import com.google.maps.model.GeocodingResult;
import com.sep.realvista.infrastructure.external.maps.GoogleMapsClient;
import com.sep.realvista.infrastructure.external.maps.exception.GoogleMapsException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for Google Maps API client.
 *
 * IMPORTANT: This test requires a valid Google Maps API key to run.
 *
 * To run these tests:
 * 1. Get an API key from Google Cloud Console: https://console.cloud.google.com/
 * 2. Enable Geocoding API for your project
 * 3. Set the environment variable: GOOGLE_MAPS_API_KEY=your-api-key
 *
 * These tests are automatically skipped if GOOGLE_MAPS_API_KEY is not set.
 *
 * Run with Maven:
 *   mvn test -DGOOGLE_MAPS_API_KEY=your-key
 *
 * Run with Gradle:
 *   ./gradlew test -DGOOGLE_MAPS_API_KEY=your-key
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "GOOGLE_MAPS_API_KEY", matches = ".+")
class GoogleMapsClientIntegrationTest {

    @Autowired
    private GoogleMapsClient googleMapsClient;

    @Test
    void testConnection_shouldSucceed() {
        log.info("Testing Google Maps API connection...");

        // Act & Assert
        assertThatCode(() -> googleMapsClient.testConnection())
                .doesNotThrowAnyException();

        boolean connected = googleMapsClient.testConnection();
        assertThat(connected).isTrue();

        log.info("✓ Connection test PASSED");
    }

    @Test
    void geocodeAddress_withValidAddress_shouldReturnResults() {
        // Arrange
        String address = "Ho Chi Minh City, Vietnam";
        log.info("Testing geocoding for address: {}", address);

        // Act
        GeocodingResult[] results = googleMapsClient.geocodeAddress(address);

        // Assert
        assertThat(results).isNotNull();
        assertThat(results).isNotEmpty();
        assertThat(results[0].formattedAddress).contains("Ho Chi Minh");
        assertThat(results[0].geometry.location.lat).isBetween(10.0, 11.0);
        assertThat(results[0].geometry.location.lng).isBetween(106.0, 107.0);

        log.info(" Geocoding test PASSED");
        log.info("   Address: {}", results[0].formattedAddress);
        log.info("   Lat/Lng: {}, {}",
                results[0].geometry.location.lat,
                results[0].geometry.location.lng);
    }

    @Test
    void geocodeAddress_withInvalidAddress_shouldThrowException() {
        // Arrange
        String invalidAddress = "xyzabc123nonexistentplace456";
        log.info("Testing geocoding with invalid address: {}", invalidAddress);

        // Act & Assert
        assertThatThrownBy(() -> googleMapsClient.geocodeAddress(invalidAddress))
                .isInstanceOf(GoogleMapsException.class)
                .hasMessageContaining("No results found");

        log.info("✓ Invalid address test PASSED");
    }

    @Test
    void reverseGeocode_withValidCoordinates_shouldReturnResults() {
        // Arrange - Coordinates of Ben Thanh Market, HCMC
        double latitude = 10.772431;
        double longitude = 106.698265;
        log.info("Testing reverse geocoding for coordinates: {}, {}", latitude, longitude);

        // Act
        GeocodingResult[] results = googleMapsClient.reverseGeocode(latitude, longitude);

        // Assert
        assertThat(results).isNotNull();
        assertThat(results).isNotEmpty();
        assertThat(results[0].formattedAddress).isNotBlank();

        log.info(" Reverse geocoding test PASSED");
        log.info("   Address: {}", results[0].formattedAddress);
    }

    @Test
    void reverseGeocode_withOceanCoordinates_shouldReturnEmptyOrWaterBody() {
        // Arrange - Coordinates in the middle of Pacific Ocean
        double latitude = 0.0;
        double longitude = 180.0;
        log.info("Testing reverse geocoding for ocean coordinates: {}, {}", latitude, longitude);

        // Act
        GeocodingResult[] results = googleMapsClient.reverseGeocode(latitude, longitude);

        // Assert - May return empty or water body result
        assertThat(results).isNotNull();

        log.info(" Ocean coordinates test PASSED");
        log.info("   Results count: {}", results.length);
    }

    @Test
    void geocodeAddress_multipleRequests_shouldNotExceedRateLimit() {
        // Arrange
        String[] addresses = {
                "District 1, Ho Chi Minh City, Vietnam",
                "District 3, Ho Chi Minh City, Vietnam",
                "Binh Thanh District, Ho Chi Minh City, Vietnam",
                "Phu Nhuan District, Ho Chi Minh City, Vietnam",
                "Go Vap District, Ho Chi Minh City, Vietnam"
        };

        log.info("Testing rate limiting with {} requests", addresses.length);

        // Act & Assert
        for (String address : addresses) {
            assertThatCode(() -> {
                GeocodingResult[] results = googleMapsClient.geocodeAddress(address);
                assertThat(results).isNotEmpty();
                log.info("    Geocoded: {} -> {}, {}",
                        address.substring(0, Math.min(30, address.length())),
                        results[0].geometry.location.lat,
                        results[0].geometry.location.lng);
            }).doesNotThrowAnyException();
        }

        log.info("✓ Rate limiting test PASSED - all requests succeeded");
    }
}