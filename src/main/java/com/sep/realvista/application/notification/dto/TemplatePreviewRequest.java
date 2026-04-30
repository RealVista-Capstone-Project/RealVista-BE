package com.sep.realvista.application.notification.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TemplatePreviewRequest {
    private String subjectTemplate;
    private String body_template;
    private Map<String, Object> testVariables;
}
