package com.sep.realvista.application.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("access_token")
    @Schema(
            name = "access_token",
            description = "JWT access token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @Schema(description = "Token type", example = "Bearer", defaultValue = "Bearer")
    private String type;

    @JsonProperty("user_id")
    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @JsonProperty("roles")
    @Schema(
            description = "User roles",
            example = "[\"BUYER\", \"TENANT\"]",
            implementation = java.util.List.class
    )
    private java.util.List<String> roles;

    /**
     * Create response with default type.
     */
    public static class AuthenticationResponseBuilder {
        private String type = "Bearer";
    }
}

