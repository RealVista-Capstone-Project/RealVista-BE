package com.sep.realvista.unit.domain.conversation;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConversationDomainService.
 * Tests all domain logic methods with comprehensive coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationDomainService Unit Tests")
class ConversationDomainServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserConversationRepository userConversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ConversationDomainService conversationDomainService;

    private UUID conversationId;
    private UUID userId1;
    private UUID userId2;
    private UUID messageId;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("validateMessageContent()")
    class ValidateMessageContent {

        @Test
        @DisplayName("Should validate TEXT message with content")
        void shouldValidateTextMessageWithContent() {
            // Act & Assert - should not throw
            conversationDomainService.validateMessageContent(
                    MessageType.TEXT,
                    "Hello, this is a message",
                    null
            );
        }

        @Test
        @DisplayName("Should throw exception when TEXT message missing content")
        void shouldThrowExceptionWhenTextMessageMissingContent() {
            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.validateMessageContent(
                    MessageType.TEXT,
                    null,
                    null
            ))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Content is required for TEXT messages");
        }

        @Test
        @DisplayName("Should throw exception when TEXT message has blank content")
        void shouldThrowExceptionWhenTextMessageHasBlankContent() {
            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.validateMessageContent(
                    MessageType.TEXT,
                    "   ",
                    null
            ))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Content is required for TEXT messages");
        }

        @Test
        @DisplayName("Should validate LISTING_CARD message with metadata")
        void shouldValidateListingCardMessageWithMetadata() {
            // Act & Assert - should not throw
            conversationDomainService.validateMessageContent(
                    MessageType.LISTING_CARD,
                    null,
                    "{\"listing_id\":\"123\"}"
            );
        }

        @Test
        @DisplayName("Should throw exception when LISTING_CARD missing metadata")
        void shouldThrowExceptionWhenListingCardMissingMetadata() {
            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.validateMessageContent(
                    MessageType.LISTING_CARD,
                    null,
                    null
            ))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Metadata is required for LISTING_CARD messages");
        }

        @Test
        @DisplayName("Should validate CONTRACT_CARD message with metadata")
        void shouldValidateContractCardMessageWithMetadata() {
            // Act & Assert - should not throw
            conversationDomainService.validateMessageContent(
                    MessageType.CONTRACT_CARD,
                    null,
                    "{\"contract_id\":\"456\"}"
            );
        }

        @Test
        @DisplayName("Should throw exception when CONTRACT_CARD missing metadata")
        void shouldThrowExceptionWhenContractCardMissingMetadata() {
            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.validateMessageContent(
                    MessageType.CONTRACT_CARD,
                    null,
                    null
            ))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Metadata is required for CONTRACT_CARD messages");
        }

        @Test
        @DisplayName("Should throw exception for SYSTEM message type")
        void shouldThrowExceptionForSystemMessageType() {
            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.validateMessageContent(
                    MessageType.SYSTEM,
                    "System message",
                    null
            ))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Cannot send SYSTEM messages via API");
        }
    }

    @Nested
    @DisplayName("findConversationBetweenUsers()")
    class FindConversationBetweenUsers {

        @Test
        @DisplayName("Should find existing conversation between two users")
        void shouldFindExistingConversation() {
            // Arrange
            Conversation mockConversation = Conversation.builder()
                    .conversationId(conversationId)
                    .build();

            when(userConversationRepository.findConversationIdBetweenUsers(userId1, userId2))
                    .thenReturn(Optional.of(conversationId));
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(mockConversation));

            // Act
            Conversation result = conversationDomainService.findConversationBetweenUsers(userId1, userId2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getConversationId()).isEqualTo(conversationId);
            verify(userConversationRepository).findConversationIdBetweenUsers(userId1, userId2);
            verify(conversationRepository).findById(conversationId);
        }

        @Test
        @DisplayName("Should throw exception when conversation not found in user conversations")
        void shouldThrowExceptionWhenConversationNotFoundInUserConversations() {
            // Arrange
            when(userConversationRepository.findConversationIdBetweenUsers(userId1, userId2))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.findConversationBetweenUsers(userId1, userId2))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No conversation found between users");

            verify(userConversationRepository).findConversationIdBetweenUsers(userId1, userId2);
            verify(conversationRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when conversation ID found but entity missing")
        void shouldThrowExceptionWhenConversationEntityMissing() {
            // Arrange
            when(userConversationRepository.findConversationIdBetweenUsers(userId1, userId2))
                    .thenReturn(Optional.of(conversationId));
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.findConversationBetweenUsers(userId1, userId2))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Conversation not found: " + conversationId);

            verify(conversationRepository).findById(conversationId);
        }
    }

    @Nested
    @DisplayName("createConversationWithParticipants()")
    class CreateConversationWithParticipants {

        @Test
        @DisplayName("Should create conversation with two participants")
        void shouldCreateConversationWithParticipants() {
            // Arrange
            Conversation newConversation = Conversation.builder()
                    .conversationId(conversationId)
                    .build();

            when(conversationRepository.save(any(Conversation.class)))
                    .thenReturn(newConversation);
            when(userConversationRepository.save(any(UserConversation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Conversation result = conversationDomainService.createConversationWithParticipants(userId1, userId2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getConversationId()).isEqualTo(conversationId);

            // Verify conversation was created
            verify(conversationRepository).save(any(Conversation.class));

            // Verify both user conversation entries were created
            verify(userConversationRepository, times(2)).save(any(UserConversation.class));
        }

        @Test
        @DisplayName("Should create UserConversation entries for both users")
        void shouldCreateUserConversationEntriesForBothUsers() {
            // Arrange
            Conversation newConversation = Conversation.builder()
                    .conversationId(conversationId)
                    .build();

            when(conversationRepository.save(any(Conversation.class)))
                    .thenReturn(newConversation);

            // Act
            conversationDomainService.createConversationWithParticipants(userId1, userId2);

            // Assert - verify UserConversation.save was called twice
            verify(userConversationRepository, times(2)).save(any(UserConversation.class));
        }
    }

    @Nested
    @DisplayName("validateReplyMessage()")
    class ValidateReplyMessage {

        @Test
        @DisplayName("Should validate reply message belonging to same conversation")
        void shouldValidateReplyMessageInSameConversation() {
            // Arrange
            Message replyMessage = Message.builder()
                    .messageId(messageId)
                    .conversationId(conversationId)
                    .build();

            when(messageRepository.findById(messageId))
                    .thenReturn(Optional.of(replyMessage));

            // Act & Assert - should not throw
            conversationDomainService.validateReplyMessage(messageId, conversationId);

            verify(messageRepository).findById(messageId);
        }

        @Test
        @DisplayName("Should throw exception when reply message not found")
        void shouldThrowExceptionWhenReplyMessageNotFound() {
            // Arrange
            when(messageRepository.findById(messageId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.validateReplyMessage(messageId, conversationId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reply message not found: " + messageId);

            verify(messageRepository).findById(messageId);
        }

        @Test
        @DisplayName("Should throw exception when reply message from different conversation")
        void shouldThrowExceptionWhenReplyMessageFromDifferentConversation() {
            // Arrange
            UUID differentConversationId = UUID.randomUUID();
            Message replyMessage = Message.builder()
                    .messageId(messageId)
                    .conversationId(differentConversationId)
                    .build();

            when(messageRepository.findById(messageId))
                    .thenReturn(Optional.of(replyMessage));

            // Act & Assert
            assertThatThrownBy(() -> conversationDomainService.validateReplyMessage(messageId, conversationId))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Reply message must belong to the same conversation");

            verify(messageRepository).findById(messageId);
        }
    }

    @Nested
    @DisplayName("findOrCreateConversation()")
    class FindOrCreateConversation {

        @Test
        @DisplayName("Should return existing conversation when found")
        void shouldReturnExistingConversationWhenFound() {
            // Arrange
            Conversation existingConversation = Conversation.builder()
                    .conversationId(conversationId)
                    .build();

            when(userConversationRepository.findConversationIdBetweenUsers(userId1, userId2))
                    .thenReturn(Optional.of(conversationId));
            when(conversationRepository.findById(conversationId))
                    .thenReturn(Optional.of(existingConversation));

            // Act
            ConversationDomainService.ConversationResult result =
                    conversationDomainService.findOrCreateConversation(userId1, userId2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.conversation()).isEqualTo(existingConversation);
            assertThat(result.created()).isFalse();

            verify(userConversationRepository).findConversationIdBetweenUsers(userId1, userId2);
            verify(conversationRepository).findById(conversationId);
            verify(conversationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create new conversation when not found")
        void shouldCreateNewConversationWhenNotFound() {
            // Arrange
            Conversation newConversation = Conversation.builder()
                    .conversationId(conversationId)
                    .build();

            when(userConversationRepository.findConversationIdBetweenUsers(userId1, userId2))
                    .thenReturn(Optional.empty());
            when(conversationRepository.save(any(Conversation.class)))
                    .thenReturn(newConversation);
            when(userConversationRepository.save(any(UserConversation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ConversationDomainService.ConversationResult result =
                    conversationDomainService.findOrCreateConversation(userId1, userId2);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.conversation()).isEqualTo(newConversation);
            assertThat(result.created()).isTrue();

            verify(userConversationRepository).findConversationIdBetweenUsers(userId1, userId2);
            verify(conversationRepository).save(any(Conversation.class));
            verify(userConversationRepository, times(2)).save(any(UserConversation.class));
        }
    }
}
