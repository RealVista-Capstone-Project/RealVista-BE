package com.sep.realvista.infrastructure.persistence.engagement.hired;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of EngagementRepository interface.
 *
 * Bridges the domain repository contract with Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class EngagementRepositoryImpl implements EngagementRepository {

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
    public Page<Engagement> findHiredAgentEngagements(UUID ownerId, Pageable pageable) {
        return jpaRepository.findHiredAgentEngagements(ownerId, EngagementStatus.ACCEPTED, pageable);
    }
}
