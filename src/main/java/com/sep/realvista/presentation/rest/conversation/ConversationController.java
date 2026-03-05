package com.sep.realvista.presentation.rest.conversation;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.conversation.dto.response.ConversationListItemResponse;
import com.sep.realvista.application.conversation.dto.response.ConversationResponse;
import com.sep.realvista.application.conversation.dto.response.MessagePaginationResponse;
import com.sep.realvista.application.conversation.dto.request.SendMessageRequest;
import com.sep.realvista.application.conversation.dto.response.SendMessageResponse;
import com.sep.realvista.application.conversation.service.ConversationApplicationService;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation Management", description = "Endpoints for managing conversations")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class ConversationController {

    private final ConversationApplicationService conversationApplicationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserDomainService userDomainService;

    @GetMapping
    @Operation(summary = "List user conversations",
            description = "Retrieves all conversations for the authenticated user, "
                    + "sorted by last message time (newest first).")
    public ResponseEntity<ApiResponse<List<ConversationListItemResponse>>> getUserConversations(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        String traceId = initializeTraceId();
        try {
            log.info("Listing conversations - traceId: {}, userId: {}",
                    traceId, currentUser.getUserId());

            List<ConversationListItemResponse> conversations = conversationApplicationService
                    .getUserConversations(currentUser.getUserId());

            return ResponseEntity.ok(
                    ApiResponse.success("Conversations retrieved successfully", conversations));
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/users/{otherUserId}")
    @Operation(summary = "Get conversation between users",
            description = "Retrieves the conversation between the authenticated user "
                    + "and another user specified by ID.")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationBetweenUsers(
            @PathVariable UUID otherUserId,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        String traceId = initializeTraceId();
        try {
            log.info("Getting conversation - traceId: {}, currentUserId: {}, otherUser: {}",
                    traceId, currentUser.getUserId(), otherUserId);

            ConversationResponse conversation = conversationApplicationService
                    .getConversationBetweenUsers(currentUser.getUserId(), otherUserId);

            return ResponseEntity.ok(
                    ApiResponse.success("Conversation retrieved successfully", conversation));
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get messages from a conversation",
            description = "Retrieves messages using cursor-based pagination. "
                    + "Messages are returned in reverse chronological order (newest first). "
                    + "Use 'before' cursor to load older messages, 'after' cursor to load newer.")
    public ResponseEntity<ApiResponse<MessagePaginationResponse>> getConversationMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        String traceId = initializeTraceId();
        try {
            log.info("Getting messages - traceId: {}, conversationId: {}, "
                            + "limit: {}, before: {}, after: {}, userId: {}",
                    traceId, conversationId, limit, before, after, currentUser.getUserId());

            MessagePaginationResponse response = conversationApplicationService
                    .getConversationMessages(conversationId, currentUser.getUserId(), limit, before, after);

            return ResponseEntity.ok(
                    ApiResponse.success("Messages retrieved successfully", response));
        } finally {
            MDC.clear();
        }
    }

    @PostMapping("/messages")
    @Operation(summary = "Send a message",
            description = "Sends a message to a user. "
                    + "If no conversation exists between users, "
                    + "it will be created automatically. "
                    + "Supports TEXT, LISTING_CARD, and CONTRACT_CARD message types.")
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        String traceId = initializeTraceId();
        try {
            log.info("Send message request - traceId: {}, from: {}, to: {}, type: {}",
                    traceId, currentUser.getUserId(), request.getRecipientUserId(),
                    request.getMessageType());

            SendMessageResponse response = conversationApplicationService
                    .sendMessage(currentUser.getUserId(), request);

            broadcastViaWebSocket(currentUser.getUserId(), request.getRecipientUserId(), response);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Message sent successfully", response));
        } finally {
            MDC.clear();
        }
    }

    private void broadcastViaWebSocket(UUID senderId, UUID recipientId, SendMessageResponse response) {
        try {
            User sender = userDomainService.getUserOrThrow(senderId);
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail().getValue(),
                    "/queue/messages",
                    response
            );

            User recipient = userDomainService.getUserOrThrow(recipientId);
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail().getValue(),
                    "/queue/messages",
                    response
            );
        } catch (Exception e) {
            log.error("Failed to deliver message via WebSocket - senderId: {}, recipientId: {}: {}",
                    senderId, recipientId, e.getMessage(), e);
        }
    }

    private String initializeTraceId() {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        return traceId;
    }
}
