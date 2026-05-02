package com.sep.realvista.application.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class TemplatePreviewRequest {
    private String title;
    @JsonProperty("contentBody")
    private String contentBody;
    @JsonProperty("mockData")
    private Map<String, Object> mockData;
}

