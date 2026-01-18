package com.sep.realvista.application.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base WebSocket message DTO for real-time communication.
 * This can be extended for specific message types.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * Message type identifier for client-side routing.
     */
    private String type;

    /**
     * Message payload containing actual data.
     */
    private Object payload;

    /**
     * Sender user ID (optional, populated by server).
     */
    private Long senderId;

    /**
     * Sender username (optional, populated by server).
     */
    private String senderName;

    /**
     * Message timestamp.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Message metadata (optional).
     */
    private Object metadata;
}
