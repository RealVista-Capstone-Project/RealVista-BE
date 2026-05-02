package com.sep.realvista.application.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateNotificationTemplateRequest {
    private String name;
    private String title;
    @JsonProperty("content_body")
    private String contentBody;
}

