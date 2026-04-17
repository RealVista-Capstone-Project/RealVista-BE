package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.AiFeature;
import com.sep.realvista.domain.billing.subscription.AiFeatureUsage;
import com.sep.realvista.domain.billing.subscription.repository.AiFeatureUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AiFeatureUsageRepositoryImpl implements AiFeatureUsageRepository {

    private final AiFeatureUsageJpaRepository jpaRepository;

    @Override
    public Optional<AiFeatureUsage> findByUserIdAndAiFeatureAndPeriod(
            UUID userId, AiFeature feature, LocalDate start, LocalDate end) {
        // Specifically for AI Assistant, we check for a period covering 'now'
        return jpaRepository.findCurrentUsage(userId, feature, LocalDate.now());
    }

    @Override
    public AiFeatureUsage save(AiFeatureUsage usage) {
        return jpaRepository.save(usage);
    }
}
