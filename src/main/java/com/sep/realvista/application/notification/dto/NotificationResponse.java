package com.sep.realvista.application.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private UUID notificationId;
    private String title;
    private String message;
    private String eventType;
    private String entityType;
    private UUID entityId;
    private Boolean isRead;
    private String metadata;
    private LocalDateTime createdAt;
}
