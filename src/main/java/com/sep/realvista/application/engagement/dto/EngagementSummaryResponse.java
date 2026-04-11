package com.sep.realvista.application.engagement.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight response DTO for the general engagement list.
 * Does not include hired-agent-specific fields (hiredAt, hasReview, soldListing).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngagementSummaryResponse {

    // --- Engagement core ---
    private UUID engagementId;
    private String engagementType;
    private String status;
    private JsonNode content;

    // --- Initiator / Receiver ---
    private UUID initiatorId;
    private String initiatorName;
    private UUID receiverId;
    private String receiverName;
    private String receiverAvatarUrl;

    // --- Property info ---
    private UUID propertyId;
    private String propertyAddress;
    private String propertyTypeName;
    private String propertyLocationName;
    private String propertyImageUrl;
    private List<String> propertyMediaUrls;

    // --- Listing info ---
    private String listingTitle;

    // --- Agent summary (for display in cards) ---
    private UUID agentUserId;
    private String agentFullName;
    private String agentAvatarUrl;

    // --- Timestamps ---
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
