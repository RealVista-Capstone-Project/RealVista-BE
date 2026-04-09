package com.sep.realvista.infrastructure.scheduler;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingExpiryScheduler {

    private final ListingRepository listingRepository;
    private final NotificationApplicationService notificationApplicationService;
    private final UserRepository userRepository;

    @Value("${realvista.listing.max-lifetime-days:14}")
    private long maxLifetimeDays;
    
    @Value("${realvista.listing.expiry-warning-days:3}")
    private long expiryWarningDays;

    @Scheduled(cron = "${realvista.listing.expiry-cron:0 0 * * * *}")
    @Transactional
    public void expireStaleListings() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(maxLifetimeDays);
        log.info("Running listing expiry scheduler – cutoff: {}, maxLifetimeDays: {}", cutoff, maxLifetimeDays);

        List<Listing> expiredListings = listingRepository.findPublishedListingsPublishedBefore(cutoff);

        if (expiredListings.isEmpty()) {
            log.info("No expired listings found.");
            return;
        }

        log.info("Found {} listing(s) to expire.", expiredListings.size());

        int successCount = 0;
        int errorCount = 0;

        for (Listing listing : expiredListings) {
            try {
                log.info("Expiring listing [id={}, publishedAt={}]", listing.getListingId(), listing.getPublishedAt());
                listing.unpublish();
                listingRepository.save(listing);
                successCount++;
            } catch (Exception ex) {
                // Catch per-listing errors so a single bad record doesn't abort the whole batch.
                log.error("Failed to expire listing [id={}]: {}", listing.getListingId(), ex.getMessage(), ex);
                errorCount++;
            }
        }

        log.info("Listing expiry complete – expired: {}, errors: {}", successCount, errorCount);
    }

    @Scheduled(cron = "${realvista.listing.expiry-warning-cron:0 0 8 * * *}")
    @Transactional(readOnly = true)
    public void notifyExpiringListings() {
        long daysOldTarget = maxLifetimeDays - expiryWarningDays;
        // Window is exactly one day, so any listing falls in here exactly once
        LocalDateTime windowEnd = LocalDateTime.now().minusDays(daysOldTarget);
        LocalDateTime windowStart = windowEnd.minusDays(1);

        log.info("Running listing expiry warning scheduler – windowStart: {}, windowEnd: {}, warningDays: {}",
                windowStart, windowEnd, expiryWarningDays);

        List<Listing> expiringListings = listingRepository
                .findPublishedListingsPublishedBetween(windowStart, windowEnd);

        if (expiringListings.isEmpty()) {
            log.info("No soon-to-expire listings found.");
            return;
        }

        log.info("Found {} listing(s) expiring soon.", expiringListings.size());

        int successCount = 0;
        int errorCount = 0;

        for (Listing listing : expiringListings) {
            try {
                userRepository.findById(listing.getUserId()).ifPresent(user -> {
                    SendNotificationRequest request = SendNotificationRequest.builder()
                            .userId(user.getUserId())
                            .userEmail(user.getEmail() != null ? user.getEmail().getValue() : null)
                            .title("Tin đăng sắp hết hạn")
                            .message(String.format("Tin đăng '%s' của bạn sẽ hết hạn trong %d ngày tới.",
                                    listing.getName(), expiryWarningDays))
                            .eventType(EventType.LISTING_EXPIRING_SOON)
                            .entityType(EntityType.LISTING)
                            .entityId(listing.getListingId())
                            .metadata(Map.of("listingId", listing.getListingId().toString(),
                                             "slug", listing.getSlug()))
                            .build();

                    notificationApplicationService.sendNotification(request);
                    log.info("Sent expiry warning for listing [id={}] to user [id={}]",
                            listing.getListingId(), user.getUserId());
                });
                successCount++;
            } catch (Exception ex) {
                log.error("Failed to send expiry warning for listing [id={}]: {}",
                        listing.getListingId(), ex.getMessage(), ex);
                errorCount++;
            }
        }

        log.info("Listing expiry warning complete – notified: {}, errors: {}", successCount, errorCount);
    }
}
