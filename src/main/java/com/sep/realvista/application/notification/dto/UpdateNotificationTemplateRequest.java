package com.sep.realvista.application.notification.dto;

import lombok.Data;

@Data
public class UpdateNotificationTemplateRequest {
    private String templateName;
    private String subjectTemplate;
    private String body_template;
    private String description;
    private Boolean isActive;
}
