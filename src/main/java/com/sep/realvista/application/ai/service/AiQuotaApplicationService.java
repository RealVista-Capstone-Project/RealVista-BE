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

        // Use originalQuota (snapshotted at checkout) so admin updates to the package
        // quota never retroactively affect existing subscriptions.
        // -1 means unlimited. For legacy rows with null originalQuota, fall back to
        // remainingQuota, never the live package quota.
        Integer quotaLimit = activeSub.getOriginalQuota();
        if (quotaLimit != null && quotaLimit == -1) {
            return true;
        }
        if (quotaLimit == null) {
            quotaLimit = activeSub.getRemainingQuota();
        }
        if (quotaLimit == null) {
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

        // 3. Check against snapshotted quota limit
        if (usage.getUsageCount() >= quotaLimit) {
            log.info("Quota exceeded for user={}, feature={}, usage={}, limit={}",
                    userId, feature, usage.getUsageCount(), quotaLimit);
            return false;
        }

        // 4. Increment and save
        usage.incrementUsage();
        usageRepository.save(usage);
        return true;
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getAiQuotaStatus(UUID userId) {
        // 1. Find active AI_REQUEST subscription
        List<UserFeatureSubscription> subscriptions = subscriptionRepository
                .findActiveByUserIdAndFeatureType(userId, FeatureType.AI_REQUEST);

        UserFeatureSubscription activeSub = subscriptions.stream()
                .filter(UserFeatureSubscription::isUsable)
                .findFirst()
                .orElse(null);

        java.util.Map<String, Object> status = new java.util.HashMap<>();
        
        if (activeSub == null) {
            status.put("hasSubscription", false);
            status.put("remaining", 0);
            status.put("limit", 0);
            status.put("isUnlimited", false);
            return status;
        }

        FeaturePackage pkg = activeSub.getFeaturePackage();
        if (pkg == null) {
            status.put("hasSubscription", false);
            status.put("remaining", 0);
            status.put("limit", 0);
            status.put("isUnlimited", false);
            return status;
        }

        // Use originalQuota (snapshotted at checkout) — same logic as checkAndIncrementQuota.
        // Never use live package quota for active subscriptions.
        Integer quotaLimit = activeSub.getOriginalQuota();
        if (quotaLimit == null) {
            quotaLimit = activeSub.getRemainingQuota();
        }
        boolean unlimited = quotaLimit == null || quotaLimit == -1;

        status.put("hasSubscription", true);
        status.put("isUnlimited", unlimited);
        status.put("limit", unlimited ? -1 : quotaLimit);

        if (unlimited) {
            status.put("remaining", -1);
        } else {
            // Get today's usage
            LocalDate today = LocalDate.now();
            int usageCount = usageRepository.findByUserIdAndAiFeatureAndPeriod(
                    userId, AiFeature.AI_ASSISTANT, today, today)
                    .map(AiFeatureUsage::getUsageCount)
                    .orElse(0);

            status.put("remaining", Math.max(0, quotaLimit - usageCount));
        }

        return status;
    }
}
