package com.sep.realvista.application.policy.dto;

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
    private UUID policyId;
    private String title;
    private String slug;
    private String content;
    private LocalDateTime updatedAt;
}
