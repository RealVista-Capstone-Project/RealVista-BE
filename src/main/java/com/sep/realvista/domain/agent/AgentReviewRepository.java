package com.sep.realvista.domain.agent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AgentReview aggregate.
 *
 * Defines contract for agent review data access operations.
 * Implementation will be provided in infrastructure layer.
 */
public interface AgentReviewRepository {

    /**
     * Saves an agent review.
     *
     * @param agentReview the review to save
     * @return the saved review
     */
    AgentReview save(AgentReview agentReview);

    /**
     * Checks if a review already exists for a given engagement.
     *
     * @param engagementId the engagement ID
     * @return true if a review exists
     */
    boolean existsByEngagementId(UUID engagementId);

    /**
     * Finds engagement IDs that have been reviewed from a list of engagement IDs.
     * Used for batch checking hasReview status.
     *
     * @param engagementIds list of engagement IDs to check
     * @return set of engagement IDs that have reviews
     */
    List<UUID> findReviewedEngagementIds(List<UUID> engagementIds);

    /**
     * Calculates the average rating for an agent profile.
     *
     * @param agentProfileId the agent profile ID
     * @return average rating, or null if no reviews exist
     */
    BigDecimal calculateAverageRating(UUID agentProfileId);

    /**
     * Returns all reviews for a given agent profile, newest first.
     *
     * @param agentProfileId the agent profile ID
     * @return list of reviews
     */
    List<AgentReview> findByAgentProfileId(UUID agentProfileId);
}
