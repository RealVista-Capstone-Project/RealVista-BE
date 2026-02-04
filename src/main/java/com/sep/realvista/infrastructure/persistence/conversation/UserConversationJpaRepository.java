package com.sep.realvista.infrastructure.persistence.conversation;

import com.sep.realvista.domain.conversation.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserConversationJpaRepository
        extends JpaRepository<UserConversation, UUID> {

    @Query("SELECT uc FROM UserConversation uc "
            + "WHERE uc.conversationId = :conversationId AND uc.userId = :userId")
    Optional<UserConversation> findByConversationIdAndUserId(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);

    @Query("SELECT uc FROM UserConversation uc WHERE uc.userId = :userId")
    List<UserConversation> findByUserId(@Param("userId") UUID userId);

    /**
     * Find conversation ID between two users using an optimized single query.
     * Uses a subquery to check if both users are participants in the same conversation.
     * This is more efficient than fetching all conversations and filtering in memory.
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return Optional containing the conversation ID if exists
     */
    @Query("SELECT uc1.conversationId FROM UserConversation uc1 "
            + "WHERE uc1.userId = :userId1 "
            + "AND EXISTS ("
            + "  SELECT 1 FROM UserConversation uc2 "
            + "  WHERE uc2.conversationId = uc1.conversationId "
            + "  AND uc2.userId = :userId2"
            + ")")
    Optional<UUID> findConversationIdBetweenUsers(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2);
}
