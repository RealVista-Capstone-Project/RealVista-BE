package com.sep.realvista.unit.application.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.conversation.dto.SenderInfo;
import com.sep.realvista.application.conversation.dto.request.SendMessageRequest;
import com.sep.realvista.application.conversation.dto.response.ConversationResponse;
import com.sep.realvista.application.conversation.dto.response.MessagePaginationResponse;
import com.sep.realvista.application.conversation.dto.response.MessageResponse;
import com.sep.realvista.application.conversation.dto.response.SendMessageResponse;
import com.sep.realvista.application.conversation.mapper.ConversationMapper;
import com.sep.realvista.application.conversation.mapper.MessageMapper;
import com.sep.realvista.application.conversation.service.ConversationApplicationService;
import com.sep.realvista.domain.agent.lead.LeadSource;
import com.sep.realvista.domain.agent.lead.ListingLead;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.conversation.Conversation;
import com.sep.realvista.domain.conversation.ConversationDomainService;
import com.sep.realvista.domain.conversation.ConversationRepository;
import com.sep.realvista.domain.conversation.Message;
import com.sep.realvista.domain.conversation.MessageRepository;
import com.sep.realvista.domain.conversation.MessageType;
import com.sep.realvista.domain.conversation.UserConversation;
import com.sep.realvista.domain.conversation.UserConversationRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ConversationApplicationService.
 * Tests all application service methods with comprehensive coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationApplicationService Unit Tests")
class ConversationApplicationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserConversationRepository userConversationRepository;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private ConversationDomainService conversationDomainService;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingLeadRepository leadRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConversationApplicationService conversationApplicationService;

    private UUID conversationId;
    private UUID userId1;
    private UUID userId2;
    private UUID messageId;
    private User user1;
    private User user2;
    private Conversation conversation;
    private Message message;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        messageId = UUID.randomUUID();

        user1 = User.builder()
                .userId(userId1)
                .firstName("John")
                .lastName("Doe")
                .avatarUrl("https://example.com/avatar1.jpg")
                .build();

        user2 = User.builder()
                .userId(userId2)
                .firstName("Jane")
                .lastName("Smith")
                .avatarUrl("https://example.com/avatar2.jpg")
                .build();

        conversation = Conversation.builder()
                .conversationId(conversationId)
                .build();

        message = Message.builder()
                .messageId(messageId)
                .conversationId(conversationId)
                .senderId(userId1)
                .messageType(MessageType.TEXT)
                .content("Hello, how are you?")
                .build();
    }

    // Helper methods
    private List<Message> createMessages(int count) {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(Message.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .senderId(userId1)
                    .messageType(MessageType.TEXT)
                    .content("Message " + i)
                    .build());
        }
        return messages;
    }

    private List<Message> createMessagesWithTimestamps(int count) {
        List<Message> messages = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();
        for (int i = 0; i < count; i++) {
            Message msg = Message.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .senderId(userId1)
                    .messageType(MessageType.TEXT)
                    .content("Message " + i)
                    .build();
            // Use reflection to set createdAt since it's from BaseEntity
            try {
                var field = msg.getClass().getSuperclass().getDeclaredField("createdAt");
                field.setAccessible(true);
                field.set(msg, baseTime.minusMinutes(count - i));
            } catch (Exception e) {
                // Fallback: just use the message without createdAt
            }
            messages.add(msg);
        }
        return messages;
    }

    private SenderInfo createSenderInfo() {
        return SenderInfo.builder()
                .userId(userId1)
                .name("John Doe")
                .avatarUrl("https://example.com/avatar1.jpg")
                .build();
    }

    @Nested
    @DisplayName("getConversationBetweenUsers()")
    class GetConversationBetweenUsers {

        @Test
        @DisplayName("Should retrieve conversation between two users")
        void shouldRetrieveConversationBetweenTwoUsers() {
            // Arrange
            ConversationResponse expectedResponse = ConversationResponse.builder()
                    .conversationId(conversationId)
                    .otherUserId(userId2)
                    .otherUserName("Jane Smith")
                    .otherUserAvatarUrl("https://example.com/avatar2.jpg")
                    .build();

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findConversationBetweenUsers(userId1, userId2))
                    .thenReturn(conversation);
            when(conversationMapper.toResponse(conversation, user2)).thenReturn(expectedResponse);

            // Act
            ConversationResponse result = conversationApplicationService
                    .getConversationBetweenUsers(userId1, userId2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getConversationId()).isEqualTo(conversationId);
            assertThat(result.getOtherUserId()).isEqualTo(userId2);
            verify(userDomainService).getUserOrThrow(userId1);
            verify(userDomainService).getUserOrThrow(userId2);
            verify(conversationDomainService).findConversationBetweenUsers(userId1, userId2);
            verify(conversationMapper).toResponse(conversation, user2);
        }

        @Test
        @DisplayName("Should throw exception when first user not found")
        void shouldThrowExceptionWhenFirstUserNotFound() {
            // Arrange
            when(userDomainService.getUserOrThrow(userId1))
                    .thenThrow(new ResourceNotFoundException("User", "User not found: " + userId1));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService
                    .getConversationBetweenUsers(userId1, userId2))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userDomainService).getUserOrThrow(userId1);
            verify(userDomainService, never()).getUserOrThrow(userId2);
            verify(conversationDomainService, never()).findConversationBetweenUsers(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when second user not found")
        void shouldThrowExceptionWhenSecondUserNotFound() {
            // Arrange
            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2))
                    .thenThrow(new ResourceNotFoundException("User", "User not found: " + userId2));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService
                    .getConversationBetweenUsers(userId1, userId2))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userDomainService).getUserOrThrow(userId1);
            verify(userDomainService).getUserOrThrow(userId2);
            verify(conversationDomainService, never()).findConversationBetweenUsers(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when conversation not found")
        void shouldThrowExceptionWhenConversationNotFound() {
            // Arrange
            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findConversationBetweenUsers(userId1, userId2))
                    .thenThrow(new ResourceNotFoundException("Conversation",
                            "No conversation found between users"));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService
                    .getConversationBetweenUsers(userId1, userId2))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No conversation found between users");

            verify(conversationDomainService).findConversationBetweenUsers(userId1, userId2);
        }
    }

    @Nested
    @DisplayName("getConversationMessages()")
    class GetConversationMessages {

        @Test
        @DisplayName("Should load initial messages when no cursor provided")
        void shouldLoadInitialMessagesWithNoCursor() {
            // Arrange
            List<Message> messages = createMessages(3);

            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));
            when(messageRepository.findLatestMessages(conversationId, 51))
                    .thenReturn(messages);
            when(messageMapper.toSenderInfo(any(User.class)))
                    .thenReturn(createSenderInfo());
            when(userDomainService.getUserOrThrow(any(UUID.class)))
                    .thenReturn(user1);
            when(messageMapper.toResponse(any(Message.class), any(SenderInfo.class)))
                    .thenAnswer(invocation -> {
                        Message msg = invocation.getArgument(0);
                        return MessageResponse.builder()
                                .messageId(msg.getMessageId())
                                .conversationId(msg.getConversationId())
                                .messageType(msg.getMessageType())
                                .content(msg.getContent())
                                .createdAt(msg.getCreatedAt())
                                .build();
                    });

            // Act
            MessagePaginationResponse result = conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, null, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMessages()).hasSize(3);
            assertThat(result.getPagination()).isNotNull();
            assertThat(result.getPagination().getLimit()).isEqualTo(50);
            assertThat(result.getPagination().isHasMore()).isFalse();
            verify(messageRepository).findLatestMessages(conversationId, 51);
        }

        @Test
        @DisplayName("Should load older messages when before cursor provided")
        void shouldLoadOlderMessagesWithBeforeCursor() {
            // Arrange
            LocalDateTime beforeCursor = LocalDateTime.now().minusHours(1);
            List<Message> messages = createMessagesWithTimestamps(3);

            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));
            when(messageRepository.findMessagesBeforeCursor(eq(conversationId), eq(beforeCursor), eq(51)))
                    .thenReturn(messages);
            when(messageMapper.toSenderInfo(any(User.class)))
                    .thenReturn(createSenderInfo());
            when(userDomainService.getUserOrThrow(any(UUID.class)))
                    .thenReturn(user1);
            when(messageMapper.toResponse(any(Message.class), any(SenderInfo.class)))
                    .thenAnswer(invocation -> {
                        Message msg = invocation.getArgument(0);
                        return MessageResponse.builder()
                                .messageId(msg.getMessageId())
                                .build();
                    });

            // Act
            MessagePaginationResponse result = conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, beforeCursor, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMessages()).hasSize(3);
            verify(messageRepository).findMessagesBeforeCursor(conversationId, beforeCursor, 51);
        }

        @Test
        @DisplayName("Should load newer messages when after cursor provided")
        void shouldLoadNewerMessagesWithAfterCursor() {
            // Arrange
            LocalDateTime afterCursor = LocalDateTime.now().minusHours(1);
            List<Message> messages = createMessagesWithTimestamps(3);

            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));
            when(messageRepository.findMessagesAfterCursor(eq(conversationId), eq(afterCursor), eq(51)))
                    .thenReturn(messages);
            when(messageMapper.toSenderInfo(any(User.class)))
                    .thenReturn(createSenderInfo());
            when(userDomainService.getUserOrThrow(any(UUID.class)))
                    .thenReturn(user1);
            when(messageMapper.toResponse(any(Message.class), any(SenderInfo.class)))
                    .thenAnswer(invocation -> {
                        Message msg = invocation.getArgument(0);
                        return MessageResponse.builder()
                                .messageId(msg.getMessageId())
                                .build();
                    });

            // Act
            MessagePaginationResponse result = conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, null, afterCursor);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMessages()).hasSize(3);
            verify(messageRepository).findMessagesAfterCursor(conversationId, afterCursor, 51);
        }

        @Test
        @DisplayName("Should throw exception when both before and after cursors provided")
        void shouldThrowExceptionWhenBothCursorsProvided() {
            // Arrange
            LocalDateTime beforeCursor = LocalDateTime.now().minusHours(1);
            LocalDateTime afterCursor = LocalDateTime.now().minusHours(2);

            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, beforeCursor, afterCursor))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Cannot use both 'before' and 'after' cursors simultaneously");

            verify(messageRepository, never()).findLatestMessages(any(UUID.class), eq(51));
            verify(messageRepository, never()).findMessagesBeforeCursor(
                    any(UUID.class), any(LocalDateTime.class), eq(51));
            verify(messageRepository, never()).findMessagesAfterCursor(
                    any(UUID.class), any(LocalDateTime.class), eq(51));
        }

        @Test
        @DisplayName("Should throw exception when conversation not found")
        void shouldThrowExceptionWhenConversationNotFound() {
            // Arrange
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, null, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Conversation not found");

            verify(conversationRepository).findById(conversationId);
            verify(messageRepository, never()).findLatestMessages(any(UUID.class), eq(51));
        }

        @Test
        @DisplayName("Should return empty messages list")
        void shouldReturnEmptyMessagesList() {
            // Arrange
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));
            when(messageRepository.findLatestMessages(conversationId, 51))
                    .thenReturn(new ArrayList<>());

            // Act
            MessagePaginationResponse result = conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, null, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMessages()).isEmpty();
            assertThat(result.getPagination().isHasMore()).isFalse();
        }

        @Test
        @DisplayName("Should cap limit at MAX_LIMIT (100)")
        void shouldCapLimitAtMaxLimit() {
            // Arrange
            List<Message> messages = createMessages(10);

            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));
            when(messageRepository.findLatestMessages(conversationId, 101))
                    .thenReturn(messages);
            when(messageMapper.toSenderInfo(any(User.class)))
                    .thenReturn(createSenderInfo());
            when(userDomainService.getUserOrThrow(any(UUID.class)))
                    .thenReturn(user1);
            when(messageMapper.toResponse(any(Message.class), any(SenderInfo.class)))
                    .thenAnswer(invocation -> {
                        Message msg = invocation.getArgument(0);
                        return MessageResponse.builder()
                                .messageId(msg.getMessageId())
                                .build();
                    });

            // Act
            MessagePaginationResponse result = conversationApplicationService
                    .getConversationMessages(conversationId, userId1, 150, null, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPagination().getLimit()).isEqualTo(100);
            verify(messageRepository).findLatestMessages(conversationId, 101);
        }

        @Test
        @DisplayName("Should use default limit when not specified")
        void shouldUseDefaultLimitWhenNotSpecified() {
            // Arrange
            List<Message> messages = createMessages(10);

            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));
            when(messageRepository.findLatestMessages(conversationId, 51))
                    .thenReturn(messages);
            when(messageMapper.toSenderInfo(any(User.class)))
                    .thenReturn(createSenderInfo());
            when(userDomainService.getUserOrThrow(any(UUID.class)))
                    .thenReturn(user1);
            when(messageMapper.toResponse(any(Message.class), any(SenderInfo.class)))
                    .thenAnswer(invocation -> {
                        Message msg = invocation.getArgument(0);
                        return MessageResponse.builder()
                                .messageId(msg.getMessageId())
                                .build();
                    });

            // Act
            MessagePaginationResponse result = conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, null, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPagination().getLimit()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should indicate hasMore when more messages available")
        void shouldIndicateHasMoreWhenMoreMessagesAvailable() {
            // Arrange - Return 51 messages to show there are more (limit is 50)
            List<Message> messages = createMessagesWithTimestamps(51);

            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(conversation));
            when(userConversationRepository.findByConversationIdAndUserId(conversationId, userId1))
                    .thenReturn(Optional.of(UserConversation.builder().conversationId(conversationId).userId(userId1).build()));
            when(messageRepository.findLatestMessages(eq(conversationId), eq(51)))
                    .thenReturn(messages);
            when(messageMapper.toSenderInfo(any(User.class)))
                    .thenReturn(createSenderInfo());
            when(userDomainService.getUserOrThrow(any(UUID.class)))
                    .thenReturn(user1);
            when(messageMapper.toResponse(any(Message.class), any(SenderInfo.class)))
                    .thenAnswer(invocation -> {
                        Message msg = invocation.getArgument(0);
                        return MessageResponse.builder()
                                .messageId(msg.getMessageId())
                                .build();
                    });

            // Act
            MessagePaginationResponse result = conversationApplicationService
                    .getConversationMessages(conversationId, userId1, null, null, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMessages()).hasSize(50); // Trimmed to limit
            assertThat(result.getPagination().isHasMore()).isTrue();
            assertThat(result.getPagination().getNextCursor()).isNotNull();
        }
    }

    @Nested
    @DisplayName("sendMessage()")
    class SendMessage {

        @Test
        @DisplayName("Should send message to existing conversation")
        void shouldSendMessageToExistingConversation() {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId2)
                    .messageType(MessageType.TEXT)
                    .content("Hello, how are you?")
                    .build();

            ConversationDomainService.ConversationResult conversationResult =
                    new ConversationDomainService.ConversationResult(conversation, false);

            SenderInfo senderInfo = createSenderInfo();

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findOrCreateConversation(userId1, userId2))
                    .thenReturn(conversationResult);
            when(messageRepository.save(any(Message.class))).thenReturn(message);
            when(messageMapper.toSenderInfo(user1)).thenReturn(senderInfo);

            // Act
            SendMessageResponse result = conversationApplicationService
                    .sendMessage(userId1, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMessageId()).isEqualTo(messageId);
            assertThat(result.getConversationId()).isEqualTo(conversationId);
            assertThat(result.isConversationCreated()).isFalse();
            verify(conversationDomainService).validateMessageContent(
                    MessageType.TEXT, "Hello, how are you?", null);
            verify(messageRepository).save(any(Message.class));
        }

        @Test
        @DisplayName("Should send message and create new conversation")
        void shouldSendMessageAndCreateNewConversation() {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId2)
                    .messageType(MessageType.TEXT)
                    .content("Hello, nice to meet you!")
                    .build();

            ConversationDomainService.ConversationResult conversationResult =
                    new ConversationDomainService.ConversationResult(conversation, true);

            SenderInfo senderInfo = createSenderInfo();

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findOrCreateConversation(userId1, userId2))
                    .thenReturn(conversationResult);
            when(messageRepository.save(any(Message.class))).thenReturn(message);
            when(messageMapper.toSenderInfo(user1)).thenReturn(senderInfo);

            // Act
            SendMessageResponse result = conversationApplicationService
                    .sendMessage(userId1, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isConversationCreated()).isTrue();
            verify(conversationDomainService).findOrCreateConversation(userId1, userId2);
        }

        @Test
        @DisplayName("Should throw exception when sender not found")
        void shouldThrowExceptionWhenSenderNotFound() {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId2)
                    .messageType(MessageType.TEXT)
                    .content("Hello")
                    .build();

            when(userDomainService.getUserOrThrow(userId1))
                    .thenThrow(new ResourceNotFoundException("User", "User not found: " + userId1));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService.sendMessage(userId1, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userDomainService).getUserOrThrow(userId1);
            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when recipient not found")
        void shouldThrowExceptionWhenRecipientNotFound() {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId2)
                    .messageType(MessageType.TEXT)
                    .content("Hello")
                    .build();

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2))
                    .thenThrow(new ResourceNotFoundException("User", "User not found: " + userId2));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService.sendMessage(userId1, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when attempting self-messaging")
        void shouldThrowExceptionWhenAttemptingSelfMessaging() {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId1) // Same as sender
                    .messageType(MessageType.TEXT)
                    .content("Hello to myself")
                    .build();

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService.sendMessage(userId1, request))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Cannot send message to yourself");

            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should send TEXT message with content")
        void shouldSendTextMessageWithContent() {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId2)
                    .messageType(MessageType.TEXT)
                    .content("This is a text message")
                    .build();

            ConversationDomainService.ConversationResult conversationResult =
                    new ConversationDomainService.ConversationResult(conversation, false);

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findOrCreateConversation(userId1, userId2))
                    .thenReturn(conversationResult);
            when(messageRepository.save(any(Message.class))).thenReturn(message);
            when(messageMapper.toSenderInfo(user1)).thenReturn(createSenderInfo());

            // Act
            SendMessageResponse result = conversationApplicationService
                    .sendMessage(userId1, request);

            // Assert
            assertThat(result).isNotNull();
            verify(conversationDomainService).validateMessageContent(
                    MessageType.TEXT, "This is a text message", null);
        }

        @Test
        @DisplayName("Should send LISTING_CARD message with metadata")
        void shouldSendListingCardMessageWithMetadata() throws Exception {
            // Arrange
            UUID listingId = UUID.randomUUID();
            String metadata = "{\"id\":\"" + listingId + "\"}";
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId2)
                    .messageType(MessageType.LISTING_CARD)
                    .metadata(metadata)
                    .build();

            ConversationDomainService.ConversationResult conversationResult =
                    new ConversationDomainService.ConversationResult(conversation, false);
            Listing listing = Listing.builder()
                    .listingId(listingId)
                    .userId(userId2)
                    .build();
            JsonNode rootNode = new ObjectMapper().readTree(metadata);

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findOrCreateConversation(userId1, userId2))
                    .thenReturn(conversationResult);
            when(messageRepository.save(any(Message.class))).thenReturn(message);
            when(messageMapper.toSenderInfo(user1)).thenReturn(createSenderInfo());
            when(objectMapper.readTree(metadata)).thenReturn(rootNode);
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(leadRepository.findByAgentIdAndBuyerIdAndListingId(userId2, userId1, listingId))
                    .thenReturn(Optional.empty());
            when(leadRepository.save(any(ListingLead.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            SendMessageResponse result = conversationApplicationService
                    .sendMessage(userId1, request);

            // Assert
            assertThat(result).isNotNull();
            verify(conversationDomainService).validateMessageContent(
                    MessageType.LISTING_CARD, null, metadata);
            ArgumentCaptor<ListingLead> leadCaptor = ArgumentCaptor.forClass(ListingLead.class);
            verify(leadRepository).save(leadCaptor.capture());
            ListingLead lead = leadCaptor.getValue();
            assertThat(lead.getAgentId()).isEqualTo(userId2);
            assertThat(lead.getBuyerId()).isEqualTo(userId1);
            assertThat(lead.getListingId()).isEqualTo(listingId);
            assertThat(lead.getConversationId()).isEqualTo(conversationId);
            assertThat(lead.getSource()).isEqualTo(LeadSource.CHAT);
        }

        @Test
        @DisplayName("Should send message with reply reference")
        void shouldSendMessageWithReplyReference() {
            // Arrange
            UUID replyToMessageId = UUID.randomUUID();
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(userId2)
                    .messageType(MessageType.TEXT)
                    .content("This is a reply")
                    .replyToMessageId(replyToMessageId)
                    .build();

            ConversationDomainService.ConversationResult conversationResult =
                    new ConversationDomainService.ConversationResult(conversation, false);

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findOrCreateConversation(userId1, userId2))
                    .thenReturn(conversationResult);
            when(messageRepository.save(any(Message.class))).thenReturn(message);
            when(messageMapper.toSenderInfo(user1)).thenReturn(createSenderInfo());

            // Act
            SendMessageResponse result = conversationApplicationService
                    .sendMessage(userId1, request);

            // Assert
            assertThat(result).isNotNull();
            verify(conversationDomainService).validateReplyMessage(replyToMessageId, conversationId);
        }
    }

    @Nested
    @DisplayName("createOrGetConversation()")
    class CreateOrGetConversation {

        @Test
        @DisplayName("Should return new conversation when it does not exist")
        void shouldReturnNewConversationWhenDoesNotExist() {
            // Arrange
            ConversationDomainService.ConversationResult conversationResult =
                    new ConversationDomainService.ConversationResult(conversation, true);

            ConversationResponse mappedResponse = ConversationResponse.builder()
                    .conversationId(conversationId)
                    .otherUserId(userId2)
                    .build();

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findOrCreateConversation(userId1, userId2))
                    .thenReturn(conversationResult);
            when(conversationMapper.toResponse(conversation, user2)).thenReturn(mappedResponse);

            // Act
            ConversationResponse result = conversationApplicationService.createOrGetConversation(userId1, userId2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isConversationCreated()).isTrue();
            verify(userDomainService).getUserOrThrow(userId1);
            verify(userDomainService).getUserOrThrow(userId2);
            verify(conversationDomainService).findOrCreateConversation(userId1, userId2);
        }

        @Test
        @DisplayName("Should return existing conversation when it exists")
        void shouldReturnExistingConversationWhenItExists() {
            // Arrange
            ConversationDomainService.ConversationResult conversationResult =
                    new ConversationDomainService.ConversationResult(conversation, false);

            ConversationResponse mappedResponse = ConversationResponse.builder()
                    .conversationId(conversationId)
                    .otherUserId(userId2)
                    .build();

            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2)).thenReturn(user2);
            when(conversationDomainService.findOrCreateConversation(userId1, userId2))
                    .thenReturn(conversationResult);
            when(conversationMapper.toResponse(conversation, user2)).thenReturn(mappedResponse);

            // Act
            ConversationResponse result = conversationApplicationService.createOrGetConversation(userId1, userId2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isConversationCreated()).isFalse();
            verify(userDomainService).getUserOrThrow(userId1);
            verify(userDomainService).getUserOrThrow(userId2);
            verify(conversationDomainService).findOrCreateConversation(userId1, userId2);
        }

        @Test
        @DisplayName("Should throw exception when attempting to create conversation with self")
        void shouldThrowExceptionWhenAttemptingSelfConversation() {
            // Arrange
            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService.createOrGetConversation(userId1, userId1))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Cannot create a conversation with yourself");

            verify(conversationDomainService, never()).findOrCreateConversation(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when requester user not found")
        void shouldThrowExceptionWhenRequesterNotFound() {
            // Arrange
            when(userDomainService.getUserOrThrow(userId1))
                    .thenThrow(new ResourceNotFoundException("User", "User not found"));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService.createOrGetConversation(userId1, userId2))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(conversationDomainService, never()).findOrCreateConversation(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when target user not found")
        void shouldThrowExceptionWhenTargetNotFound() {
            // Arrange
            when(userDomainService.getUserOrThrow(userId1)).thenReturn(user1);
            when(userDomainService.getUserOrThrow(userId2))
                    .thenThrow(new ResourceNotFoundException("User", "User not found"));

            // Act & Assert
            assertThatThrownBy(() -> conversationApplicationService.createOrGetConversation(userId1, userId2))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(conversationDomainService, never()).findOrCreateConversation(any(), any());
        }
    }
}
