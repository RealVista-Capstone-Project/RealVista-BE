package com.sep.realvista.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for password encoding.
 * <p>
 * This is separated from SecurityConfig to avoid circular dependencies:
 * - PasswordService needs PasswordEncoder
 * - SecurityConfig needs OAuth2AuthenticationSuccessHandler
 * - OAuth2AuthenticationSuccessHandler needs PasswordService
 * <p>
 * By extracting PasswordEncoder into its own config, we break the cycle.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates the password encoder bean.
     * Uses BCrypt hashing algorithm with default strength (10 rounds).
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
