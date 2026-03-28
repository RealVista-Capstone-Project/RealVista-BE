package com.sep.realvista.application.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Streams AI chat responses from the NestJS AI microservice via SSE.
 * Uses WebClient (reactive) to proxy the SSE stream, forwarding
 * authenticated user identity through internal headers.
 */
@Slf4j
@Service
public class AiChatApplicationService {

    private final WebClient aiWebClient;
    private final String serviceApiKey;

    public AiChatApplicationService(
            WebClient aiWebClient,
            @Value("${realvista.ai.api-key:}") String serviceApiKey
    ) {
        this.aiWebClient = aiWebClient;
        this.serviceApiKey = serviceApiKey;
    }

    /**
     * Stream chat responses from the AI service as Server-Sent Events.
     *
     * @param message   the user's chat message
     * @param threadId  optional thread ID for conversation continuity
     * @param userId    authenticated user's UUID
     * @param userName  authenticated user's email/name
     * @param userRoles comma-separated role list
     * @return a Flux of raw SSE strings from the AI service
     */
    public Flux<String> streamChat(String message,
                                   String threadId,
                                   String userId,
                                   String userName,
                                   String userRoles) {
        log.info("Streaming AI chat for user={}, threadId={}", userId, threadId);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("prompt", message);
        if (threadId != null && !threadId.isBlank()) {
            body.put("threadId", threadId);
        }

        return aiWebClient.post()
                .uri("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("x-api-key", serviceApiKey)
                .header("x-user-id", userId)
                .header("x-user-name", userName != null ? userName : "unknown")
                .header("x-user-roles", userRoles != null ? userRoles : "USER")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("AI service error: status={}, body={}",
                            ex.getStatusCode(), ex.getResponseBodyAsString());
                    String errorEvent = buildErrorEvent(
                            "AI service error: " + ex.getStatusCode());
                    return Flux.just(errorEvent);
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected error streaming AI chat: {}",
                            ex.getMessage(), ex);
                    String errorEvent = buildErrorEvent(
                            "Internal server error");
                    return Flux.just(errorEvent);
                });
    }

    private String buildErrorEvent(String message) {
        return "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
    }
}
