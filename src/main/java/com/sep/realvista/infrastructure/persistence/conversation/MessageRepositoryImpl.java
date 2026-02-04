package com.sep.realvista.infrastructure.persistence.conversation;

import com.sep.realvista.domain.conversation.Message;
import com.sep.realvista.domain.conversation.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository messageJpaRepository;

    @Override
    public List<Message> findLatestMessages(UUID conversationId, int limit) {
        return messageJpaRepository.findLatestMessages(conversationId, limit);
    }

    @Override
    public List<Message> findMessagesBeforeCursor(UUID conversationId, LocalDateTime cursor, int limit) {
        return messageJpaRepository.findMessagesBeforeCursor(conversationId, cursor, limit);
    }

    @Override
    public List<Message> findMessagesAfterCursor(UUID conversationId, LocalDateTime cursor, int limit) {
        return messageJpaRepository.findMessagesAfterCursor(conversationId, cursor, limit);
    }

    @Override
    public Message save(Message message) {
        return messageJpaRepository.save(message);
    }

    @Override
    public Optional<Message> findById(UUID messageId) {
        return messageJpaRepository.findById(messageId);
    }
}
