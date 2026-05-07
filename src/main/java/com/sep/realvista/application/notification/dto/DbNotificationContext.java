package com.sep.realvista.application.notification.dto;

import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;

import java.util.Map;
import java.util.UUID;

/**
 * Bundles entity reference + optional client-side metadata for a template-driven
 * notification dispatch. Keeps {@code sendDbNotification} parameter count under
 * the project's checkstyle limit while still allowing callers to attach deep-link
 * metadata (e.g. a slug for a listing).
 */
public record DbNotificationContext(
        EventType eventType,
        EntityType entityType,
        UUID entityId,
        Map<String, String> metadata) {

    public static DbNotificationContext of(EventType eventType, EntityType entityType, UUID entityId) {
        return new DbNotificationContext(eventType, entityType, entityId, null);
    }

    public static DbNotificationContext of(
            EventType eventType, EntityType entityType, UUID entityId, Map<String, String> metadata) {
        return new DbNotificationContext(eventType, entityType, entityId, metadata);
    }
}
