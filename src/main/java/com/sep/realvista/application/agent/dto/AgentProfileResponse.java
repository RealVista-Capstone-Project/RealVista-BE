package com.sep.realvista.application.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * API view of the authenticated agent's profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentProfileResponse {
    private UUID agentProfileId;
    private UUID userId;
    private String bio;
    private String specialties;
    private String serviceAreas;
    private BigDecimal rating;
    private Integer yearsOfExperience;
    private Integer propertiesSold;
}
