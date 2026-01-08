package com.sep.realvista.infrastructure.config;


import org.springframework.beans.factory.annotation.Value;

/**
 * Infrastructure layer security constants.
 * Contains technical security-related configurations.
 * <p>
 * These are infrastructure concerns, not domain or application logic.
 */
public final class SecurityConstants {

    @Value("${spring.profiles.active:dev}")
    private static String activeProfile;

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
                "/login/oauth2/**"
        };

        private PublicEndpoints() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    /**
     * OAuth2 constants.
     */
    public static final class OAuth2 {
        public static final String GOOGLE_PROVIDER = "google";
        public static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization";
        public static final String REDIRECT_BASE_URI = "/login/oauth2/code/*";
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
        public static final String[] ALLOWED_ORIGINS = {"http://localhost:3000"};
        public static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};
        public static final String[] ALLOWED_HEADERS = {"*"};
        public static final long MAX_AGE_SECONDS = 3600L;
        private Cors() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    public static final class Url {
        public static final String FRONTEND_URL = "dev".equals(activeProfile) ? "http://localhost:3000" : "TBU";
        public static final String FRONTEND_LOGIN_GOOGLE = FRONTEND_URL + "/login-google";
        private Url() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }
}

