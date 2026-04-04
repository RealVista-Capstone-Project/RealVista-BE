package com.sep.realvista.infrastructure.persistence.aichat;

import com.sep.realvista.domain.aichat.AiConversation;
import com.sep.realvista.domain.aichat.AiConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the domain AI conversation repository
 * delegating to Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class AiConversationRepositoryImpl
        implements AiConversationRepository {

    private final AiConversationJpaRepository jpaRepository;

    @Override
    public AiConversation save(AiConversation conversation) {
        return jpaRepository.save(conversation);
    }

    @Override
    public Optional<AiConversation> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void delete(AiConversation conversation) {
        jpaRepository.delete(conversation);
    }
}
