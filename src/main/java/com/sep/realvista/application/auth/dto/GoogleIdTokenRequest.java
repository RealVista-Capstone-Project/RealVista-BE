package com.sep.realvista.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for mobile Google OAuth2 login using ID token.
 * <p>
 * Mobile apps should use Google Sign-In SDK to get the ID token,
 * then send it to this endpoint for validation and authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Google ID token request for mobile authentication")
public class GoogleIdTokenRequest {

    @Schema(
            description = "Google ID token obtained from Google Sign-In SDK on mobile",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU0M...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "ID token is required")
    private String idToken;
}
