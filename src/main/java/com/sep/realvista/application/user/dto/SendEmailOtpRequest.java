package com.sep.realvista.application.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
}
