package com.sep.realvista.infrastructure.external.marble;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MarbleClient {

    private final RestTemplate restTemplate;
    private final String marbleBaseUrl;
    private final String apiKey;

    public MarbleClient(
            RestTemplate restTemplate,
            @Value("${realvista.marble.api-url:https://api.worldlabs.ai/marble/v1}") String marbleBaseUrl,
            @Value("${realvista.marble.api-key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.marbleBaseUrl = marbleBaseUrl;
        this.apiKey = apiKey;

        if (this.apiKey == null || this.apiKey.isBlank()) {
            log.error("MARBLE_API_KEY is not configured! 3D generation and status polling will fail.");
        }
    }

    public JsonNode getOperation(String operationId) {
        String url = marbleBaseUrl + "/operations/" + operationId;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("WLT-Api-Key", apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            log.warn("Marble API returned non-2xx for operation {}: {}", operationId, response.getStatusCode());
        } catch (RestClientException e) {
            log.error("Failed to call Marble API get operation {}: {}", operationId, e.getMessage());
        }
        return null;
    }
    public JsonNode generateWorld(MarbleGenerateRequest request) {
        String url = marbleBaseUrl + "/generate";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("WLT-Api-Key", apiKey);
            HttpEntity<MarbleGenerateRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            log.warn("Marble API returned non-2xx for generateWorld: {}", response.getStatusCode());
        } catch (RestClientException e) {
            log.error("Failed to call Marble API generateWorld: {}", e.getMessage());
            throw new RuntimeException("Marble Generate API failed", e);
        }
        return null;
    }
}
