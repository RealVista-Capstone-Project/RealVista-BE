package com.sep.realvista.presentation.rest.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long userId;
    private String email;
}

