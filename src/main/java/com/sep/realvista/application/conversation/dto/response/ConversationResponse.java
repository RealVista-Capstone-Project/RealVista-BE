package com.sep.realvista.application.conversation.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conversation response DTO.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private UUID conversationId;
    private UUID otherUserId;
    private String otherUserName;
    private String otherUserAvatarUrl;
    private LocalDateTime createdAt;

    /** True when this conversation was just created; false if it already existed. */
    private boolean conversationCreated;
}
