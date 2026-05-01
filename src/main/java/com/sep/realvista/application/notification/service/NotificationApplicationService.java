package com.sep.realvista.application.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.common.dto.PageResponse;
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
import com.sep.realvista.domain.common.exception.DomainException;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.application.service.TemplateEngineService;
import com.sep.realvista.domain.user.notification.NotificationTemplate;
import com.sep.realvista.domain.user.notification.NotificationTemplateRepository;
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
    private final SettingPreferenceRepository settingPreferenceRepository;
    private final ObjectMapper objectMapper;
    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngineService templateEngineService;
    private final UserRepository userRepository;

    /**
     * Send a notification to a user via all channels.
     * Preference gates: in-app (WS) only if inAppEnabled; push (FCM) only if pushEnabled.
     */
    public void sendNotification(SendNotificationRequest request) {
        log.info("Sending notification to user {} - type: {}, title: {}",
                request.getUserId(), request.getEventType(), request.getTitle());

        SettingPreference prefs = settingPreferenceRepository
                .findByUserId(request.getUserId())
                .orElse(SettingPreference.builder().userId(request.getUserId()).build());

        // 1. Persist notification — markAsSent() BEFORE save (single save, no double-save)
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
        notification.markAsSent();
        Notification saved = notificationRepository.save(notification);

        // 2. Build response DTO for WebSocket
        NotificationResponse response = toResponse(saved);

        // 3. Push via WebSocket (real-time in-app) — gated by inAppEnabled
        if (Boolean.TRUE.equals(prefs.getInAppEnabled())) {
            sendWebSocketNotification(request.getUserEmail(), response);
        } else {
            log.debug("In-app notifications disabled for user {}. Skipping WebSocket.", request.getUserId());
        }

        // 4. Push via Firebase FCM — gated by pushEnabled
        if (Boolean.TRUE.equals(prefs.getPushEnabled())) {
            sendFirebasePushNotification(
                    request.getUserId(), request.getTitle(),
                    request.getMessage(), request.getEventType(),
                    request.getEntityType(), request.getEntityId(),
                    request.getMetadata()
            );
        } else {
            log.debug("Push notifications disabled for user {}. Skipping FCM.", request.getUserId());
        }

        log.info("Notification sent successfully to user {} via enabled channels", request.getUserId());
    }

    /**
     * Send a notification using a database template.
     */
    public void sendDbNotification(UUID userId, String templateKey, String language, Map<String, Object> variables,
                                  EventType eventType, EntityType entityType, UUID entityId) {
        NotificationTemplate template = templateRepository.findByTemplateKeyAndLanguage(templateKey, language)
                .orElseThrow(() -> new DomainException("Notification template not found: " + templateKey, 
                        "ERROR_TEMPLATE_NOT_FOUND"));

        // Get user email for WebSocket
        com.sep.realvista.domain.user.User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found: " + userId, "ERROR_USER_NOT_FOUND"));

        TemplateEngineService.RenderedTemplate rendered = templateEngineService.preview(
                template.getTitle(), template.getContentBody(), variables);

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .userEmail(user.getEmail().getValue())
                .title(rendered.title())
                .message(rendered.body())
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(new HashMap<>()) // Default empty metadata
                .build();

        sendNotification(request);
    }

    /** Get paginated notifications for a user. */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
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

    /** Get unread notification count for a user. */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndDeletedFalse(userId);
    }

    /** Mark a single notification as read. */
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new DomainException(
                        "Notification not found: " + notificationId,
                        "ERROR_NOTIFICATION_NOT_FOUND"));

        if (!notification.getUserId().equals(userId)) {
            throw new DomainException("Notification does not belong to this user", "ERROR_NOTIFICATION_NOT_OWNED");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /** Mark all notifications as read for a user. */
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    /** Soft-delete a notification (sets deleted = true via BaseEntity.markAsDeleted()). */
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new DomainException(
                        "Notification not found: " + notificationId,
                        "ERROR_NOTIFICATION_NOT_FOUND"));

        if (!notification.getUserId().equals(userId)) {
            throw new DomainException("Notification does not belong to this user", "ERROR_NOTIFICATION_NOT_OWNED");
        }

        notification.markAsDeleted();
        notificationRepository.save(notification);
    }

    // ============================================================================
    // Private helpers
    // ============================================================================

    private void sendWebSocketNotification(String userEmail, NotificationResponse response) {
        try {
            messagingTemplate.convertAndSendToUser(userEmail, "/queue/notifications", response);
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
            data.put("event_type", eventType.name());
            if (entityType != null) {
                data.put("entity_type", entityType.name());
            }
            if (entityId != null) {
                data.put("entity_id", entityId.toString());
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
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata to JSON: {}", e.getMessage());
            return null;
        }
    }
}
