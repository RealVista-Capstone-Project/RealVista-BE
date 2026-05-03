package com.sep.realvista.infrastructure.external.geocoding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Calls Google Maps Geocoding API to resolve a human-readable address into
 * a geographic bounding box (viewport).
 *
 * <p>If the API call fails or returns no results, all coordinates default to 0.0.
 * Callers must decide whether default bounds are acceptable.</p>
 */
@Slf4j
@Service
public class GeocodingService {

    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final RestTemplate restTemplate;
    private final String apiKey;

    public GeocodingService(
            RestTemplate restTemplate,
            @Value("${google.maps.api-key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    /**
     * Result record carrying the four bounding-box edges.
     */
    public record BoundingBox(
            BigDecimal northLat,
            BigDecimal southLat,
            BigDecimal eastLng,
            BigDecimal westLng) {

        public static BoundingBox zero() {
            return new BoundingBox(ZERO, ZERO, ZERO, ZERO);
        }
    }

    /**
     * Geocodes {@code addressQuery} and extracts the viewport bounding box.
     *
     * @param addressQuery human-readable address, e.g. "Hoàn Kiếm, Hà Nội, Vietnam"
     * @return bounding box, or all-zeros on any failure
     */
    public BoundingBox getBoundingBox(String addressQuery) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Google Maps API key is not configured — bounding box will default to 0.0");
            return BoundingBox.zero();
        }

        String url = UriComponentsBuilder.fromUriString(GEOCODE_URL)
                .queryParam("address", addressQuery)
                .queryParam("key", apiKey)
                .build()
                .toUriString();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                log.warn("Geocoding API returned null for address: {}", addressQuery);
                return BoundingBox.zero();
            }

            String status = response.get("status") instanceof String value ? value : null;
            if (!"OK".equals(status)) {
                Object errorMessage = response.get("error_message");
                log.warn(
                        "Geocoding API failed for address '{}': status={}, error_message={}",
                        addressQuery,
                        status,
                        errorMessage);
                return BoundingBox.zero();
            }

            @SuppressWarnings("unchecked")
            var results = (java.util.List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                log.warn("Geocoding API returned no results for address: {}", addressQuery);
                return BoundingBox.zero();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> geometry = (Map<String, Object>) results.get(0).get("geometry");
            @SuppressWarnings("unchecked")
            Map<String, Object> viewport = (Map<String, Object>) geometry.get("viewport");
            @SuppressWarnings("unchecked")
            Map<String, Object> northeast = (Map<String, Object>) viewport.get("northeast");
            @SuppressWarnings("unchecked")
            Map<String, Object> southwest = (Map<String, Object>) viewport.get("southwest");

            BigDecimal northLat = toBigDecimal(northeast.get("lat"));
            BigDecimal eastLng  = toBigDecimal(northeast.get("lng"));
            BigDecimal southLat = toBigDecimal(southwest.get("lat"));
            BigDecimal westLng  = toBigDecimal(southwest.get("lng"));

            log.debug("Geocoded '{}' → N={} S={} E={} W={}", addressQuery, northLat, southLat, eastLng, westLng);
            return new BoundingBox(northLat, southLat, eastLng, westLng);

        } catch (RestClientException | ClassCastException | NullPointerException ex) {
            log.warn("Geocoding failed for address '{}': {} — defaulting to 0.0", addressQuery, ex.getMessage());
            return BoundingBox.zero();
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        return ZERO;
    }
}
