package com.sep.realvista.component.presentation.rest.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.conversation.dto.request.SendMessageRequest;
import com.sep.realvista.application.conversation.dto.response.ConversationResponse;
import com.sep.realvista.application.conversation.dto.response.MessagePaginationResponse;
import com.sep.realvista.application.conversation.dto.response.SendMessageResponse;
import com.sep.realvista.application.conversation.service.ConversationApplicationService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.conversation.MessageType;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.presentation.common.util.ControllerUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for ConversationController.
 * Tests all endpoints with happy paths and edge cases.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ConversationController Component Tests")
class ConversationControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConversationApplicationService conversationApplicationService;

    @MockitoBean
    private ControllerUtils controllerUtils;

    private UUID currentUserId;
    private UUID otherUserId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        User mockUser = User.builder()
                .userId(currentUserId)
                .email(Email.of("test@example.com"))
                .businessName("Test User")
                .passwordHash("hashedPassword")
                .build();

        // Mock ControllerUtils
        when(controllerUtils.initializeTraceId()).thenReturn(UUID.randomUUID().toString());
        when(controllerUtils.getCurrentUser(any())).thenReturn(mockUser);
    }

    @Nested
    @DisplayName("GET /api/v1/conversations/users/{otherUserId}")
    class GetConversationBetweenUsers {

        @Test
        @WithMockUser
        @DisplayName("Should successfully get conversation between users")
        void shouldGetConversationBetweenUsers() throws Exception {
            // Arrange
            ConversationResponse mockResponse = ConversationResponse.builder()
                    .conversationId(conversationId)
                    .otherUserId(otherUserId)
                    .otherUserName("Other User")
                    .otherUserAvatarUrl("https://example.com/avatar.jpg")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(conversationApplicationService.getConversationBetweenUsers(
                    eq(currentUserId), eq(otherUserId)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Conversation retrieved successfully"))
                    .andExpect(jsonPath("$.data.conversation_id").value(conversationId.toString()))
                    .andExpect(jsonPath("$.data.other_user_id").value(otherUserId.toString()))
                    .andExpect(jsonPath("$.data.other_user_name").value("Other User"));

            verify(conversationApplicationService).getConversationBetweenUsers(
                    currentUserId, otherUserId
            );
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when conversation not found")
        void shouldReturn404WhenConversationNotFound() throws Exception {
            // Arrange
            when(conversationApplicationService.getConversationBetweenUsers(
                    eq(currentUserId), eq(otherUserId)
            )).thenThrow(new ResourceNotFoundException(
                    "Conversation",
                    "No conversation found between users"
            ));

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when other user does not exist")
        void shouldReturn404WhenOtherUserDoesNotExist() throws Exception {
            // Arrange
            when(conversationApplicationService.getConversationBetweenUsers(
                    eq(currentUserId), eq(otherUserId)
            )).thenThrow(new ResourceNotFoundException(
                    "User",
                    "User not found: " + otherUserId
            ));

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/conversations/{conversationId}/messages")
    class GetConversationMessages {

        @Test
        @WithMockUser
        @DisplayName("Should successfully get messages with default pagination")
        void shouldGetMessagesWithDefaultPagination() throws Exception {
            // Arrange
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(null), eq(null), eq(null)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Messages retrieved successfully"))
                    .andExpect(jsonPath("$.data.messages").isArray());

            verify(conversationApplicationService).getConversationMessages(
                    conversationId, null, null, null
            );
        }

        @Test
        @WithMockUser
        @DisplayName("Should successfully get messages with custom limit")
        void shouldGetMessagesWithCustomLimit() throws Exception {
            // Arrange
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(20), eq(null), eq(null)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(conversationApplicationService).getConversationMessages(
                    conversationId, 20, null, null
            );
        }

        @Test
        @WithMockUser
        @DisplayName("Should successfully get messages with before cursor")
        void shouldGetMessagesWithBeforeCursor() throws Exception {
            // Arrange
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(1);
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(null), eq(beforeTime), eq(null)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .param("before", beforeTime.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("Should successfully get messages with after cursor")
        void shouldGetMessagesWithAfterCursor() throws Exception {
            // Arrange
            LocalDateTime afterTime = LocalDateTime.now().minusHours(1);
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(null), eq(null), eq(afterTime)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .param("after", afterTime.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 when both before and after cursors provided")
        void shouldReturn400WhenBothCursorsProvided() throws Exception {
            // Arrange
            LocalDateTime beforeTime = LocalDateTime.now();
            LocalDateTime afterTime = LocalDateTime.now().minusHours(1);

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(null), eq(beforeTime), eq(afterTime)
            )).thenThrow(new BusinessConflictException(
                    "Cannot use both 'before' and 'after' cursors simultaneously",
                    "INVALID_CURSOR_COMBINATION"
            ));

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .param("before", beforeTime.toString())
                            .param("after", afterTime.toString()))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when conversation not found")
        void shouldReturn404WhenConversationNotFound() throws Exception {
            // Arrange
            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(null), eq(null), eq(null)
            )).thenThrow(new ResourceNotFoundException(
                    "Conversation",
                    "Conversation not found: " + conversationId
            ));

            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/conversations/messages")
    class SendMessage {

        @Test
        @WithMockUser
        @DisplayName("Should successfully send TEXT message")
        void shouldSendTextMessage() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("Hello, this is a test message")
                    .build();

            SendMessageResponse mockResponse = SendMessageResponse.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("Hello, this is a test message")
                    .conversationCreated(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Message sent successfully"))
                    .andExpect(jsonPath("$.data.message_type").value("TEXT"))
                    .andExpect(jsonPath("$.data.content").value("Hello, this is a test message"))
                    .andExpect(jsonPath("$.data.conversation_created").value(false));

            verify(conversationApplicationService).sendMessage(eq(currentUserId), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should successfully send message and create conversation")
        void shouldSendMessageAndCreateConversation() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("First message")
                    .build();

            SendMessageResponse mockResponse = SendMessageResponse.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("First message")
                    .conversationCreated(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.conversation_created").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("Should successfully send LISTING_CARD message")
        void shouldSendListingCardMessage() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.LISTING_CARD)
                    .metadata("{\"listing_id\":\"123\",\"title\":\"Modern Apartment\"}")
                    .build();

            SendMessageResponse mockResponse = SendMessageResponse.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.LISTING_CARD)
                    .metadata("{\"listing_id\":\"123\",\"title\":\"Modern Apartment\"}")
                    .conversationCreated(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.message_type").value("LISTING_CARD"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should successfully send CONTRACT_CARD message")
        void shouldSendContractCardMessage() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.CONTRACT_CARD)
                    .metadata("{\"contract_id\":\"456\",\"status\":\"pending\"}")
                    .build();

            SendMessageResponse mockResponse = SendMessageResponse.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.CONTRACT_CARD)
                    .metadata("{\"contract_id\":\"456\",\"status\":\"pending\"}")
                    .conversationCreated(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.message_type").value("CONTRACT_CARD"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should successfully send reply message")
        void shouldSendReplyMessage() throws Exception {
            // Arrange
            UUID replyToMessageId = UUID.randomUUID();
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("This is a reply")
                    .replyToMessageId(replyToMessageId)
                    .build();

            SendMessageResponse mockResponse = SendMessageResponse.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("This is a reply")
                    .replyToMessageId(replyToMessageId)
                    .conversationCreated(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.reply_to_message_id")
                            .value(replyToMessageId.toString()));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 when recipient user ID is missing")
        void shouldReturn400WhenRecipientMissing() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .messageType(MessageType.TEXT)
                    .content("Hello")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 when message type is missing")
        void shouldReturn400WhenMessageTypeMissing() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .content("Hello")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 409 when sending message to self")
        void shouldReturn409WhenSendingToSelf() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(currentUserId)
                    .messageType(MessageType.TEXT)
                    .content("Message to myself")
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenThrow(new BusinessConflictException(
                    "Cannot send message to yourself",
                    "SELF_MESSAGING_NOT_ALLOWED"
            ));

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 409 when TEXT message missing content")
        void shouldReturn409WhenTextMessageMissingContent() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenThrow(new BusinessConflictException(
                    "Content is required for TEXT messages",
                    "MISSING_MESSAGE_CONTENT"
            ));

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when recipient user not found")
        void shouldReturn404WhenRecipientNotFound() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("Hello")
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenThrow(new ResourceNotFoundException(
                    "User",
                    "User not found: " + otherUserId
            ));

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when reply message not found")
        void shouldReturn404WhenReplyMessageNotFound() throws Exception {
            // Arrange
            UUID invalidReplyId = UUID.randomUUID();
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("Reply to non-existent message")
                    .replyToMessageId(invalidReplyId)
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenThrow(new ResourceNotFoundException(
                    "Message",
                    "Reply message not found: " + invalidReplyId
            ));

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 409 when reply message from different conversation")
        void shouldReturn409WhenReplyFromDifferentConversation() throws Exception {
            // Arrange
            UUID replyToMessageId = UUID.randomUUID();
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("Invalid reply")
                    .replyToMessageId(replyToMessageId)
                    .build();

            when(conversationApplicationService.sendMessage(
                    eq(currentUserId), any(SendMessageRequest.class)
            )).thenThrow(new BusinessConflictException(
                    "Reply message must belong to the same conversation",
                    "INVALID_REPLY_MESSAGE"
            ));

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Arrange
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("Hello")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
