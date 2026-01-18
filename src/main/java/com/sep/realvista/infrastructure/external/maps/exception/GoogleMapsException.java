package com.sep.realvista.infrastructure.external.maps.exception;

import com.sep.realvista.domain.common.exception.DomainException;

/**
 * Exception thrown when Google Maps API operations fail.
 */
public class GoogleMapsException extends DomainException {

    public GoogleMapsException(String message, String errorCode) {
        super(message, errorCode);
    }

    public GoogleMapsException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    /**
     * Factory method for quota exceeded errors.
     */
    public static GoogleMapsException quotaExceeded(String details) {
        return new GoogleMapsException(
                "Google Maps API quota exceeded: " + details,
                "MAPS_QUOTA_EXCEEDED"
        );
    }

    /**
     * Factory method for rate limit errors.
     */
    public static GoogleMapsException rateLimitExceeded() {
        return new GoogleMapsException(
                "Google Maps API rate limit exceeded. Please try again later.",
                "MAPS_RATE_LIMIT_EXCEEDED"
        );
    }

    /**
     * Factory method for invalid API key errors.
     */
    public static GoogleMapsException invalidApiKey() {
        return new GoogleMapsException(
                "Google Maps API key is invalid or has insufficient permissions",
                "MAPS_API_KEY_INVALID"
        );
    }

    /**
     * Factory method for network/connectivity errors.
     */
    public static GoogleMapsException networkError(Throwable cause) {
        return new GoogleMapsException(
                "Failed to connect to Google Maps API",
                "MAPS_NETWORK_ERROR",
                cause
        );
    }

    /**
     * Factory method for geocoding failures.
     */
    public static GoogleMapsException geocodingFailed(String address, String reason) {
        return new GoogleMapsException(
                String.format("Failed to geocode address '%s': %s", address, reason),
                "MAPS_GEOCODE_FAILED"
        );
    }
}