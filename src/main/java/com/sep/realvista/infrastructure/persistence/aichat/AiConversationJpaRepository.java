package com.sep.realvista.infrastructure.persistence.aichat;

import com.sep.realvista.domain.aichat.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for AI conversations.
 */
public interface AiConversationJpaRepository
        extends JpaRepository<AiConversation, UUID> {

    Optional<AiConversation> findByUserId(UUID userId);
}
