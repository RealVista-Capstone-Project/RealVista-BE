package com.sep.realvista.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class H2FunctionAliases {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * H2 alias for PostgreSQL jsonb_extract_path_text function
     */
    public static String jsonb_extract_path_text(String jsonObject, String path) {
        if (jsonObject == null || path == null) return null;
        
        try {
            JsonNode node = mapper.readTree(jsonObject);
            JsonNode value = node.path(path);
            return value.isMissingNode() ? null : value.asText();
        } catch (Exception e) {
            return null;
        }
    }
}