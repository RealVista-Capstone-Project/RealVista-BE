package com.sep.realvista.application.conversation.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sep.realvista.application.conversation.dto.SenderInfo;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
