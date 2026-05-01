package com.sep.realvista.application.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateNotificationTemplateRequest {
    @JsonProperty("template_key")
    private String templateKey;
    private String name;
    private String type;
    private String language;
    private String title;
    @JsonProperty("content_body")
    private String contentBody;
}

