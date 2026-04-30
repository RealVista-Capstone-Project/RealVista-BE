package com.sep.realvista.application.notification.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TestSendRequest {
    private String email;
    private Map<String, Object> variables;
}
