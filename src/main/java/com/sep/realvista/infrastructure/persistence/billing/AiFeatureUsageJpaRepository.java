package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.AiFeature;
import com.sep.realvista.domain.billing.subscription.AiFeatureUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiFeatureUsageJpaRepository extends JpaRepository<AiFeatureUsage, UUID> {
    
    @Query("SELECT a FROM AiFeatureUsage a "
            + "WHERE a.userId = :userId "
            + "AND a.aiFeature = :feature "
            + "AND a.periodStart <= :date "
            + "AND a.periodEnd >= :date")
    Optional<AiFeatureUsage> findCurrentUsage(
            @Param("userId") UUID userId,
            @Param("feature") AiFeature feature,
            @Param("date") LocalDate date);
}
