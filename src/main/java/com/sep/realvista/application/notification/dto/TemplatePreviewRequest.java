package com.sep.realvista.application.notification.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TemplatePreviewRequest {
    private String title;
    private String contentBody;
    private Map<String, Object> mockData;
}
