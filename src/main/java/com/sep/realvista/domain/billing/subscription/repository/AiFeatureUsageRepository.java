package com.sep.realvista.domain.billing.subscription.repository;

import com.sep.realvista.domain.billing.subscription.AiFeature;
import com.sep.realvista.domain.billing.subscription.AiFeatureUsage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface AiFeatureUsageRepository {
    Optional<AiFeatureUsage> findByUserIdAndAiFeatureAndPeriod(
            UUID userId, AiFeature feature, LocalDate start, LocalDate end);
    
    AiFeatureUsage save(AiFeatureUsage usage);
}
