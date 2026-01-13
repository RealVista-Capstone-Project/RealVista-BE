package com.sep.realvista.infrastructure.security.util;

import com.sep.realvista.infrastructure.security.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for password-related operations.
 * <p>
 * Provides common password generation and encoding functionality
 * to avoid code duplication across the application.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordUtil {

    private final PasswordService passwordService;

    /**
     * Generates a random password and returns it hashed.
     * <p>
     * This is useful for OAuth2/Social login users who don't use
     * traditional password authentication. The generated password
     * is a random UUID that the user will never need to know.
     *
     * @return hashed random password
     */
    public String generateRandomHashedPassword() {
        log.debug("Generating random hashed password for OAuth2 user");
        String randomPassword = UUID.randomUUID().toString();
        return passwordService.encode(randomPassword);
    }

    /**
     * Generates a random password string (not hashed).
     * <p>
     * Use this when you need the raw password before hashing.
     *
     * @return random password string
     */
    public String generateRandomPassword() {
        return UUID.randomUUID().toString();
    }

    /**
     * Encodes a raw password.
     *
     * @param rawPassword the raw password to encode
     * @return hashed password
     */
    public String encodePassword(String rawPassword) {
        return passwordService.encode(rawPassword);
    }
}
