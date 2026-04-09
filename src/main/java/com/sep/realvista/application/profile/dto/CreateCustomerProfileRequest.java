package com.sep.realvista.application.profile.dto;

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
public class CreateCustomerProfileRequest {

    @NotBlank(message = "Profile name is required")
    @Size(max = 200, message = "Profile name must not exceed 200 characters")
    private String profileName;
}
