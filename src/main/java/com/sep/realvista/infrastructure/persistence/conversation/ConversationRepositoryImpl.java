package com.sep.realvista.infrastructure.persistence.conversation;

import com.sep.realvista.domain.conversation.Conversation;
import com.sep.realvista.domain.conversation.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final ConversationJpaRepository conversationJpaRepository;

    @Override
    public Conversation save(Conversation conversation) {
        return conversationJpaRepository.save(conversation);
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return conversationJpaRepository.findById(id);
    }
}
