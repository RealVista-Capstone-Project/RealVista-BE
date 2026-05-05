package com.sep.realvista.application.billing.service;

import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscriptionStatus;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationService {

    private final UserFeatureSubscriptionRepository subscriptionRepository;

    /**
     * Runs daily at 00:00 (midnight) to check and expire subscriptions that have passed their end date.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void dailyMaintenance() {
        log.info("Starting daily subscription maintenance...");
        expireExpiredSubscriptions();
        resetAiQuotas();
        log.info("Daily subscription maintenance completed.");
    }

    private void expireExpiredSubscriptions() {
        List<UserFeatureSubscription> activeSubscriptions =
                subscriptionRepository.findByStatus(UserFeatureSubscriptionStatus.ACTIVE);

        LocalDate today = LocalDate.now();
        int expiredCount = 0;

        for (UserFeatureSubscription sub : activeSubscriptions) {
            if (sub.getFeaturePackage() != null && sub.getFeaturePackage().isFree()) {
                continue;
            }
            if (sub.getEndDate() != null && today.isAfter(sub.getEndDate())) {
                sub.expire();
                subscriptionRepository.save(sub);
                expiredCount++;
                log.debug("Expired subscription: id={}, userId={}, endDate={}",
                        sub.getUserFeatureSubscriptionId(), sub.getUserId(), sub.getEndDate());
            }
        }
        log.info("{} subscriptions expired.", expiredCount);
    }

    private void resetAiQuotas() {
        log.info("Resetting AI quotas for the new day...");
        // Reset both ACTIVE and EXHAUSTED subscriptions (if they are not expired)
        List<UserFeatureSubscription> activeSubs =
                subscriptionRepository.findByStatus(UserFeatureSubscriptionStatus.ACTIVE);
        List<UserFeatureSubscription> exhaustedSubs =
                subscriptionRepository.findByStatus(UserFeatureSubscriptionStatus.EXHAUSTED);
        
        int resetCount = 0;
        
        for (UserFeatureSubscription sub : activeSubs) {
            if (isAiSubscription(sub)) {
                sub.resetQuota();
                subscriptionRepository.save(sub);
                resetCount++;
            }
        }
        
        for (UserFeatureSubscription sub : exhaustedSubs) {
            if (isAiSubscription(sub)) {
                sub.resetQuota();
                subscriptionRepository.save(sub);
                resetCount++;
            }
        }
        
        log.info("Reset AI quotas for {} subscriptions.", resetCount);
    }

    private boolean isAiSubscription(UserFeatureSubscription sub) {
        return sub.getFeaturePackage() != null
                && com.sep.realvista.domain.billing.subscription.FeatureType.AI_REQUEST
                .equals(sub.getFeaturePackage().getFeatureType());
    }
}
