package com.sep.realvista.domain.aichat;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for AI conversations.
 */
public interface AiConversationRepository {

    AiConversation save(AiConversation conversation);

    Optional<AiConversation> findByUserId(UUID userId);

    void delete(AiConversation conversation);
}
