package com.sep.realvista.application.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conversation response DTO.
 */
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
}
