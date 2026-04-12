package com.sep.realvista.infrastructure.persistence.engagement.hired;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of EngagementRepository interface.
 * Bridges the domain repository contract with Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class EngagementRepositoryImpl implements EngagementRepository {

    private static final List<EngagementStatus> HIRED_STATUSES = List.of(
            EngagementStatus.ACCEPTED,
            EngagementStatus.FINISHED,
            EngagementStatus.CANCELLED
    );

    private final EngagementJpaRepository jpaRepository;

    @Override
    public Engagement save(Engagement engagement) {
        return jpaRepository.save(engagement);
    }

    @Override
    public Optional<Engagement> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Engagement> findByIdWithFetches(UUID id) {
        return jpaRepository.findByIdWithFetches(id);
    }

    @Override
    public Page<Engagement> findHiredAgentEngagements(
            UUID ownerId, EngagementStatus status, String search, Pageable pageable) {
        return jpaRepository.findHiredAgentEngagements(ownerId, status, search, pageable);
    }

    @Override
    public Page<Engagement> findAllHiredAgentEngagements(UUID ownerId, String search, Pageable pageable) {
        return jpaRepository.findAllHiredAgentEngagements(ownerId, HIRED_STATUSES, search, pageable);
    }

    @Override
    public List<Engagement> findByParticipantWithFetches(UUID userId, String search) {
        return jpaRepository.findByParticipantWithFetches(userId, search);
    }

    @Override
    public List<Engagement> findByInitiatorId(UUID userId) {
        return jpaRepository.findByInitiatorIdAndDeletedFalse(userId);
    }

    @Override
    public Optional<Engagement> findLatestAgentProposalEngagement(
            UUID initiatorId, UUID receiverId, UUID propertyId) {
        return jpaRepository.findTopByInitiatorIdAndReceiverIdAndPropertyIdAndEngagementTypeAndDeletedFalseOrderByUpdatedAtDesc(
                initiatorId,
                receiverId,
                propertyId,
                EngagementType.AGENT_PROPOSAL
        );
    }
}
