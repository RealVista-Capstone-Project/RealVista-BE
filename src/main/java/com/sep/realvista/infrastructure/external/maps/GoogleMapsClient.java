package com.sep.realvista.infrastructure.external.maps;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.sep.realvista.infrastructure.config.map.GoogleMapsProperties;
import com.sep.realvista.infrastructure.external.maps.exception.GoogleMapsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Client for interacting with Google Maps API services.
 * Provides methods for geocoding, reverse geocoding, and place searches.
 * This is the infrastructure layer component that handles external API calls.
 *
 * Note: This bean is only created when GeoApiContext is available (i.e., when API key is configured).
 */
@Slf4j
@Component
@ConditionalOnBean(GeoApiContext.class)
@RequiredArgsConstructor
public class GoogleMapsClient {

    private final GeoApiContext geoApiContext;
    private final GoogleMapsProperties properties;

    /**
     * Forward geocoding: Convert address string to geographic coordinates.
     *
     * @param address The address to geocode
     * @return Array of geocoding results
     * @throws GoogleMapsException if geocoding fails
     */
    public GeocodingResult[] geocodeAddress(String address) {
        log.info("Geocoding address: {}", address);

        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();

            if (results == null || results.length == 0) {
                log.warn("No geocoding results found for address: {}", address);
                throw GoogleMapsException.geocodingFailed(address, "No results found");
            }

            log.info("Successfully geocoded address. Found {} results", results.length);
            return results;

        } catch (ApiException e) {
            log.error("Google Maps API error while geocoding address: {}", address, e);
            throw handleApiException(e, address);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Geocoding request interrupted for address: {}", address, e);
            throw GoogleMapsException.networkError(e);
        } catch (IOException e) {
            log.error("Network error while geocoding address: {}", address, e);
            throw GoogleMapsException.networkError(e);
        }
    }

    /**
     * Reverse geocoding: Convert geographic coordinates to address.
     *
     * @param latitude  Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Array of geocoding results
     * @throws GoogleMapsException if reverse geocoding fails
     */
    public GeocodingResult[] reverseGeocode(double latitude, double longitude) {
        log.info("Reverse geocoding coordinates: lat={}, lng={}", latitude, longitude);

        LatLng location = new LatLng(latitude, longitude);

        try {
            GeocodingResult[] results = GeocodingApi.reverseGeocode(geoApiContext, location).await();

            if (results == null || results.length == 0) {
                log.warn("No reverse geocoding results found for coordinates: {}, {}",
                        latitude, longitude);
                return new GeocodingResult[0];
            }

            log.info("Successfully reverse geocoded coordinates. Found {} results", results.length);
            return results;

        } catch (ApiException e) {
            log.error("Google Maps API error while reverse geocoding: lat={}, lng={}",
                    latitude, longitude, e);
            throw handleApiException(e, String.format("lat=%f, lng=%f", latitude, longitude));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Reverse geocoding request interrupted", e);
            throw GoogleMapsException.networkError(e);
        } catch (IOException e) {
            log.error("Network error while reverse geocoding", e);
            throw GoogleMapsException.networkError(e);
        }
    }

    /**
     * Test connection to Google Maps API.
     * Performs a simple geocoding request to verify API key and connectivity.
     *
     * @return true if connection is successful
     * @throws GoogleMapsException if connection test fails
     */
    public boolean testConnection() {
        log.info("Testing Google Maps API connection");

        try {
            // Simple test: geocode a well-known address
            GeocodingResult[] results = GeocodingApi.geocode(
                    geoApiContext,
                    "1600 Amphitheatre Parkway, Mountain View, CA"
            ).await();

            boolean success = results != null && results.length > 0;
            log.info("Google Maps API connection test {}", success ? "PASSED" : "FAILED");
            return success;

        } catch (ApiException e) {
            log.error("Google Maps API connection test failed", e);
            throw handleApiException(e, "connection test");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Connection test interrupted", e);
            throw GoogleMapsException.networkError(e);
        } catch (IOException e) {
            log.error("Network error during connection test", e);
            throw GoogleMapsException.networkError(e);
        }
    }

    /**
     * Handle Google Maps API exceptions and convert to domain exceptions.
     */
    private GoogleMapsException handleApiException(ApiException e, String context) {
        switch (e.getMessage()) {
            case "OVER_QUERY_LIMIT":
                return GoogleMapsException.quotaExceeded(context);
            case "REQUEST_DENIED":
                return GoogleMapsException.invalidApiKey();
            case "ZERO_RESULTS":
                return GoogleMapsException.geocodingFailed(context, "No results found");
            default:
                return new GoogleMapsException(
                        String.format("Google Maps API error: %s (context: %s)",
                                e.getMessage(), context),
                        "MAPS_API_ERROR",
                        e
                );
        }
    }
}