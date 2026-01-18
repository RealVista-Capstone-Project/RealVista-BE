package com.sep.realvista.infrastructure.constants;

/**
 * Constants for Google Maps API integration.
 */
public final class GoogleMapsConstants {

    private GoogleMapsConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    public static final class Api {
        public static final String GEOCODING_BASE_URL = "https://maps.googleapis.com/maps/api/geocode";
        public static final String PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place";
        public static final String MAPS_JAVASCRIPT_BASE_URL = "https://maps.googleapis.com/maps/api/js";

        private Api() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    public static final class ResponseStatus {
        public static final String OK = "OK";
        public static final String ZERO_RESULTS = "ZERO_RESULTS";
        public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
        public static final String REQUEST_DENIED = "REQUEST_DENIED";
        public static final String INVALID_REQUEST = "INVALID_REQUEST";
        public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

        private ResponseStatus() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    public static final class CacheKeys {
        public static final String GEOCODE_PREFIX = "maps:geocode:";
        public static final String PLACE_SEARCH_PREFIX = "maps:place:";

        private CacheKeys() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    public static final class ErrorCodes {
        public static final String API_KEY_INVALID = "MAPS_API_KEY_INVALID";
        public static final String QUOTA_EXCEEDED = "MAPS_QUOTA_EXCEEDED";
        public static final String RATE_LIMIT_EXCEEDED = "MAPS_RATE_LIMIT_EXCEEDED";
        public static final String GEOCODE_FAILED = "MAPS_GEOCODE_FAILED";
        public static final String PLACE_SEARCH_FAILED = "MAPS_PLACE_SEARCH_FAILED";
        public static final String NETWORK_ERROR = "MAPS_NETWORK_ERROR";

        private ErrorCodes() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }
}
