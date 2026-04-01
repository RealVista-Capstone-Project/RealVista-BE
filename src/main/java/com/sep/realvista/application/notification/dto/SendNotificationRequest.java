package com.sep.realvista.application.notification.dto;

import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Internal request object for sending notifications across all channels.
 */
@Getter
@Builder
public class SendNotificationRequest {

    private final UUID userId;
    private final String userEmail;
    private final String title;
    private final String message;
    private final EventType eventType;
    private final EntityType entityType;
    private final UUID entityId;
    private final Map<String, String> metadata;
}
