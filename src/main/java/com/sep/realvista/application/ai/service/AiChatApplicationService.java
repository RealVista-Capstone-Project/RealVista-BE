package com.sep.realvista.application.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.ai.dto.AiChatMessageResponse;
import com.sep.realvista.application.ai.dto.AiConversationMessagesResponse;
import com.sep.realvista.domain.aichat.AiConversation;
import com.sep.realvista.domain.aichat.AiConversationRepository;
import com.sep.realvista.domain.aichat.AiMessage;
import com.sep.realvista.domain.aichat.AiMessageRepository;
import com.sep.realvista.domain.aichat.AiMessageRole;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages AI chat conversations with persistence.
 *
 * <p>Flow for streaming chat:
 * <ol>
 *   <li>Find-or-create the user's single AI conversation</li>
 *   <li>Persist the USER message (synchronous, within TX)</li>
 *   <li>POST to NestJS AI service with conversation threadId</li>
 *   <li>Stream SSE tokens; on complete save ASSISTANT message
 *       in a separate thread/TX via
 *       {@link AiChatPersistenceHelper}</li>
 * </ol>
 */
@Slf4j
@Service
public class AiChatApplicationService {

    private static final ParameterizedTypeReference<
            ServerSentEvent<String>> SSE_TYPE =
            new ParameterizedTypeReference<>() { };

    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;
    private final String serviceApiKey;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final AiChatPersistenceHelper persistenceHelper;

    public AiChatApplicationService(
            WebClient aiWebClient,
            ObjectMapper objectMapper,
            @Value("${realvista.ai.api-key:}") String serviceApiKey,
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository,
            AiChatPersistenceHelper persistenceHelper
    ) {
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
        this.serviceApiKey = serviceApiKey;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.persistenceHelper = persistenceHelper;
    }

