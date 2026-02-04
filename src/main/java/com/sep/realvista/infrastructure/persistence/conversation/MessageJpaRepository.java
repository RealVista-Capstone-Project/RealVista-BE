package com.sep.realvista.infrastructure.persistence.conversation;

import com.sep.realvista.domain.conversation.Message;
import com.sep.realvista.domain.conversation.MessageRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageJpaRepository extends JpaRepository<Message, UUID>, MessageRepository {

    @Override
    @Query("SELECT m FROM Message m "
            + "WHERE m.conversationId = :conversationId "
            + "ORDER BY m.createdAt DESC")
    List<Message> findLatestMessages(
            @Param("conversationId") UUID conversationId,
            @Param("limit") int limit);

    @Override
    @Query("SELECT m FROM Message m "
            + "WHERE m.conversationId = :conversationId "
            + "AND m.createdAt < :cursor "
            + "ORDER BY m.createdAt DESC")
    List<Message> findMessagesBeforeCursor(
            @Param("conversationId") UUID conversationId,
            @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit);

    @Override
    @Query("SELECT m FROM Message m "
            + "WHERE m.conversationId = :conversationId "
            + "AND m.createdAt > :cursor "
            + "ORDER BY m.createdAt ASC")
    List<Message> findMessagesAfterCursor(
            @Param("conversationId") UUID conversationId,
            @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit);
}
