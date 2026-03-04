package com.sep.realvista.application.websocket.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * WebSocket message DTO for real-time chat messaging.
 * Used for sending messages via STOMP /app/chat.send endpoint.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatWebSocketMessage {

    private UUID conversationId;
    private UUID recipientUserId;
    private String messageType;
    private String content;
    private String metadata;
    private UUID replyToMessageId;
}
