package com.sep.realvista.application.conversation.dto;

import com.sep.realvista.domain.conversation.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private UUID messageId;
    private UUID conversationId;
    private UUID replyToMessageId;
    private MessageType messageType;
    private String content;
    private String metadata;
    private SenderInfo sender;
    private LocalDateTime createdAt;
}
