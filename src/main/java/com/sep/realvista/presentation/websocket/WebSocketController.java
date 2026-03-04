package com.sep.realvista.presentation.websocket;

import com.sep.realvista.application.conversation.dto.request.SendMessageRequest;
import com.sep.realvista.application.conversation.dto.response.SendMessageResponse;
import com.sep.realvista.application.conversation.service.ConversationApplicationService;
import com.sep.realvista.application.websocket.dto.ChatWebSocketMessage;
import com.sep.realvista.application.websocket.dto.WebSocketMessage;
import com.sep.realvista.domain.conversation.MessageType;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for real-time chat communication.
 * Handles message sending and typing indicators via STOMP.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationApplicationService conversationApplicationService;
    private final UserDomainService userDomainService;

    /**
     * Handle incoming chat messages via WebSocket.
     * Persists the message and broadcasts to the recipient's private queue.
     *
     * Client sends to: /app/chat.send
     * Server broadcasts to: /user/{recipientEmail}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(
            @Payload ChatWebSocketMessage message,
            Principal principal) {

        if (principal == null) {
            log.warn("Received chat message without authentication");
            return;
        }

        String senderEmail = principal.getName();
        log.info("Chat message received - from: {}, to: {}, type: {}",
                senderEmail, message.getRecipientUserId(), message.getMessageType());

        try {
            User sender = userDomainService.getUserByEmailOrThrow(senderEmail);

            // Build the send message request
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(message.getRecipientUserId())
                    .messageType(MessageType.valueOf(message.getMessageType()))
                    .content(message.getContent())
                    .metadata(message.getMetadata())
                    .replyToMessageId(message.getReplyToMessageId())
                    .build();

            // Persist message via application service
            SendMessageResponse response = conversationApplicationService
                    .sendMessage(sender.getUserId(), request);

            // Send to sender's queue (for multi-device sync)
            messagingTemplate.convertAndSendToUser(
                    senderEmail,
                    "/queue/messages",
                    response
            );

            // Send to recipient's queue
            User recipient = userDomainService.getUserOrThrow(message.getRecipientUserId());
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail().getValue(),
                    "/queue/messages",
                    response
            );

            log.info("Chat message delivered - messageId: {}, conversationId: {}",
                    response.getMessageId(), response.getConversationId());

        } catch (Exception e) {
            log.error("Error processing chat message from {}: {}", senderEmail, e.getMessage(), e);
        }
    }

    /**
     * Handle typing indicator events.
     * Forwards typing status to the other participant's private queue.
     *
     * Client sends to: /app/chat.typing
     * Server broadcasts to: /user/{recipientEmail}/queue/typing
     */
    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(
            @Payload ChatWebSocketMessage message,
            Principal principal) {

        if (principal == null) {
            return;
        }

        String senderEmail = principal.getName();

        try {
            User recipient = userDomainService.getUserOrThrow(message.getRecipientUserId());

            WebSocketMessage typingEvent = WebSocketMessage.builder()
                    .type("TYPING")
                    .senderName(senderEmail)
                    .payload(message.getConversationId())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail().getValue(),
                    "/queue/typing",
                    typingEvent
            );
        } catch (Exception e) {
            log.error("Error processing typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Public WebSocket endpoint (no authentication required).
     * Client sends to: /app/public
     * Server broadcasts to: /topic/public
     */
    @MessageMapping("/public")
    @SendTo("/topic/public")
    public WebSocketMessage handlePublicMessage(
            @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Received public WebSocket message - session: {}, type: {}",
                headerAccessor.getSessionId(), message.getType());

        return message;
    }
}
