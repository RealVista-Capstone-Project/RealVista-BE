package com.sep.realvista.infrastructure.persistence.agent;

import com.sep.realvista.domain.agent.AgentReview;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * JPA implementation of AgentReviewRepository interface.
 *
 * Bridges the domain repository contract with Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class AgentReviewRepositoryImpl implements AgentReviewRepository {

    private final AgentReviewJpaRepository jpaRepository;

    @Override
    public AgentReview save(AgentReview agentReview) {
        return jpaRepository.save(agentReview);
    }

    @Override
    public boolean existsByEngagementId(UUID engagementId) {
        return jpaRepository.existsByEngagementIdAndDeletedFalse(engagementId);
    }

    @Override
    public List<UUID> findReviewedEngagementIds(List<UUID> engagementIds) {
        if (engagementIds == null || engagementIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findEngagementIdsByEngagementIdIn(engagementIds);
    }

    @Override
    public BigDecimal calculateAverageRating(UUID agentProfileId) {
        return jpaRepository.calculateAverageRatingByAgentProfileId(agentProfileId);
    }
}
