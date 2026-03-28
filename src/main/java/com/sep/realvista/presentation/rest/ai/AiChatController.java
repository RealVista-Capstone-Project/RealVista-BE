package com.sep.realvista.presentation.rest.ai;

import com.sep.realvista.application.ai.dto.AiChatRequest;
import com.sep.realvista.application.ai.service.AiChatApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

/**
 * SSE proxy endpoint for AI chat conversations.
 * JWT-protected — the authenticated user's identity is forwarded
 * to the NestJS AI microservice via internal headers.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "AI-powered chat with streaming responses")
@Slf4j
public class AiChatController {

    private final AiChatApplicationService aiChatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Stream AI chat response",
            description = "Sends a message to the AI service and streams "
                    + "the response as Server-Sent Events. "
                    + "Requires a valid JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "SSE stream of AI chat tokens",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    description = "Server-Sent Events stream"
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Flux<String>> chat(
            @Valid @RequestBody AiChatRequest request,
            @AuthenticationPrincipal SecurityUserDetails principal
    ) {
        log.info("AI chat request from user={}", principal.getUserId());

        String roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Flux<String> stream = aiChatService.streamChat(
                request.getMessage(),
                request.getThreadId(),
                principal.getUserId().toString(),
                principal.getUsername(),
                roles
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
    }
}
