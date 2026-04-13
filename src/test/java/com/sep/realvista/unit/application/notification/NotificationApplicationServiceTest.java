package com.sep.realvista.unit.application.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.FirebaseNotificationService;
import com.sep.realvista.domain.user.notification.DeliveryStatus;
import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import com.sep.realvista.domain.user.notification.Notification;
import com.sep.realvista.domain.user.notification.NotificationRepository;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock DeviceTokenRepository deviceTokenRepository;
    @Mock FirebaseNotificationService firebaseNotificationService;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock SettingPreferenceRepository settingPreferenceRepository;

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
                objectMapper
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
}
