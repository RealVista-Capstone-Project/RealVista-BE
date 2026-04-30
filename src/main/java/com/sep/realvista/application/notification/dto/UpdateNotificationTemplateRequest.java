package com.sep.realvista.application.notification.dto;

import lombok.Data;

@Data
public class UpdateNotificationTemplateRequest {
    private String name;
    private String title;
    private String contentBody;
}
