package com.sep.realvista.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO. Accepts either email or phone number.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request with email or phone, and password")
public class LoginRequest {

    @Schema(
            description = "User email address (provide either email or phone)",
            example = "buyertenantuser001@realvista.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
            description = "User phone number (provide either email or phone)",
            example = "+84901234567",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phone;

    @Schema(
            description = "User password",
            example = "Password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    private String password;

    @AssertTrue(message = "Either email or phone number must be provided")
    private boolean isIdentifierProvided() {
        return (email != null && !email.isBlank()) || (phone != null && !phone.isBlank());
    }
}
