package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Agent/Owner information nested DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentInfoDTO {
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("business_name")
    private String businessName;
    private String email;
    private String phone;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String company;
    @JsonProperty("is_verified")
    private Boolean isVerified;
}
