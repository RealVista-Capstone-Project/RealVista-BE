package com.sep.realvista.application.notification.service;

import com.sep.realvista.application.notification.dto.NotificationResponse;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.service.FirebaseNotificationService;
import com.sep.realvista.domain.user.notification.DeviceToken;
import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import com.sep.realvista.domain.user.notification.Notification;
import com.sep.realvista.domain.user.notification.NotificationChannel;
import com.sep.realvista.domain.user.notification.NotificationRepository;
import com.sep.realvista.application.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service that orchestrates notification persistence,
 * WebSocket real-time delivery, and Firebase push notifications.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebaseNotificationService firebaseNotificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a notification to a user via all channels:
     * 1. Persist in DB (for notification history / inbox)
     * 2. Push via WebSocket (real-time in-app UI)
     * 3. Push via Firebase FCM (mobile + web push)
     */
    public void sendNotification(SendNotificationRequest request) {
        log.info("Sending notification to user {} - type: {}, title: {}",
                request.getUserId(), request.getEventType(), request.getTitle());

        // 1. Persist notification
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .channel(NotificationChannel.BOTH)
                .title(request.getTitle())
                .message(request.getMessage())
                .eventType(request.getEventType())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .metadata(request.getMetadata() != null
                        ? toJsonString(request.getMetadata()) : null)
                .build();

        Notification saved = notificationRepository.save(notification);
        saved.markAsSent();
        notificationRepository.save(saved);

        // 2. Build response DTO for WebSocket
        NotificationResponse response = toResponse(saved);

        // 3. Push via WebSocket (real-time in-app)
        sendWebSocketNotification(request.getUserEmail(), response);

        // 4. Push via Firebase FCM (mobile + web push)
        sendFirebasePushNotification(
                request.getUserId(), request.getTitle(),
                request.getMessage(), request.getEventType(),
                request.getEntityType(), request.getEntityId(),
                request.getMetadata()
        );

        log.info("Notification sent successfully to user {} via all channels",
                request.getUserId());
    }

    /**
     * Get paginated notifications for a user.
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(
            UUID userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository
                .findByUserIdAndDeletedFalse(userId, pageable)
                .map(this::toResponse);

        return PageResponse.<NotificationResponse>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * Get unread notification count for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndDeletedFalse(userId);
    }

    /**
     * Mark a single notification as read.
     */
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to this user");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user.
     */
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // ============================================================================
    // Private helpers
    // ============================================================================

    private void sendWebSocketNotification(String userEmail, NotificationResponse response) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    "/queue/notifications",
                    response
            );
            log.debug("WebSocket notification sent to user: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to {}: {}", userEmail, e.getMessage(), e);
        }
    }

    private void sendFirebasePushNotification(UUID userId, String title, String body,
                                               EventType eventType, EntityType entityType,
                                               UUID entityId, Map<String, String> extraData) {
        try {
            List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdAndActiveTrue(userId);

            if (deviceTokens.isEmpty()) {
                log.debug("No active device tokens for user {}. Skipping Firebase push.", userId);
                return;
            }

            List<String> fcmTokens = deviceTokens.stream()
                    .map(DeviceToken::getFcmToken)
                    .toList();

            Map<String, String> data = new HashMap<>();
            data.put("eventType", eventType.name());
            if (entityType != null) {
                data.put("entityType", entityType.name());
            }
            if (entityId != null) {
                data.put("entityId", entityId.toString());
            }
            if (extraData != null) {
                data.putAll(extraData);
            }

            firebaseNotificationService.sendNotificationToMultipleDevices(fcmTokens, title, body, data);
            log.debug("Firebase push notification dispatched to {} devices for user {}", fcmTokens.size(), userId);
        } catch (Exception e) {
            log.error("Failed to send Firebase push notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .eventType(notification.getEventType().name())
                .entityType(notification.getEntityType() != null ? notification.getEntityType().name() : null)
                .entityId(notification.getEntityId())
                .isRead(notification.getIsRead())
                .metadata(notification.getMetadata())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String toJsonString(Map<String, String> map) {
        try {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                        .append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to serialize metadata to JSON: {}", e.getMessage());
            return null;
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
