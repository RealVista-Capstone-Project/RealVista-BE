package com.sep.realvista.application.engagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for an agent review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private UUID reviewId;
    private UUID engagementId;
    private UUID agentUserId;
    private UUID reviewerId;
    private BigDecimal rating;
    private String comment;
    private LocalDateTime createdAt;
}
