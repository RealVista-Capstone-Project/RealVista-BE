package com.sep.realvista.infrastructure.constants;


import java.util.List;

/**
 * Infrastructure layer security constants.
 * Contains technical security-related configurations.
 * <p>
 * These are infrastructure concerns, not domain or application logic.
 */
public final class SecurityConstants {


    private SecurityConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    /**
     * JWT token constants.
     */
    public static final class Jwt {
        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String HEADER_NAME = "Authorization";
        public static final String TOKEN_TYPE = "Bearer";
        public static final long DEFAULT_EXPIRATION_MS = 86400000L; // 24 hours

        private Jwt() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    /**
     * Public endpoints that don't require authentication.
     */
    public static final class PublicEndpoints {
        public static final String[] PUBLIC_PATHS = {
                "/api/v1/auth/**",
                "/v1/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/**",
                "/actuator/health/**",
                "/oauth2/**",
                "/login/oauth2/**",
                "/ws/**"
        };

        private PublicEndpoints() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    /**
     * OAuth2 constants.
     */
    public static final class OAuth2 {
        // Frontend redirect paths
        public static final String CALLBACK_PATH = "/vi/auth/callback";
        public static final String ERROR_PATH = "/login";

        // Query parameter names
        public static final String PARAM_ACCESS_TOKEN = "access_token";
        public static final String PARAM_USER_ID = "user_id";
        public static final String PARAM_EMAIL = "email";
        public static final String PARAM_ERROR = "error";

        // Error types
        public static final String ERROR_NO_EMAIL = "no_email";
        public static final String ERROR_AUTH_FAILED = "auth_failed";

        private OAuth2() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    /**
     * Cache names.
     */
    public static final class Cache {
        public static final String USERS = "users";
        public static final String USER_DETAILS = "userDetails";
        public static final int DEFAULT_TTL_MINUTES = 60;

        private Cache() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    /**
     * CORS configuration.
     */
    public static final class Cors {
        public static final List<String> ALLOWED_ORIGINS = List.of("http://localhost:3000");
        public static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        public static final List<String> ALLOWED_HEADERS = List.of("Authorization", "Content-Type", "Accept", "Origin");
        public static final List<String> EXPOSED_HEADERS = List.of("Authorization", "Content-Disposition");
        public static final long MAX_AGE_SECONDS = 3600L;

        private Cors() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    /**
     * OAuth2 URL paths.
     */
    public static final class Url {
        public static final String LOGIN_GOOGLE = "/api/v1/auth/login-google";

        private Url() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }
}

