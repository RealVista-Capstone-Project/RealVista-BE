package com.sep.realvista.infrastructure.persistence.agent;

import com.sep.realvista.domain.agent.AgentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for AgentReview entity.
 */
public interface AgentReviewJpaRepository extends JpaRepository<AgentReview, UUID> {

    /**
     * Checks if a non-deleted review exists for a given engagement.
     */
    boolean existsByEngagementIdAndDeletedFalse(UUID engagementId);

    /**
     * Finds engagement IDs that have non-deleted reviews from a list of engagement IDs.
     */
    @Query("SELECT ar.engagementId FROM AgentReview ar "
           + "WHERE ar.engagementId IN :engagementIds AND ar.deleted = false")
    List<UUID> findEngagementIdsByEngagementIdIn(@Param("engagementIds") List<UUID> engagementIds);

    /**
     * Calculates the average rating for an agent profile (non-deleted reviews only).
     */
    @Query("SELECT AVG(ar.rating) FROM AgentReview ar "
           + "WHERE ar.agentProfileId = :agentProfileId AND ar.deleted = false")
    BigDecimal calculateAverageRatingByAgentProfileId(@Param("agentProfileId") UUID agentProfileId);

    /**
     * Fetches all non-deleted reviews for a given agent profile, newest first.
     */
    List<AgentReview> findByAgentProfileIdAndDeletedFalseOrderByCreatedAtDesc(UUID agentProfileId);
}
