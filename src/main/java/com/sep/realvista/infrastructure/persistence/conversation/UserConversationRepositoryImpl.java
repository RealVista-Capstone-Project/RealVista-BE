package com.sep.realvista.infrastructure.persistence.conversation;

import com.sep.realvista.domain.conversation.UserConversation;
import com.sep.realvista.domain.conversation.UserConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserConversationRepositoryImpl implements UserConversationRepository {

    private final UserConversationJpaRepository userConversationJpaRepository;


    @Override
    public UserConversation save(UserConversation userConversation) {
        return userConversationJpaRepository.save(userConversation);
    }

    @Override
    public Optional<UserConversation> findByConversationIdAndUserId(UUID conversationId, UUID userId) {
        return userConversationJpaRepository.findByConversationIdAndUserId(conversationId, userId);
    }

    @Override
    public List<UserConversation> findByUserId(UUID userId) {
        return userConversationJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<UUID> findConversationIdBetweenUsers(UUID userId1, UUID userId2) {
        return userConversationJpaRepository.findConversationIdBetweenUsers(userId1, userId2);
    }
}
