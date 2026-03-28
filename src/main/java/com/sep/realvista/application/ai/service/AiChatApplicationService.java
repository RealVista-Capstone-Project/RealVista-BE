package com.sep.realvista.application.ai.service;

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
import org.springframework.http.MediaType;
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

    private final WebClient aiWebClient;
    private final String serviceApiKey;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final AiChatPersistenceHelper persistenceHelper;

    public AiChatApplicationService(
            WebClient aiWebClient,
            @Value("${realvista.ai.api-key:}") String serviceApiKey,
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository,
            AiChatPersistenceHelper persistenceHelper
    ) {
        this.aiWebClient = aiWebClient;
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
        StringBuilder fullResponse = new StringBuilder();

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
                .bodyToFlux(String.class)
                .doOnNext(chunk -> {
                    if (chunk != null && !chunk.isBlank()) {
                        fullResponse.append(chunk);
                    }
                })
                .doOnComplete(() ->
                        persistenceHelper.saveAssistantMessage(
                                convId, fullResponse.toString(),
                                assistantSeq))
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
