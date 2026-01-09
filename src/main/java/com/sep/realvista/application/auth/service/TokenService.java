package com.sep.realvista.application.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

/**
 * Token service contract for authentication token operations.
 * <p>
 * This interface follows the Dependency Inversion Principle (DIP) by defining
 * the contract in the application layer, allowing the infrastructure layer to
 * provide concrete implementations (e.g., JWT, OAuth2, etc.).
 * <p>
 * Benefits:
 * - Decouples application logic from specific token implementation
 * - Improves testability (easy to mock)
 * - Allows future token strategy changes without modifying application code
 * - Follows Clean Architecture and DDD principles
 */
public interface TokenService {

    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the authentication token
     * @return the username contained in the token
     */
    String extractUsername(String token);

    /**
     * Generates a token for the given user details.
     *
     * @param userDetails the user details
     * @return the generated authentication token
     */
    String generateToken(UserDetails userDetails);

    /**
     * Generates a token with additional claims for the given user details.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @return the generated authentication token
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Validates if the token is valid for the given user details.
     *
     * @param token       the authentication token to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}
