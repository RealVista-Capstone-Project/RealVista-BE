package com.sep.realvista.application.ai.service;

import com.sep.realvista.domain.billing.subscription.AiFeature;
import com.sep.realvista.domain.billing.subscription.AiFeatureUsage;
import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.repository.AiFeatureUsageRepository;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuotaApplicationService {

    private final UserFeatureSubscriptionRepository subscriptionRepository;
    private final AiFeatureUsageRepository usageRepository;

    @Transactional
    public boolean checkAndIncrementQuota(UUID userId, AiFeature feature) {
        // 1. Find active AI_REQUEST subscription
        List<UserFeatureSubscription> subscriptions = subscriptionRepository
                .findActiveByUserIdAndFeatureType(userId, FeatureType.AI_REQUEST);

        UserFeatureSubscription activeSub = subscriptions.stream()
                .filter(UserFeatureSubscription::isUsable)
                .findFirst()
                .orElse(null);

        if (activeSub == null) {
            log.warn("No active AI subscription found for user={}", userId);
            return false;
        }

        FeaturePackage pkg = activeSub.getFeaturePackage();
        if (pkg == null) {
            return false;
        }

        // Unlimited check
        if (pkg.isUnlimited()) {
            return true;
        }

        // 2. Get today's usage
        LocalDate today = LocalDate.now();
        AiFeatureUsage usage = usageRepository.findByUserIdAndAiFeatureAndPeriod(
                userId, feature, today, today)
                .orElseGet(() -> AiFeatureUsage.builder()
                        .userId(userId)
                        .aiFeature(feature)
                        .periodStart(today)
                        .periodEnd(today)
                        .usageCount(0)
                        .build());

        // 3. Check against package quota
        if (usage.getUsageCount() >= pkg.getQuota()) {
            log.info("Quota exceeded for user={}, feature={}, usage={}, limit={}",
                    userId, feature, usage.getUsageCount(), pkg.getQuota());
            return false;
        }

        // 4. Increment and save
        usage.incrementUsage();
        usageRepository.save(usage);
        return true;
    }
}
