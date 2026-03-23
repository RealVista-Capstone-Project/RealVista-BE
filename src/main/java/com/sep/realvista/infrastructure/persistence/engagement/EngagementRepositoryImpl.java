package com.sep.realvista.infrastructure.persistence.engagement;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.repository.EngagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        return jpaRepository.findById(id)
                .filter(e -> !e.getDeleted());
    }

    @Override
    public List<Engagement> findByInitiatorId(UUID initiatorId) {
        return jpaRepository.findByInitiatorIdAndDeletedFalse(initiatorId);
    }

    @Override
    public List<Engagement> findByInitiatorIdAndStatus(UUID initiatorId, EngagementStatus status) {
        return jpaRepository.findByInitiatorIdAndStatusAndDeletedFalse(initiatorId, status);
    }
}
