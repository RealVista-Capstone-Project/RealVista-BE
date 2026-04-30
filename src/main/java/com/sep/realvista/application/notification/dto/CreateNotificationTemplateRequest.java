package com.sep.realvista.application.notification.dto;

import lombok.Data;

@Data
public class CreateNotificationTemplateRequest {
    private String templateKey;
    private String name;
    private String type;
    private String language;
    private String title;
    private String contentBody;
}
