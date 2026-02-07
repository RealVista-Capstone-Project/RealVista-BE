package com.sep.realvista.application.conversation.service;

import com.sep.realvista.application.conversation.dto.CursorBasedPaginationMetadata;
import com.sep.realvista.application.conversation.dto.SenderInfo;
import com.sep.realvista.application.conversation.dto.request.SendMessageRequest;
import com.sep.realvista.application.conversation.dto.response.ConversationResponse;
import com.sep.realvista.application.conversation.dto.response.MessagePaginationResponse;
import com.sep.realvista.application.conversation.dto.response.MessageResponse;
import com.sep.realvista.application.conversation.dto.response.SendMessageResponse;
import com.sep.realvista.application.conversation.mapper.ConversationMapper;
import com.sep.realvista.application.conversation.mapper.MessageMapper;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.conversation.Conversation;
import com.sep.realvista.domain.conversation.ConversationDomainService;
import com.sep.realvista.domain.conversation.ConversationRepository;
import com.sep.realvista.domain.conversation.Message;
import com.sep.realvista.domain.conversation.MessageRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Application Service for Conversation operations.
 * Orchestrates business logic for creating and managing 1-1 conversations.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConversationApplicationService {

    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_LIMIT = 50;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserDomainService userDomainService;
    private final ConversationDomainService conversationDomainService;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    /**
     * Get the conversation between two users.
     *
     * @param userId1 first user ID
     * @param userId2 second user ID (the other user in the conversation)
     * @return the conversation response with details
     * @throws com.sep.realvista.domain.common.exception.ResourceNotFoundException
     *     if conversation not found or users don't exist
     */
    @Transactional(readOnly = true)
    public ConversationResponse getConversationBetweenUsers(UUID userId1, UUID userId2) {
        log.info("Getting conversation between user {} and user {}", userId1, userId2);

        // Validate both users exist
        userDomainService.getUserOrThrow(userId1);
        User otherUser = userDomainService.getUserOrThrow(userId2);

        // Find conversation using domain service
        Conversation conversation = conversationDomainService
                .findConversationBetweenUsers(userId1, userId2);

        log.info("Found conversation with ID: {}", conversation.getConversationId());

        return conversationMapper.toResponse(conversation, otherUser);
    }

    /**
     * Get messages from a conversation with cursor-based pagination.
     * Supports loading initial messages, older messages (scroll up),
     * and newer messages (refresh).
     *
     * @param conversationId the conversation ID
     * @param limit          number of messages to return (max 100)
     * @param before         cursor for loading older messages
     * @param after          cursor for loading newer messages
     * @return paginated message response with cursor metadata
     */
    @Transactional(readOnly = true)
    public MessagePaginationResponse getConversationMessages(
            UUID conversationId,
            Integer limit,
            LocalDateTime before,
            LocalDateTime after
    ) {
        log.info("Getting messages for conversation {} - limit: {}, before: {}, after: {}",
                conversationId, limit, before, after);

        // Validate conversation exists
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new com.sep.realvista.domain.common.exception
                        .ResourceNotFoundException(
                        "Conversation",
                        "Conversation not found: " + conversationId));

        // Validate cursor usage (cannot use both before and after)
        if (before != null && after != null) {
            throw new BusinessConflictException(
                    "Cannot use both 'before' and 'after' cursors simultaneously",
                    "INVALID_CURSOR_COMBINATION"
            );
        }

        // Apply limit constraints
        int effectiveLimit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        if (limit != null && limit > MAX_LIMIT) {
            log.warn("Requested limit {} exceeds max {}, capping at max",
                    limit, MAX_LIMIT);
        }

        // Fetch limit + 1 to determine if there are more messages
        int fetchLimit = effectiveLimit + 1;

        // Fetch messages based on cursor
        List<Message> messages;
        if (before != null) {
            // Load older messages
            messages = messageRepository
                    .findMessagesBeforeCursor(conversationId, before, fetchLimit);
        } else if (after != null) {
            // Load newer messages (note: results are in ASC order)
            messages = messageRepository
                    .findMessagesAfterCursor(conversationId, after, fetchLimit);
        } else {
            // Load most recent messages
            messages = messageRepository.findLatestMessages(conversationId, fetchLimit);
        }

        // Check if there are more messages
        boolean hasMore = messages.size() > effectiveLimit;

        // Trim to requested limit
        List<Message> trimmedMessages = hasMore
                ? messages.subList(0, effectiveLimit)
                : messages;

        // Reverse order if loading newer messages (they come in ASC, need DESC)
        if (after != null && !trimmedMessages.isEmpty()) {
            trimmedMessages = new ArrayList<>(trimmedMessages);
            Collections.reverse(trimmedMessages);
        }

        // Map to response DTOs
        List<MessageResponse> messageResponses = trimmedMessages.stream()
                .map(message -> {
                    SenderInfo senderInfo = null;
                    if (message.getSenderId() != null) {
                        User sender = userDomainService
                                .getUserOrThrow(message.getSenderId());
                        senderInfo = messageMapper.toSenderInfo(sender);
                    }
                    return messageMapper.toResponse(message, senderInfo);
                })
                .toList();

        // Build pagination metadata
        CursorBasedPaginationMetadata pagination = buildPaginationMetadata(
                effectiveLimit,
                hasMore,
                trimmedMessages,
                before,
                after
        );

        log.info("Retrieved {} messages for conversation {}, hasMore: {}",
                messageResponses.size(), conversationId, hasMore);

        return MessagePaginationResponse.builder()
                .messages(messageResponses)
                .pagination(pagination)
                .build();
    }

    private CursorBasedPaginationMetadata buildPaginationMetadata(
            int limit,
            boolean hasMore,
            List<Message> messages,
            LocalDateTime before,
            LocalDateTime after
    ) {
        String nextCursor = null;
        String prevCursor = null;

        if (!messages.isEmpty()) {
            // next_cursor points to the oldest message (for loading older)
            Message oldestMessage = messages.getLast();
            nextCursor = hasMore && after == null
                    ? oldestMessage.getCreatedAt().format(ISO_FORMATTER)
                    : null;

            // prev_cursor points to the newest message (for loading newer)
            Message newestMessage = messages.getFirst();
            prevCursor = before != null || after != null
                    ? newestMessage.getCreatedAt().format(ISO_FORMATTER)
                    : null;
        }

        return CursorBasedPaginationMetadata.builder()
                .limit(limit)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .prevCursor(prevCursor)
                .build();
    }

    /**
     * Send a message to a user. Creates conversation if it doesn't exist.
     *
     * @param senderId the sender user ID (authenticated user)
     * @param request  the send message request
     * @return the send message response
     */
    @Transactional
    public SendMessageResponse sendMessage(UUID senderId, SendMessageRequest request) {
        log.info("Sending message from {} to {} - type: {}",
                senderId, request.getRecipientUserId(), request.getMessageType());

        // Validate sender and recipient
        User sender = userDomainService.getUserOrThrow(senderId);
        userDomainService.getUserOrThrow(request.getRecipientUserId());

        // Prevent self-messaging
        // TODO: Consider allowing self-messaging for notes in future
        if (senderId.equals(request.getRecipientUserId())) {
            throw new BusinessConflictException(
                    "Cannot send message to yourself",
                    "SELF_MESSAGING_NOT_ALLOWED"
            );
        }

        // Validate message content based on type (delegate to domain service)
        conversationDomainService.validateMessageContent(
                request.getMessageType(),
                request.getContent(),
                request.getMetadata()
        );

        // Find or create conversation (delegate to domain service)
        ConversationDomainService.ConversationResult result =
                conversationDomainService.findOrCreateConversation(
                        senderId, request.getRecipientUserId());
        UUID conversationId = result.conversation().getConversationId();
        boolean conversationCreated = result.created();

        // Validate reply message if provided (delegate to domain service)
        if (request.getReplyToMessageId() != null) {
            conversationDomainService.validateReplyMessage(
                    request.getReplyToMessageId(),
                    conversationId
            );
        }

        // Create message entity
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .messageType(request.getMessageType())
                .content(request.getContent())
                .metadata(request.getMetadata())
                .replyToMessageId(request.getReplyToMessageId())
                .build();

        // Save message
        Message savedMessage = messageRepository.save(message);

        log.info("Message sent successfully - messageId: {}, conversationId: {}, created: {}",
                savedMessage.getMessageId(), conversationId, conversationCreated);

        // Build response
        return SendMessageResponse.builder()
                .messageId(savedMessage.getMessageId())
                .conversationId(conversationId)
                .sender(messageMapper.toSenderInfo(sender))
                .recipientUserId(request.getRecipientUserId())
                .messageType(savedMessage.getMessageType())
                .content(savedMessage.getContent())
                .metadata(savedMessage.getMetadata())
                .replyToMessageId(savedMessage.getReplyToMessageId())
                .createdAt(savedMessage.getCreatedAt())
                .conversationCreated(conversationCreated)
                .build();
    }


}
