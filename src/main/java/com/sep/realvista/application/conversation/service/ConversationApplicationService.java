package com.sep.realvista.application.conversation.service;

import com.sep.realvista.application.conversation.dto.ConversationResponse;
import com.sep.realvista.application.conversation.dto.CreateConversationRequest;
import com.sep.realvista.application.conversation.mapper.ConversationMapper;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.conversation.Conversation;
import com.sep.realvista.domain.conversation.ConversationRepository;
import com.sep.realvista.domain.conversation.UserConversation;
import com.sep.realvista.domain.conversation.UserConversationRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ConversationRepository conversationRepository;
    private final UserConversationRepository userConversationRepository;
    private final UserDomainService userDomainService;
    private final ConversationMapper conversationMapper;

    /**
     * Create a new 1-1 conversation between the current user and target user.
     * If a conversation already exists between these users, returns the existing conversation.
     *
     * @param currentUserId the ID of the current user
     * @param request the create conversation request containing target user ID
     * @return the conversation response with details
     * @throws BusinessConflictException if user tries to create conversation with themselves
     */
    public ConversationResponse createConversation(UUID currentUserId, CreateConversationRequest request) {
        log.info("Creating conversation between user {} and user {}", currentUserId, request.getTargetUserId());

        // Validate that user is not creating conversation with themselves
        if (currentUserId.equals(request.getTargetUserId())) {
            throw new BusinessConflictException(
                    "Cannot create conversation with yourself",
                    "SELF_CONVERSATION_NOT_ALLOWED"
            );
        }

        // Validate target user exists
        User targetUser = userDomainService.getUserOrThrow(request.getTargetUserId());
        
        // Validate current user exists
        userDomainService.getUserOrThrow(currentUserId);

        // Check if conversation already exists between these users
        List<UserConversation> currentUserConversations = userConversationRepository.findByUserId(currentUserId);
        
        for (UserConversation uc : currentUserConversations) {
            // Check if the other user in this conversation is the target user
            List<UserConversation> conversationParticipants = 
                    userConversationRepository.findByUserId(request.getTargetUserId());
            
            for (UserConversation targetUc : conversationParticipants) {
                if (uc.getConversationId().equals(targetUc.getConversationId())) {
                    log.info("Conversation already exists with ID: {}",
                            uc.getConversationId());
                    Conversation existingConversation = conversationRepository
                            .findById(uc.getConversationId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Conversation not found: " + uc.getConversationId()));
                    return conversationMapper.toResponse(existingConversation, targetUser);
                }
            }
        }

        // Create new conversation
        Conversation conversation = Conversation.create();
        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("Created conversation with ID: {}", savedConversation.getConversationId());

        // Create UserConversation entries for both participants
        UserConversation currentUserConversation = UserConversation.create(
                savedConversation.getConversationId(),
                currentUserId
        );
        userConversationRepository.save(currentUserConversation);

        UserConversation targetUserConversation = UserConversation.create(
                savedConversation.getConversationId(),
                request.getTargetUserId()
        );
        userConversationRepository.save(targetUserConversation);

        log.info("Created UserConversation entries for users {} and {}", currentUserId, request.getTargetUserId());

        return conversationMapper.toResponse(savedConversation, targetUser);
    }
}
