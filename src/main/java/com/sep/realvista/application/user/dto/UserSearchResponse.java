package com.sep.realvista.application.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for user search results, with masked phone number for security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String maskedPhone;
    private String phone;
    private String avatarUrl;
}
