package com.sep.realvista.infrastructure.persistence.aichat;

import com.sep.realvista.domain.aichat.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for AI chat messages.
 */
public interface AiMessageJpaRepository
        extends JpaRepository<AiMessage, UUID> {

    List<AiMessage> findByConversationIdOrderBySequenceAsc(
            UUID conversationId);

    int countByConversationId(UUID conversationId);
}
