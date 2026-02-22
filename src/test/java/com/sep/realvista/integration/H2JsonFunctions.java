package com.sep.realvista.integration;



public class H2JsonFunctions {
    /**
     * H2 implementation for PostgreSQL jsonb_extract_path_text
     * Usage: jsonb_extract_path_text(column, 'key')
     */
    private static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public static String jsonbExtractPathText(String json, String path) {
        if (json == null || path == null) {
            return null;
        }

        try {
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(json);
            
            // If the node is textual, it might be a double-wrapped JSON string
            if (node.isTextual()) {
                node = mapper.readTree(node.asText());
            }
            
            com.fasterxml.jackson.databind.JsonNode attributeNode = node.get(path);
            
            if (attributeNode == null || attributeNode.isNull()) {
                return null;
            }
            
            return attributeNode.asText();
        } catch (Exception e) {
            return null;
        }
    }
}
