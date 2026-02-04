package com.sep.realvista.presentation.rest.conversation;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.conversation.dto.ConversationResponse;
import com.sep.realvista.application.conversation.dto.MessagePaginationResponse;
import com.sep.realvista.application.conversation.dto.SendMessageRequest;
import com.sep.realvista.application.conversation.dto.SendMessageResponse;
import com.sep.realvista.application.conversation.service.ConversationApplicationService;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST Controller for Conversation operations.
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation Management", description = "Endpoints for managing conversations")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class ConversationController {

    private final ConversationApplicationService conversationApplicationService;
    private final UserDomainService userDomainService;

    @GetMapping("/users/{otherUserId}")
    @Operation(summary = "Get conversation between users",
            description = "Retrieves the conversation between the authenticated user "
                    + "and another user specified by ID.")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationBetweenUsers(
            @PathVariable UUID otherUserId,
            Authentication authentication
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        String currentUserEmail = authentication.getName();
        log.info("Getting conversation - traceId: {}, currentUser: {}, otherUser: {}",
                traceId, currentUserEmail, otherUserId);

        // Get current user ID from email
        User currentUser = userDomainService.getUserByEmailOrThrow(currentUserEmail);

        ConversationResponse conversation = conversationApplicationService
                .getConversationBetweenUsers(currentUser.getUserId(), otherUserId);

        return ResponseEntity.ok(
                ApiResponse.success("Conversation retrieved successfully", conversation));
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
            Authentication authentication
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        String currentUserEmail = authentication.getName();
        log.info("Getting messages - traceId: {}, conversationId: {}, "
                        + "limit: {}, before: {}, after: {}, user: {}",
                traceId, conversationId, limit, before, after, currentUserEmail);

        // Verify user is authenticated
        userDomainService.getUserByEmailOrThrow(currentUserEmail);

        MessagePaginationResponse response = conversationApplicationService
                .getConversationMessages(conversationId, limit, before, after);

        return ResponseEntity.ok(
                ApiResponse.success("Messages retrieved successfully", response));
    }

    @PostMapping("/messages")
    @Operation(summary = "Send a message",
            description = "Sends a message to a user. "
                    + "If no conversation exists between users, "
                    + "it will be created automatically. "
                    + "Supports TEXT, LISTING_CARD, and CONTRACT_CARD message types.")
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        String currentUserEmail = authentication.getName();
        log.info("Send message request - traceId: {}, from: {}, to: {}, type: {}",
                traceId, currentUserEmail, request.getRecipientUserId(),
                request.getMessageType());

        // Get current user ID from email
        User currentUser = userDomainService.getUserByEmailOrThrow(currentUserEmail);

        SendMessageResponse response = conversationApplicationService
                .sendMessage(currentUser.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", response));
    }
}
