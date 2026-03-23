package com.sep.realvista.domain.engagement.repository;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EngagementRepository {
    Engagement save(Engagement engagement);
    Optional<Engagement> findById(UUID id);
    List<Engagement> findByInitiatorId(UUID initiatorId);
    List<Engagement> findByInitiatorIdAndStatus(UUID initiatorId, EngagementStatus status);
}
