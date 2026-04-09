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
    public void expireExpiredSubscriptions() {
        log.info("Starting subscription expiration check...");

        List<UserFeatureSubscription> activeSubscriptions =
                subscriptionRepository.findByStatus(UserFeatureSubscriptionStatus.ACTIVE);

        LocalDate today = LocalDate.now();
        int expiredCount = 0;

        for (UserFeatureSubscription sub : activeSubscriptions) {
            if (sub.getEndDate() != null && today.isAfter(sub.getEndDate())) {
                sub.expire();
                subscriptionRepository.save(sub);
                expiredCount++;
                log.debug("Expired subscription: id={}, userId={}, endDate={}",
                        sub.getUserFeatureSubscriptionId(), sub.getUserId(), sub.getEndDate());
            }
        }

        log.info("Subscription expiration check completed. {} subscriptions expired.", expiredCount);
    }
}
