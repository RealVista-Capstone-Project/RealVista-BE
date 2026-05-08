package com.sep.realvista.infrastructure.scheduler;

import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.claim.PropertyClaim;
import com.sep.realvista.domain.property.claim.PropertyClaimRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler that runs nightly and handles two tasks:
 * <ol>
 *   <li>Marks properties as STALE if they have had no active listing for
 *       {@code realvista.property.stale-after-months} months (default: 6).</li>
 *   <li>Expires or escalates pending property claims that have passed their deadline.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyStaleScheduler {

    private final PropertyRepository propertyRepository;
    private final PropertyClaimRepository propertyClaimRepository;
    private final NotificationApplicationService notificationApplicationService;
    private final UserRepository userRepository;

    @Value("${realvista.property.stale-after-months:6}")
    private int staleAfterMonths;

    /** Run at 02:00 every day. */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void markStaleProperties() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(staleAfterMonths);
        List<Property> eligible = propertyRepository.findPropertiesEligibleForStale(cutoff);

        if (eligible.isEmpty()) {
            log.debug("PropertyStaleScheduler: no properties eligible for stale.");
            return;
        }

        log.info("PropertyStaleScheduler: marking {} properties as STALE", eligible.size());
        for (Property property : eligible) {
            property.markAsStale();
            propertyRepository.save(property);

            // Notify owner
            try {
                userRepository.findById(property.getOwnerId()).ifPresent(owner -> {
                    String email = owner.getEmail() != null ? owner.getEmail().getValue() : null;
                    notificationApplicationService.sendNotification(SendNotificationRequest.builder()
                            .userId(property.getOwnerId())
                            .userEmail(email)
                            .title("Bất động sản của bạn đã ngừng hoạt động")
                            .message("Bất động sản tại " + property.getStreetAddress()
                                    + " chưa có hoạt động trong hơn " + staleAfterMonths
                                    + " tháng và đã được đánh dấu ngừng hoạt động (STALE). "
                                    + "Đăng tin mới để kích hoạt lại.")
                            .eventType(EventType.SYSTEM)
                            .entityType(EntityType.PROPERTY)
                            .entityId(property.getPropertyId())
                            .build());
                });
            } catch (Exception e) {
                log.error("Failed to send stale notification for property {}: {}",
                        property.getPropertyId(), e.getMessage());
            }
        }
    }

    /** Run at 03:00 every day — expire/escalate overdue pending claims. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void processExpiredClaims() {
        List<PropertyClaim> expiredClaims =
                propertyClaimRepository.findExpiredPendingBefore(LocalDateTime.now());

        if (expiredClaims.isEmpty()) {
            log.debug("PropertyStaleScheduler: no expired claims to process.");
            return;
        }

        log.info("PropertyStaleScheduler: escalating {} expired claims", expiredClaims.size());
        for (PropertyClaim claim : expiredClaims) {
            claim.escalate();
            propertyClaimRepository.save(claim);

            // Notify the property owner that admin review is now required
            try {
                propertyRepository.findById(claim.getPropertyId()).ifPresent(property -> {
                    userRepository.findById(property.getOwnerId()).ifPresent(owner -> {
                        String email = owner.getEmail() != null ? owner.getEmail().getValue() : null;
                        notificationApplicationService.sendNotification(SendNotificationRequest.builder()
                                .userId(property.getOwnerId())
                                .userEmail(email)
                                .title("Yêu cầu claim bất động sản đã hết hạn")
                                .message("Yêu cầu claim bất động sản tại " + property.getStreetAddress()
                                        + " đã hết hạn phản hồi. Quản trị viên sẽ xem xét trường hợp này.")
                                .eventType(EventType.PROPERTY_CLAIM_ESCALATED)
                                .entityType(EntityType.PROPERTY)
                                .entityId(claim.getPropertyId())
                                .build());
                    });
                });
            } catch (Exception e) {
                log.error("Failed to send escalation notification for claim {}: {}",
                        claim.getClaimId(), e.getMessage());
            }
        }
    }
}
