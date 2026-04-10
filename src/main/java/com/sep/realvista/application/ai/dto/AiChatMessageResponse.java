package com.sep.realvista.application.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a single AI chat message.
 * Fields use camelCase; Jackson global SNAKE_CASE handles JSON conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single AI chat message")
public class AiChatMessageResponse {

    @Schema(description = "Message ID")
    private UUID messageId;

    @Schema(description = "Role: USER or ASSISTANT")
    private String role;

    @Schema(description = "Message content")
    private String content;

    @Schema(description = "Message sequence number within the conversation")
    private int sequence;

    @Schema(description = "When the message was created")
    private LocalDateTime createdAt;
}
