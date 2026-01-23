package com.sep.realvista.presentation.rest.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class NotificationController {

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendNotification(
            @RequestBody Map<String, String> request) {

        try {
            String token = request.get("token");
            String title = request.get("title");
            String body = request.get("body");

            // Build notification
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // Send notification
            String response = FirebaseMessaging.getInstance().send(message);

            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "messageId", response
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", "false",
                    "error", e.getMessage()
            ));
        }
    }
}