package com.sep.realvista.unit.application.notification;

import com.sep.realvista.application.notification.dto.RegisterDeviceTokenRequest;
import com.sep.realvista.application.notification.service.DeviceTokenApplicationService;
import com.sep.realvista.domain.user.notification.DeviceToken;
import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import com.sep.realvista.domain.user.notification.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenApplicationServiceTest {

    @Mock DeviceTokenRepository deviceTokenRepository;

    DeviceTokenApplicationService service;

    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new DeviceTokenApplicationService(deviceTokenRepository);
    }

    /** Build a DeviceToken with an explicit createdAt via reflection (BaseEntity field). */
    private DeviceToken tokenWithCreatedAt(LocalDateTime createdAt) throws Exception {
        DeviceToken token = DeviceToken.builder()
                .userId(userId)
                .fcmToken(UUID.randomUUID().toString())
                .deviceType(DeviceType.ANDROID)
                .build();
        // BaseEntity.createdAt is private — set via reflection
        Field f = token.getClass().getSuperclass().getDeclaredField("createdAt");
        f.setAccessible(true);
        f.set(token, createdAt);
        return token;
    }

    @Test
    @DisplayName("registerDeviceToken deactivates oldest tokens when count exceeds 20")
    void registerDeviceToken_enforcesCapOf20() throws Exception {
        // Simulate 21 active tokens already present after upsert
        List<DeviceToken> existing = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            existing.add(tokenWithCreatedAt(LocalDateTime.now().minusDays(21 - i)));
        }
        RegisterDeviceTokenRequest req = RegisterDeviceTokenRequest.builder()
                .fcmToken("new-token")
                .deviceType(DeviceType.ANDROID)
                .build();
        when(deviceTokenRepository.findByUserIdAndFcmToken(userId, "new-token"))
                .thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(existing);

        service.registerDeviceToken(userId, req);

        // The oldest token (index 0, createdAt = now-21days) should be deactivated
        DeviceToken oldest = existing.get(0);
        assertThat(oldest.getActive()).isFalse();

        // save() called once for new token + once for the deactivated token = 2
        verify(deviceTokenRepository, times(2)).save(any(DeviceToken.class));
    }

    @Test
    @DisplayName("registerDeviceToken does NOT deactivate when count is exactly 20")
    void registerDeviceToken_noDeactivationAt20() throws Exception {
        List<DeviceToken> existing = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            existing.add(tokenWithCreatedAt(LocalDateTime.now().minusDays(20 - i)));
        }
        RegisterDeviceTokenRequest req = RegisterDeviceTokenRequest.builder()
                .fcmToken("new-token-20")
                .deviceType(DeviceType.ANDROID)
                .build();
        when(deviceTokenRepository.findByUserIdAndFcmToken(userId, "new-token-20"))
                .thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(deviceTokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(existing);

        service.registerDeviceToken(userId, req);

        // Only the save for the new token itself — no deactivation saves
        verify(deviceTokenRepository, times(1)).save(any(DeviceToken.class));
    }
}
