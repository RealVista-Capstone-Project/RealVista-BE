package com.sep.realvista.application.notification.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TestSendRequest {
    private String type; // EMAIL or IN_APP
    private String title;
    private String contentBody;
    private Map<String, Object> mockData;
}
