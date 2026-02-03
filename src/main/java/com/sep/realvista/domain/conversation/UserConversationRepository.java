package com.sep.realvista.domain.conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserConversationRepository {

    UserConversation save(UserConversation userConversation);

    Optional<UserConversation> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    List<UserConversation> findByUserId(UUID userId);

    Optional<UUID> findConversationIdBetweenUsers(UUID userId1, UUID userId2);
}
