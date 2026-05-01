package com.sep.realvista.application.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationTemplateResponse {
    @JsonProperty("template_id")
    private UUID templateId;
    @JsonProperty("template_key")
    private String templateKey;
    private String name;
    private String type;
    private String language;
    private String title;
    @JsonProperty("content_body")
    private String contentBody;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
