package com.sep.realvista.domain.conversation;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Domain Service for Conversation operations.
 * Contains pure business logic and domain rules for conversation management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationDomainService {

    private final ConversationRepository conversationRepository;
    private final UserConversationRepository userConversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Validates message content based on message type.
     *
     * @param messageType the type of message
     * @param content     the message content
     * @param metadata    the message metadata
     * @throws BusinessConflictException if validation fails
     */
    public void validateMessageContent(MessageType messageType, String content, String metadata) {
        switch (messageType) {
            case TEXT:
                if (content == null || content.isBlank()) {
                    throw new BusinessConflictException(
                            "Content is required for TEXT messages",
                            "MISSING_MESSAGE_CONTENT"
                    );
                }
                break;
            case LISTING_CARD:
            case CONTRACT_CARD:
                if (metadata == null || metadata.isBlank()) {
                    throw new BusinessConflictException(
                            "Metadata is required for " + messageType + " messages",
                            "MISSING_MESSAGE_METADATA"
                    );
                }
                break;
            case SYSTEM:
                throw new BusinessConflictException(
                        "Cannot send SYSTEM messages via API",
                        "SYSTEM_MESSAGE_NOT_ALLOWED"
                );
            default:
                throw new BusinessConflictException(
                        "Unsupported message type: " + messageType,
                        "UNSUPPORTED_MESSAGE_TYPE"
                );
        }
    }

    /**
     * Finds an existing conversation between two users.
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return the conversation entity
     * @throws ResourceNotFoundException if conversation not found
     */
    public Conversation findConversationBetweenUsers(UUID userId1, UUID userId2) {
        UUID conversationId = userConversationRepository
                .findConversationIdBetweenUsers(userId1, userId2)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation",
                        "No conversation found between users"));

        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation",
                        "Conversation not found: " + conversationId));
    }

    /**
     * Creates a new conversation with two participants.
     *
     * @param user1Id first user ID
     * @param user2Id second user ID
     * @return the created conversation
     */
    public Conversation createConversationWithParticipants(UUID user1Id, UUID user2Id) {
        log.info("Creating new conversation between users {} and {}", user1Id, user2Id);

        // Create conversation
        Conversation newConversation = Conversation.create();
        Conversation savedConversation = conversationRepository.save(newConversation);
        UUID conversationId = savedConversation.getConversationId();

        // Create UserConversation entries for both users
        UserConversation user1Conv = UserConversation.create(conversationId, user1Id);
        UserConversation user2Conv = UserConversation.create(conversationId, user2Id);

        userConversationRepository.save(user1Conv);
        userConversationRepository.save(user2Conv);

        log.info("Created conversation {} with participants {} and {}",
                conversationId, user1Id, user2Id);

        return savedConversation;
    }

    /**
     * Validates that a reply message exists and belongs to the specified conversation.
     *
     * @param replyToMessageId the message ID being replied to
     * @param conversationId   the expected conversation ID
     * @throws ResourceNotFoundException    if reply message not found
     * @throws BusinessConflictException if reply message belongs to different conversation
     */
    public void validateReplyMessage(UUID replyToMessageId, UUID conversationId) {
        Message replyToMessage = messageRepository.findById(replyToMessageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Message",
                        "Reply message not found: " + replyToMessageId));

        // Validate reply message belongs to same conversation
        if (!replyToMessage.getConversationId().equals(conversationId)) {
            throw new BusinessConflictException(
                    "Reply message must belong to the same conversation",
                    "INVALID_REPLY_MESSAGE"
            );
        }
    }

    /**
     * Finds an existing conversation between two users or creates a new one.
     *
     * @param senderId    the sender user ID
     * @param recipientId the recipient user ID
     * @return the conversation entity and whether it was newly created
     */
    public ConversationResult findOrCreateConversation(UUID senderId, UUID recipientId) {
        try {
            // Try to find existing conversation
            Conversation existingConversation = findConversationBetweenUsers(senderId, recipientId);
            log.info("Found existing conversation: {}", existingConversation.getConversationId());
            return new ConversationResult(existingConversation, false);
        } catch (ResourceNotFoundException e) {
            // Conversation doesn't exist, create new one
            Conversation newConversation = createConversationWithParticipants(senderId, recipientId);
            log.info("Created new conversation: {}", newConversation.getConversationId());
            return new ConversationResult(newConversation, true);
        }
    }

    /**
     * Result of finding or creating a conversation.
     */
    public record ConversationResult(Conversation conversation, boolean created) {
    }
}
