package com.sep.realvista.application.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Agent list item returned by GET /api/v1/agents.
 * Includes engagement status for a specific property when property_id is passed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentListItemResponse {
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private String specialties;
    private String serviceAreas;
    private BigDecimal rating;
    private Integer yearsOfExperience;
    private Integer propertiesSold;

    /** Engagement status this agent has with the queried property (null if none). */
    private String engagementStatus;
    /** Engagement ID for this agent + property (null if none). */
    private UUID engagementId;
    /** Engagement type (AGENT_PROPOSAL or OWNER_INVITATION). */
    private String engagementType;
}
