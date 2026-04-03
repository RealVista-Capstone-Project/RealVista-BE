package com.sep.realvista.infrastructure.persistence.aichat;

import com.sep.realvista.domain.aichat.AiMessage;
import com.sep.realvista.domain.aichat.AiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the domain AI message repository
 * delegating to Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class AiMessageRepositoryImpl implements AiMessageRepository {

    private final AiMessageJpaRepository jpaRepository;

    @Override
    public AiMessage save(AiMessage message) {
        return jpaRepository.save(message);
    }

    @Override
    public List<AiMessage> findByConversationIdOrderBySequence(
            UUID conversationId) {
        return jpaRepository
                .findByConversationIdOrderBySequenceAsc(conversationId);
    }

    @Override
    public int countByConversationId(UUID conversationId) {
        return jpaRepository.countByConversationId(conversationId);
    }
}
