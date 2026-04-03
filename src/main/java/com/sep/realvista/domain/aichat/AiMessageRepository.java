package com.sep.realvista.domain.aichat;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository interface for AI chat messages.
 */
public interface AiMessageRepository {

    AiMessage save(AiMessage message);

    List<AiMessage> findByConversationIdOrderBySequence(UUID conversationId);

    int countByConversationId(UUID conversationId);
}
