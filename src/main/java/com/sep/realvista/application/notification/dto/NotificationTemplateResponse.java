package com.sep.realvista.application.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationTemplateResponse {
    private UUID templateId;
    private String templateKey;
    private String name;
    private String type;
    private String language;
    private String title;
    private String contentBody;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
