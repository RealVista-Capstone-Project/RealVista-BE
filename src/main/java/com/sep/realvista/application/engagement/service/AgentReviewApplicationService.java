package com.sep.realvista.application.engagement.service;

import com.sep.realvista.application.engagement.dto.CreateReviewRequest;
import com.sep.realvista.application.engagement.dto.ReviewResponse;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.agent.AgentReview;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Application service for agent review operations.
 *
 * Handles review submission after engagement completion/cancellation
 * and recalculates agent average ratings.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AgentReviewApplicationService {

    private final EngagementRepository engagementRepository;
    private final AgentReviewRepository agentReviewRepository;
    private final AgentProfileRepository agentProfileRepository;

    /**
     * Submits a review for an agent after an engagement is finished or cancelled.
     *
     * Validates:
     * - Engagement exists
     * - Reviewer is the owner of the engagement
     * - Engagement status is FINISHED or CANCELLED
     * - Engagement has a listing_id (required for agent_reviews table)
     * - No existing review for this engagement
     *
     * After saving the review, recalculates the agent's average rating.
     *
     * @param engagementId the engagement ID
     * @param reviewerId the authenticated owner's user ID
     * @param request the review details (rating, optional comment)
     * @return the created review response
     */
    @Caching(evict = {
            @CacheEvict(value = "hiredAgents", allEntries = true),
            @CacheEvict(value = "engagement", allEntries = true)
    })
    public ReviewResponse submitReview(UUID engagementId, UUID reviewerId, CreateReviewRequest request) {
        log.info("Submitting review for engagement: {} by reviewer: {}", engagementId, reviewerId);

        // 1. Find and validate engagement
        Engagement engagement = engagementRepository.findById(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("Engagement", engagementId));

        // 2. Validate reviewer is the owner of this engagement
        validateReviewerIsOwner(engagement, reviewerId);

        // 3. Validate engagement status allows review
        if (engagement.getStatus() != EngagementStatus.FINISHED
                && engagement.getStatus() != EngagementStatus.CANCELLED) {
            throw new BusinessConflictException(
                    "Reviews can only be submitted for FINISHED or CANCELLED engagements",
                    "INVALID_ENGAGEMENT_STATUS");
        }

        // 4. Validate engagement has a listing_id
        if (engagement.getListingId() == null) {
            throw new BusinessConflictException(
                    "Cannot submit review: this engagement has no associated listing",
                    "ENGAGEMENT_NO_LISTING");
        }

        // 5. Check for existing review
        if (agentReviewRepository.existsByEngagementId(engagementId)) {
            throw new BusinessConflictException(
                    "A review has already been submitted for this engagement",
                    "REVIEW_ALREADY_EXISTS");
        }

        // 6. Resolve agent profile
        UUID agentUserId = resolveAgentUserId(engagement);
        AgentProfile agentProfile = agentProfileRepository.findByUserId(agentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agent profile not found for user: " + agentUserId));

        // 7. Build and save the review
        AgentReview agentReview = AgentReview.builder()
                .agentProfileId(agentProfile.getAgentProfileId())
                .reviewerId(reviewerId)
                .listingId(engagement.getListingId())
                .engagementId(engagementId)
                .review(request.getComment())
                .rating(request.getRating())
                .build();

        AgentReview savedReview = agentReviewRepository.save(agentReview);
        log.info("Review saved with ID: {} for engagement: {}", savedReview.getAgentReviewId(), engagementId);

        // 8. Recalculate and update agent's average rating
        BigDecimal averageRating = agentReviewRepository.calculateAverageRating(agentProfile.getAgentProfileId());
        if (averageRating != null) {
            BigDecimal roundedRating = averageRating.setScale(1, RoundingMode.HALF_UP);
            agentProfile.updateRating(roundedRating);
            agentProfileRepository.save(agentProfile);
            log.info("Updated agent {} average rating to {}", agentUserId, roundedRating);
        }

        // 9. Return response
        return ReviewResponse.builder()
                .reviewId(savedReview.getAgentReviewId())
                .engagementId(engagementId)
                .agentUserId(agentUserId)
                .reviewerId(savedReview.getReviewerId())
                .rating(savedReview.getRating())
                .comment(savedReview.getReview())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }

    /**
     * Validates that the reviewer is the owner of the engagement.
     */
    private void validateReviewerIsOwner(Engagement engagement, UUID reviewerId) {
        boolean isOwner;
        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            isOwner = engagement.getReceiverId().equals(reviewerId);
        } else {
            isOwner = engagement.getInitiatorId().equals(reviewerId);
        }

        if (!isOwner) {
            throw new BusinessConflictException(
                    "You are not authorized to review this engagement",
                    "ENGAGEMENT_NOT_OWNED");
        }
    }

    /**
     * Resolves the agent user ID from an engagement based on engagement type.
     */
    private UUID resolveAgentUserId(Engagement engagement) {
        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            return engagement.getInitiatorId();
        } else {
            return engagement.getReceiverId();
        }
    }
}
