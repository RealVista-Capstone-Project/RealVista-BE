package com.sep.realvista.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reset password using token from email")
public class ResetPasswordRequest {

    @NotBlank(message = "Token is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).*$",
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
