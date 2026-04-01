package com.sep.realvista.application.notification.service;

import com.sep.realvista.application.notification.dto.RegisterDeviceTokenRequest;
import com.sep.realvista.domain.user.notification.DeviceToken;
import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for managing FCM device tokens.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeviceTokenApplicationService {

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Register or update a device token for a user.
     * If the same FCM token already exists for the user, reactivates and updates it.
     */
    public void registerDeviceToken(UUID userId, RegisterDeviceTokenRequest request) {
        log.info("Registering device token for user: {}, deviceType: {}", userId, request.getDeviceType());

        Optional<DeviceToken> existing = deviceTokenRepository.findByUserIdAndFcmToken(
                userId, request.getFcmToken());

        if (existing.isPresent()) {
            DeviceToken token = existing.get();
            token.activate();
            token.updateLastUsed();
            deviceTokenRepository.save(token);
            log.info("Reactivated existing device token for user: {}", userId);
        } else {
            DeviceToken token = DeviceToken.builder()
                    .userId(userId)
                    .fcmToken(request.getFcmToken())
                    .deviceType(request.getDeviceType())
                    .deviceName(request.getDeviceName())
                    .lastUsedAt(LocalDateTime.now())
                    .build();
            deviceTokenRepository.save(token);
            log.info("Registered new device token for user: {}", userId);
        }
    }

    /**
     * Unregister a device token (e.g., on logout).
     */
    public void unregisterDeviceToken(UUID userId, String fcmToken) {
        log.info("Unregistering device token for user: {}", userId);
        Optional<DeviceToken> existing = deviceTokenRepository.findByUserIdAndFcmToken(userId, fcmToken);
        if (existing.isPresent()) {
            existing.get().deactivate();
            deviceTokenRepository.save(existing.get());
            log.info("Deactivated device token for user: {}", userId);
        } else {
            log.warn("Device token not found for user: {}", userId);
        }
    }

    /**
     * Deactivate all device tokens for a user (e.g., on password change).
     */
    public void deactivateAllTokens(UUID userId) {
        log.info("Deactivating all device tokens for user: {}", userId);
        deviceTokenRepository.deactivateAllByUserId(userId);
    }
}
