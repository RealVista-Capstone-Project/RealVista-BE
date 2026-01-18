package com.sep.realvista.presentation.websocket;

import com.sep.realvista.application.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for handling real-time message communications.
 * Provides endpoints for message handling and broadcasting.
 */
// TODO: To be removed or replaced with actual implementation
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    /**
     * Example: Public WebSocket endpoint (no authentication required).
     * Client sends to: /app/public
     * Server broadcasts to: /topic/public
     *
     * @param message        the incoming WebSocket message
     * @param headerAccessor the message header accessor for session info
     * @return the processed message to broadcast
     */
    @MessageMapping("/public")
    @SendTo("/topic/public")
    public WebSocketMessage handlePublicMessage(
            @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        log.info("Received public WebSocket message - session: {}, type: {}",
                sessionId, message.getType());

        return message;
    }

    /**
     * Example: Authenticated WebSocket endpoint (requires JWT authentication).
     * Client sends to: /app/secured
     * Server broadcasts to: /topic/secured
     *
     * @param message        the incoming WebSocket message
     * @param headerAccessor the message header accessor for session info
     * @param principal      the authenticated user principal
     * @return the processed message to broadcast with sender information
     */
    @MessageMapping("/secured")
    @SendTo("/topic/secured")
    public WebSocketMessage handleSecuredMessage(
            @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {

        String sessionId = headerAccessor.getSessionId();
        String username = principal != null ? principal.getName() : "Anonymous";

        log.info("Received secured WebSocket message - session: {}, user: {}, type: {}",
                sessionId, username, message.getType());

        // Populate sender information from authenticated user
        if (principal != null) {
            message.setSenderName(username);
        }

        return message;
    }
}
