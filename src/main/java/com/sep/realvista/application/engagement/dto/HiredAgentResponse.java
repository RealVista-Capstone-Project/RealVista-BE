package com.sep.realvista.application.engagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a hired agent as seen by the property owner.
 *
 * Contains agent profile information, the associated property,
 * and the engagement details that connect them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HiredAgentResponse {

    // --- Agent info ---
    private UUID agentUserId;
    private String agentFullName;
    private String agentAvatarUrl;
    private String agentPhone;
    private String agentEmail;
    private String agentBio;
    private String agentSpecialties;
    private String agentServiceAreas;
    private BigDecimal agentRating;
    private Integer agentYearsOfExperience;
    private Integer agentPropertiesSold;

    // --- Property info ---
    private UUID propertyId;
    private String propertyAddress;
    private String propertyTypeName;
    private String propertyLocationName;

    // --- Engagement info ---
    private UUID engagementId;
    private String engagementType;
    private String status;
    private LocalDateTime hiredAt;
    private boolean hasReview;
    private String cancellationReason;
    private String content;
}
