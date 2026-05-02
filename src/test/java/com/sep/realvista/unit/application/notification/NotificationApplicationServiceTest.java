package com.sep.realvista.unit.application.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.FirebaseNotificationService;
import com.sep.realvista.domain.user.notification.DeliveryStatus;
import com.sep.realvista.domain.user.notification.DeviceToken;
import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import com.sep.realvista.domain.user.notification.DeviceType;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import com.sep.realvista.domain.user.notification.Notification;
import com.sep.realvista.domain.user.notification.NotificationRepository;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.domain.common.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.sep.realvista.domain.user.notification.NotificationTemplateRepository;
import com.sep.realvista.application.service.TemplateEngineService;
import com.sep.realvista.domain.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock DeviceTokenRepository deviceTokenRepository;
    @Mock FirebaseNotificationService firebaseNotificationService;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock SettingPreferenceRepository settingPreferenceRepository;
    @Mock NotificationTemplateRepository templateRepository;
    @Mock TemplateEngineService templateEngineService;
    @Mock UserRepository userRepository;

    // ObjectMapper is a real instance — no need to mock it
    ObjectMapper objectMapper = new ObjectMapper();

    NotificationApplicationService service;

    UUID userId = UUID.randomUUID();
    String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        service = new NotificationApplicationService(
                notificationRepository,
                deviceTokenRepository,
                firebaseNotificationService,
                messagingTemplate,
                settingPreferenceRepository,
                objectMapper,
                templateRepository,
                templateEngineService,
                userRepository
        );
    }

    private SendNotificationRequest buildRequest() {
        return SendNotificationRequest.builder()
                .userId(userId)
                .userEmail(userEmail)
                .title("Test Title")
                .message("Test message")
                .eventType(EventType.NEW_MESSAGE)
                .entityType(EntityType.LISTING)
                .entityId(UUID.randomUUID())
                .build();
    }

    private Notification savedNotification(SendNotificationRequest req) {
        return Notification.builder()
                .userId(req.getUserId())
                .title(req.getTitle())
                .message(req.getMessage())
                .eventType(req.getEventType())
                .entityType(req.getEntityType())
                .entityId(req.getEntityId())
                .build();
    }

    // -- Task 1: double-save -------------------------------------------------------

    @Test
    @DisplayName("sendNotification calls notificationRepository.save exactly once")
    void sendNotification_savesExactlyOnce() {
        SendNotificationRequest req = buildRequest();
        Notification n = savedNotification(req);
        when(notificationRepository.save(any())).thenReturn(n);
        when(settingPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(SettingPreference.builder().userId(userId).build()));
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of());

        service.sendNotification(req);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("sendNotification persists notification with SENT delivery status")
    void sendNotification_persistsAsSent() {
        SendNotificationRequest req = buildRequest();
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(settingPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(SettingPreference.builder().userId(userId).build()));
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of());

        service.sendNotification(req);

        Notification persisted = captor.getValue();
        assertThat(persisted.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
    }

    // -- Task 1: ObjectMapper ------------------------------------------------------

    @Test
    @DisplayName("sendNotification serializes metadata using ObjectMapper (valid JSON)")
    void sendNotification_metadataIsValidJson() throws Exception {
        Map<String, String> meta = Map.of("key", "val\"ue");
        SendNotificationRequest req = SendNotificationRequest.builder()
                .userId(userId)
                .userEmail(userEmail)
                .title("T")
                .message("M")
                .eventType(EventType.SYSTEM)
                .metadata(meta)
                .build();
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(settingPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(SettingPreference.builder().userId(userId).build()));
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of());

        service.sendNotification(req);

        String metadataJson = captor.getValue().getMetadata();
        // must parse without exception
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = objectMapper.readValue(metadataJson, Map.class);
        assertThat(parsed).containsKey("key");
        assertThat(parsed.get("key")).isEqualTo("val\"ue");
    }

    // ── Task 5: SettingPreference gates ─────────────────────────────────────

    @Test
    @DisplayName("sendNotification skips WebSocket when inAppEnabled is false")
    void sendNotification_skipsWebSocketWhenInAppDisabled() {
        SendNotificationRequest req = buildRequest();
        Notification n = savedNotification(req);
        when(notificationRepository.save(any())).thenReturn(n);
        SettingPreference prefs = SettingPreference.builder()
                .userId(userId)
                .inAppEnabled(false)
                .pushEnabled(true)
                .build();
        when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of());

        service.sendNotification(req);

        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    @DisplayName("sendNotification skips FCM when pushEnabled is false")
    void sendNotification_skipsFcmWhenPushDisabled() {
        SendNotificationRequest req = buildRequest();
        Notification n = savedNotification(req);
        when(notificationRepository.save(any())).thenReturn(n);
        SettingPreference prefs = SettingPreference.builder()
                .userId(userId)
                .inAppEnabled(true)
                .pushEnabled(false)
                .build();
        when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));

        service.sendNotification(req);

        verify(firebaseNotificationService, never())
                .sendNotificationToMultipleDevices(any(), any(), any(), any());
    }

    @Test
    @DisplayName("sendNotification uses default prefs (both enabled) when preference not found")
    void sendNotification_defaultsToAllEnabledWhenPrefMissing() {
        SendNotificationRequest req = buildRequest();
        Notification n = savedNotification(req);
        when(notificationRepository.save(any())).thenReturn(n);
        when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of());

        service.sendNotification(req);

        // WS should fire even with no preference record (defaults: inAppEnabled=true)
        verify(messagingTemplate).convertAndSendToUser(eq(userEmail), eq("/queue/notifications"), any());
    }

    // ── Task 6: FCM data keys snake_case ────────────────────────────────────

    @Test
    @DisplayName("sendFirebasePushNotification uses snake_case data keys")
    void sendNotification_fcmDataKeysAreSnakeCase() {
        UUID entityId = UUID.randomUUID();
        SendNotificationRequest req = SendNotificationRequest.builder()
                .userId(userId)
                .userEmail(userEmail)
                .title("T")
                .message("M")
                .eventType(EventType.NEW_LISTING)
                .entityType(EntityType.LISTING)
                .entityId(entityId)
                .build();
        Notification n = savedNotification(req);
        when(notificationRepository.save(any())).thenReturn(n);
        when(settingPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(SettingPreference.builder()
                        .userId(userId).inAppEnabled(false).pushEnabled(true).build()));

        DeviceToken token = DeviceToken.builder()
                .userId(userId)
                .fcmToken("tok123")
                .deviceType(DeviceType.ANDROID)
                .build();
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of(token));

        service.sendNotification(req);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(firebaseNotificationService).sendNotificationToMultipleDevices(
                any(), any(), any(), dataCaptor.capture());
        Map<String, String> data = dataCaptor.getValue();
        assertThat(data).containsKey("event_type")
                        .containsKey("entity_type")
                        .containsKey("entity_id")
                        .doesNotContainKey("eventType")
                        .doesNotContainKey("entityType")
                        .doesNotContainKey("entityId");
        assertThat(data.get("event_type")).isEqualTo("NEW_LISTING");
        assertThat(data.get("entity_type")).isEqualTo("LISTING");
        assertThat(data.get("entity_id")).isEqualTo(entityId.toString());
    }

    // ── Task 7: deleteNotification ───────────────────────────────────────────

    @Test
    @DisplayName("deleteNotification marks notification as deleted and saves")
    void deleteNotification_marksDeletedAndSaves() {
        UUID notifId = UUID.randomUUID();
        Notification n = Notification.builder()
                .userId(userId)
                .title("T")
                .message("M")
                .eventType(EventType.SYSTEM)
                .build();
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.deleteNotification(notifId, userId);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
    }

    @Test
    @DisplayName("deleteNotification throws when notification belongs to different user")
    void deleteNotification_throwsForWrongUser() {
        UUID notifId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        Notification n = Notification.builder()
                .userId(otherUser)
                .title("T")
                .message("M")
                .eventType(EventType.SYSTEM)
                .build();
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(n));

        org.junit.jupiter.api.Assertions.assertThrows(DomainException.class,
                () -> service.deleteNotification(notifId, userId));

        verify(notificationRepository, never()).save(any());
    }
}
