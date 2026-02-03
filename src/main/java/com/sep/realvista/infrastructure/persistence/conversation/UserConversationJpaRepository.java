package com.sep.realvista.infrastructure.persistence.conversation;

import com.sep.realvista.domain.conversation.UserConversation;
import com.sep.realvista.domain.conversation.UserConversationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserConversationJpaRepository
        extends JpaRepository<UserConversation, UUID>, UserConversationRepository {

    @Override
    @Query("SELECT uc FROM UserConversation uc "
            + "WHERE uc.conversationId = :conversationId AND uc.userId = :userId")
    Optional<UserConversation> findByConversationIdAndUserId(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);

    @Override
    @Query("SELECT uc FROM UserConversation uc WHERE uc.userId = :userId")
    List<UserConversation> findByUserId(@Param("userId") UUID userId);
}
