package com.sep.realvista.presentation.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Authentication response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT token and user information")
public class AuthenticationResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer", defaultValue = "Bearer")
    private String type;

    @Schema(description = "User ID", example = "1")
    private UUID userId;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    /**
     * Create response with default type.
     */
    public static class AuthenticationResponseBuilder {
        private String type = "Bearer";
    }
}

