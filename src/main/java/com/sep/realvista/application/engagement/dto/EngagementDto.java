package com.sep.realvista.application.engagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
