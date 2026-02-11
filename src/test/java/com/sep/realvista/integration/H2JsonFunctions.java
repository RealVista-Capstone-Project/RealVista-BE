package com.sep.realvista.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class H2JsonFunctions {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String jsonbExtractPathText(String json, String key) {
        if (json == null || key == null) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode value = root.get(key);
            if (value != null) {
                 return value.asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
