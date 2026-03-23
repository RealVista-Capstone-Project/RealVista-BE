package com.sep.realvista.infrastructure.persistence.engagement;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EngagementJpaRepository extends JpaRepository<Engagement, UUID> {
    List<Engagement> findByInitiatorIdAndDeletedFalse(UUID initiatorId);
    List<Engagement> findByInitiatorIdAndStatusAndDeletedFalse(UUID initiatorId, EngagementStatus status);
}
