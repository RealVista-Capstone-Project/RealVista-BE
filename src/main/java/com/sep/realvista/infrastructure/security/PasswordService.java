package com.sep.realvista.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for password-related operations.
 * <p>
 * This service encapsulates password encoding logic to avoid circular dependencies
 * in the security configuration.
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    /**
     * Encodes a raw password.
     *
     * @param rawPassword the raw password to encode
     * @return the encoded password
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Checks if a raw password matches an encoded password.
     *
     * @param rawPassword     the raw password to check
     * @param encodedPassword the encoded password to match against
     * @return true if passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
