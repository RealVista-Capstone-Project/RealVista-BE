package com.sep.realvista.application.service;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for Firebase Cloud Messaging push notification service.
 */
public interface FirebaseNotificationService {

    /**
     * Send a push notification to a single device.
     *
     * @param fcmToken  the FCM device token
     * @param title     notification title
     * @param body      notification body
     * @param data      additional data payload (key-value pairs)
     */
    void sendNotification(String fcmToken, String title, String body, Map<String, String> data);

    /**
     * Send push notifications to multiple devices.
     *
     * @param fcmTokens list of FCM device tokens
     * @param title     notification title
     * @param body      notification body
     * @param data      additional data payload (key-value pairs)
     */
    void sendNotificationToMultipleDevices(List<String> fcmTokens, String title, String body, Map<String, String> data);

    /**
     * Check if Firebase is available and properly configured.
     *
     * @return true if Firebase is initialized and ready
     */
    boolean isAvailable();
}
