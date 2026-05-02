package com.sep.realvista.application.policy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDto {
    @JsonProperty("policy_id")
    private UUID policyId;
    private String title;
    private String slug;
    private String content;
    @JsonProperty("is_active")
    private Boolean isActive;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

}

