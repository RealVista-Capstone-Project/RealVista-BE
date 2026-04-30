package com.sep.realvista.application.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationTemplateResponse {
    private UUID templateId;
    private String templateName;
    private String slug;
    private String subjectTemplate;
    private String body_template;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
