package com.sep.realvista.application.conversation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sep.realvista.domain.conversation.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for sent message.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageResponse {

    private UUID messageId;
    private UUID conversationId;
    private SenderInfo sender;
    private UUID recipientUserId;
    private MessageType messageType;
    private String content;
    private String metadata;
    private UUID replyToMessageId;
    private LocalDateTime createdAt;
    private boolean conversationCreated;
}