    /**
     * Stream an AI chat response, persisting both the user message
     * and the final assistant response.
     *
     * @return Flux of raw SSE strings from the AI service
     */
    @Transactional
    public Flux<String> streamChat(String message,
                                   UUID userId,
                                   String userName,
                                   String userRoles) {
        log.info("AI chat request from user={}", userId);

        // 1. Find or create conversation
        AiConversation conversation = conversationRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    AiConversation c = AiConversation.create(userId);
                    log.info("Created AI conversation user={}, "
                            + "threadId={}", userId, c.getThreadId());
                    return conversationRepository.save(c);
                });

        // 2. Save the USER message
        int nextSeq = messageRepository
                .countByConversationId(conversation.getId()) + 1;
        AiMessage userMsg = AiMessage.create(
                conversation.getId(), AiMessageRole.USER,
                message, nextSeq);
        messageRepository.save(userMsg);

        conversation.touchUpdatedAt();
        conversationRepository.save(conversation);

        // 3. Build request body for the AI service
        Map<String, Object> body = new HashMap<>();
        body.put("prompt", message);
        body.put("threadId", conversation.getThreadId().toString());

        // 4. Emit start event, then stream AI tokens
        String startEvent = buildStartEvent(conversation);
        int assistantSeq = nextSeq + 1;
        UUID convId = conversation.getId();

        // Accumulate token content as fallback;
        // prefer fullResponse from the done event.
        StringBuilder tokenAccumulator = new StringBuilder();
        AtomicReference<String> doneFullResponse =
                new AtomicReference<>();
        AtomicBoolean hasError = new AtomicBoolean(false);

        Flux<String> aiStream = aiWebClient.post()
                .uri("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("x-api-key", serviceApiKey)
                .header("x-user-id", userId.toString())
                .header("x-user-name",
                        userName != null ? userName : "unknown")
                .header("x-user-roles",
                        userRoles != null ? userRoles : "USER")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(SSE_TYPE)
                .mapNotNull(sse -> processSseEvent(
                        sse, tokenAccumulator,
                        doneFullResponse, hasError))
                .doOnComplete(() -> {
                    if (hasError.get()) {
                        return;
                    }
                    String content = doneFullResponse.get();
                    if (content == null || content.isBlank()) {
                        content = tokenAccumulator.toString();
                    }
                    persistenceHelper.saveAssistantMessage(
                            convId, content, assistantSeq);
                })
                .onErrorResume(
                        WebClientResponseException.class, ex -> {
                    log.error("AI service error: status={}, body={}",
                            ex.getStatusCode(),
                            ex.getResponseBodyAsString());
                    return Flux.just(buildErrorEvent(
                            "AI service error: "
                                    + ex.getStatusCode()));
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected streaming error: {}",
                            ex.getMessage(), ex);
                    return Flux.just(
                            buildErrorEvent("Internal server error"));
                });

        return Flux.concat(Flux.just(startEvent), aiStream);
    }

    /**
     * Returns all messages for the user's AI conversation.
     */
    @Transactional(readOnly = true)
    public AiConversationMessagesResponse getMessages(UUID userId) {
        AiConversation conversation = conversationRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AI conversation", userId));

        List<AiMessage> messages = messageRepository
                .findByConversationIdOrderBySequence(
                        conversation.getId());

        List<AiChatMessageResponse> dtos = messages.stream()
                .map(this::toDto)
                .toList();

        return AiConversationMessagesResponse.builder()
                .conversationId(conversation.getId())
                .threadId(conversation.getThreadId())
                .createdAt(conversation.getCreatedAt())
                .messages(dtos)
                .build();
    }

    /**
     * Deletes the user's AI conversation and notifies the AI service
     * to clean up the thread.
     */
    @Transactional
    public void deleteConversation(UUID userId,
                                   String userName,
                                   String userRoles) {
        AiConversation conversation = conversationRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AI conversation", userId));

        UUID threadId = conversation.getThreadId();

        // Hard-delete locally (CASCADE removes messages)
        conversationRepository.delete(conversation);
        log.info("Deleted AI conversation user={}, threadId={}",
                userId, threadId);

        // Fire-and-forget: tell NestJS to clean up the thread
        aiWebClient.delete()
                .uri("/ai/threads/{threadId}", threadId)
                .header("x-api-key", serviceApiKey)
                .header("x-user-id", userId.toString())
                .header("x-user-name",
                        userName != null ? userName : "unknown")
                .header("x-user-roles",
                        userRoles != null ? userRoles : "USER")
                .retrieve()
                .toBodilessEntity()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(ex -> log.warn(
                        "Failed to delete AI thread {}: {}",
                        threadId, ex.getMessage()))
                .subscribe();
    }

    // ── private helpers ─────────────────────────────────────────

    /**
     * Processes a single SSE event from the NestJS AI service
     * and returns a structured JSON string to forward to the
     * frontend, or {@code null} to skip the event.
     *
     * <p>Event types handled:
     * <ul>
     *   <li>{@code token} – extract {@code content}, accumulate,
     *       forward as {@code {"type":"token","content":"..."}}</li>
     *   <li>{@code done} – extract {@code fullResponse} for
     *       persistence, forward {@code {"type":"done"}}</li>
     *   <li>{@code tool_start/tool_end} – forward with
     *       {@code name} field for loading indicators</li>
     *   <li>{@code error} – mark error flag, forward with
     *       {@code message} field</li>
     *   <li>{@code start} – ignored (we emit our own)</li>
     * </ul>
     */
    @SuppressWarnings("ReturnCount")
    private String processSseEvent(
            ServerSentEvent<String> sse,
            StringBuilder tokenAccumulator,
            AtomicReference<String> doneFullResponse,
            AtomicBoolean hasError) {
        String event = sse.event();
        String data = sse.data();

        if (event == null || data == null) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(data);
            return switch (event) {
                case "token" -> handleTokenEvent(
                        node, tokenAccumulator);
                case "done" -> handleDoneEvent(
                        node, doneFullResponse);
                case "tool_start", "tool_end" ->
                        handleToolEvent(event, node);
                case "error" -> handleErrorEvent(
                        node, hasError);
                default -> {
                    log.debug("Ignoring SSE event: {}", event);
                    yield null;
                }
            };
        } catch (JsonProcessingException ex) {
            log.warn("Failed to parse SSE data for event={}: {}",
                    event, ex.getMessage());
            return null;
        }
    }

    private String handleTokenEvent(JsonNode node,
                                    StringBuilder accumulator) {
        String content = node.path("content").asText("");
        if (!content.isEmpty()) {
            accumulator.append(content);
        }
        return "{\"type\":\"token\",\"content\":"
                + quoteJson(content) + "}";
    }

    private String handleDoneEvent(
            JsonNode node,
            AtomicReference<String> doneFullResponse) {
        String full = node.path("fullResponse").asText(null);
        if (full != null) {
            doneFullResponse.set(full);
        }
        return "{\"type\":\"done\"}";
    }

    private String handleToolEvent(String event, JsonNode node) {
        String name = node.path("name").asText("");
        return "{\"type\":\"" + event + "\",\"name\":"
                + quoteJson(name) + "}";
    }

    private String handleErrorEvent(JsonNode node,
                                    AtomicBoolean hasError) {
        hasError.set(true);
        String msg = node.path("message").asText("Unknown error");
        log.error("AI service returned error event: {}", msg);
        return buildErrorEvent(msg);
    }

    private String quoteJson(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "\"\"";
        }
    }

    private String buildStartEvent(AiConversation conversation) {
        return "{\"type\":\"start\","
                + "\"conversationId\":\""
                + conversation.getId() + "\","
                + "\"threadId\":\""
                + conversation.getThreadId() + "\"}";
    }

    private String buildErrorEvent(String msg) {
        return "{\"type\":\"error\",\"error\":\""
                + msg.replace("\"", "\\\"") + "\"}";
    }

    private AiChatMessageResponse toDto(AiMessage msg) {
        return AiChatMessageResponse.builder()
                .messageId(msg.getId())
                .role(msg.getRole().name())
                .content(msg.getContent())
                .sequence(msg.getSequence())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
