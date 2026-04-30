package com.sep.realvista.application.notification.dto;

import lombok.Data;

@Data
public class CreateNotificationTemplateRequest {
    private String templateName;
    private String slug;
    private String subjectTemplate;
    private String body_template;
    private String description;
}
