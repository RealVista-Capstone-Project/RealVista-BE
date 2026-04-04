package com.sep.realvista.application.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO wrapping all messages in an AI conversation.
 * Fields use camelCase; Jackson global SNAKE_CASE handles JSON conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI conversation with message history")
public class AiConversationMessagesResponse {

    @Schema(description = "Conversation ID")
    private UUID conversationId;

    @Schema(description = "Thread ID used by the AI service")
    private UUID threadId;

    @Schema(description = "When the conversation was created")
    private LocalDateTime createdAt;

    @Schema(description = "Ordered list of messages")
    private List<AiChatMessageResponse> messages;
}
