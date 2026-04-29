package com.sep.realvista.presentation.rest.ai;

import com.sep.realvista.application.ai.dto.AiChatRequest;
import com.sep.realvista.application.ai.dto.AiConversationMessagesResponse;
import com.sep.realvista.application.ai.service.AiChatApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

/**
 * AI chat endpoints with conversation persistence.
 *
 * <ul>
 *   <li>{@code POST /chat} — stream AI response (auto-creates
 *       conversation on first call)</li>
 *   <li>{@code GET /conversations/messages} — fetch message
 *       history</li>
 *   <li>{@code DELETE /conversations} — reset conversation</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat",
        description = "AI-powered chat with streaming responses "
                + "and conversation persistence")
@Slf4j
public class AiChatController {

    private final AiChatApplicationService aiChatService;

    // ── POST /api/v1/ai/chat ─────────────────────────────────

    @PostMapping(value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Stream AI chat response",
            description = "Sends a message to the AI service and "
                    + "streams the response as Server-Sent Events. "
                    + "Auto-creates a conversation on first call. "
                    + "Requires a valid JWT token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SSE stream of AI chat tokens",
                    content = @Content(
                            mediaType = MediaType
                                    .TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    description = "SSE stream"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request")
    })
    public ResponseEntity<Flux<String>> chat(
            @Valid @RequestBody AiChatRequest request,
            @AuthenticationPrincipal SecurityUserDetails principal
    ) {
        log.info("AI chat from user={}", principal.getUserId());

        String roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Flux<String> stream = aiChatService.streamChat(
                request.getMessage(),
                request.getListingId(),
                principal.getUserId(),
                principal.getUsername(),
                roles
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
    }

    // ── GET /api/v1/ai/conversations/messages ────────────────

    @GetMapping("/conversations/messages")
    @Operation(
            summary = "Get AI conversation messages",
            description = "Returns the full message history for "
                    + "the authenticated user's AI conversation."
    )
    public ResponseEntity<ApiResponse<AiConversationMessagesResponse>>
            getMessages(
            @AuthenticationPrincipal SecurityUserDetails principal
    ) {
        log.info("Get AI messages for user={}",
                principal.getUserId());

        AiConversationMessagesResponse response =
                aiChatService.getMessages(principal.getUserId());

        return ResponseEntity.ok(
                ApiResponse.success("Messages retrieved", response));
    }

    // ── DELETE /api/v1/ai/conversations ──────────────────────

    @DeleteMapping("/conversations")
    @Operation(
            summary = "Delete AI conversation",
            description = "Deletes the user's AI conversation "
                    + "and all messages. The AI service thread is "
                    + "also cleaned up."
    )
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal SecurityUserDetails principal
    ) {
        log.info("Delete AI conversation for user={}",
                principal.getUserId());

        String roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        aiChatService.deleteConversation(
                principal.getUserId(),
                principal.getUsername(),
                roles
        );

        return ResponseEntity.ok(
                ApiResponse.success("Conversation deleted", null));
    }
    // ── GET /api/v1/ai/quota ──────────────────────────────────

    @GetMapping("/quota")
    @Operation(
            summary = "Get AI chat quota status",
            description = "Returns the remaining and total AI chat quota "
                    + "for the authenticated user for the current day."
    )
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>>
            getQuotaStatus(
            @AuthenticationPrincipal SecurityUserDetails principal
    ) {
        log.info("Get AI quota status for user={}",
                principal.getUserId());

        java.util.Map<String, Object> status =
                aiChatService.getAiQuotaStatus(principal.getUserId());

        return ResponseEntity.ok(
                ApiResponse.success("Quota status retrieved", status));
    }
}
