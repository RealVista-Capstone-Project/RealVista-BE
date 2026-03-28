package com.sep.realvista.infrastructure.external.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.sep.realvista.application.service.FirebaseNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

    @Override
    @Async
    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (!isAvailable()) {
            log.warn("Firebase is not available. Skipping push notification to token: {}...",
                    fcmToken.substring(0, Math.min(10, fcmToken.length())));
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Successfully sent FCM notification. Response: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification to token {}...: {}",
                    fcmToken.substring(0, Math.min(10, fcmToken.length())), e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendNotificationToMultipleDevices(
            List<String> fcmTokens, String title,
            String body, Map<String, String> data) {
        if (!isAvailable()) {
            log.warn("Firebase is not available. Skipping push notification to {} devices.", fcmTokens.size());
            return;
        }

        if (fcmTokens.isEmpty()) {
            log.debug("No FCM tokens provided. Skipping multicast notification.");
            return;
        }

        try {
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(messageBuilder.build());
            log.info("Multicast FCM notification sent. Success: {}, Failure: {}",
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                response.getResponses().forEach(sendResponse -> {
                    if (!sendResponse.isSuccessful()) {
                        log.warn("Failed to send to a device: {}",
                                sendResponse.getException() != null
                                        ? sendResponse.getException().getMessage()
                                        : "Unknown error");
                    }
                });
            }
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast FCM notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            return !FirebaseApp.getApps().isEmpty();
        } catch (Exception e) {
            log.debug("Firebase availability check failed: {}", e.getMessage());
            return false;
        }
    }
}
