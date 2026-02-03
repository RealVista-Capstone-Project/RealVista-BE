package com.sep.realvista.presentation.rest.conversation;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.conversation.dto.ConversationResponse;
import com.sep.realvista.application.conversation.dto.CreateConversationRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @Operation(summary = "Create a new 1-1 conversation",
            description = "Creates a new conversation between the authenticated user "
                    + "and another user. If a conversation already exists, "
                    + "returns the existing conversation.")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            Authentication authentication
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("Create Conversation request is: {}", request.toString());

        String currentUserEmail = authentication.getName();
        log.info("Creating conversation - traceId: {}, currentUser: {}, targetUser: {}",
                traceId, currentUserEmail, request.getTargetUserId());

        // Get current user ID from email
        User currentUser = userDomainService.getUserByEmailOrThrow(currentUserEmail);

        ConversationResponse conversation = conversationApplicationService.createConversation(
                currentUser.getUserId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation created successfully", conversation));
    }

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
}
