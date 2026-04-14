package com.sep.realvista.domain.user.notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository {

    DeviceToken save(DeviceToken deviceToken);

    Optional<DeviceToken> findById(UUID id);

    List<DeviceToken> findByUserIdAndActiveTrue(UUID userId);

    Optional<DeviceToken> findByUserIdAndFcmToken(UUID userId, String fcmToken);

    void deleteByUserIdAndFcmToken(UUID userId, String fcmToken);

    void deactivateAllByUserId(UUID userId);

    /** Bulk-deactivate tokens by their FCM token strings (used for stale token cleanup). */
    void deactivateByFcmTokenIn(List<String> fcmTokens);
}
