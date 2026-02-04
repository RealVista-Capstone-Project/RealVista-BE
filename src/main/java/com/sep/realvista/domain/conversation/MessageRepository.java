package com.sep.realvista.domain.conversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain repository interface for Message entity.
 */
public interface MessageRepository {

    /**
     * Find the most recent messages in a conversation.
     *
     * @param conversationId the conversation ID
     * @param limit maximum number of messages to return
     * @return list of messages ordered by created_at DESC
     */
    List<Message> findLatestMessages(UUID conversationId, int limit);

    /**
     * Find messages created before a cursor timestamp (for loading older messages).
     *
     * @param conversationId the conversation ID
     * @param cursor the cursor timestamp
     * @param limit maximum number of messages to return
     * @return list of messages ordered by created_at DESC
     */
    List<Message> findMessagesBeforeCursor(UUID conversationId, LocalDateTime cursor, int limit);

    /**
     * Find messages created after a cursor timestamp (for loading newer messages).
     *
     * @param conversationId the conversation ID
     * @param cursor the cursor timestamp
     * @param limit maximum number of messages to return
     * @return list of messages ordered by created_at ASC
     */
    List<Message> findMessagesAfterCursor(UUID conversationId, LocalDateTime cursor, int limit);
}
