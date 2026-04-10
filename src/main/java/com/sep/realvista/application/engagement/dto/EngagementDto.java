package com.sep.realvista.application.engagement.dto;

import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EngagementDto {
    private UUID engagementId;
    private UUID initiatorId;
    private UUID receiverId;
    private EngagementType engagementType;
    private String content;
    private UUID listingId;
    private UUID propertyId;
    private EngagementStatus status;

    // Enriched listing info for display
    private String listingTitle;
    private String propertyAddress;
    private String propertyImageUrl;

    /** ISO-8601; avoid fixed pattern without fractional seconds (DB timestamps often have nanos). */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
