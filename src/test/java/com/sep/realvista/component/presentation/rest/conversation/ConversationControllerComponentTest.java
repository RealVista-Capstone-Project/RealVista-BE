package com.sep.realvista.component.presentation.rest.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.conversation.dto.request.CreateConversationRequest;
import com.sep.realvista.application.conversation.dto.request.SendMessageRequest;
import com.sep.realvista.application.conversation.dto.response.ConversationResponse;
import com.sep.realvista.application.conversation.dto.response.MessagePaginationResponse;
import com.sep.realvista.application.conversation.dto.response.SendMessageResponse;
import com.sep.realvista.application.conversation.service.ConversationApplicationService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.conversation.MessageType;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
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
    private UserDomainService userDomainService;

    private UUID currentUserId;
    private UUID otherUserId;
    private UUID conversationId;
    private SecurityUserDetails mockSecurityUser;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        mockSecurityUser = new SecurityUserDetails(
                currentUserId,
                "test@example.com",
                "hashedPassword",
                List.of(),
                true
        );

        User currentUser = User.builder()
                .userId(currentUserId)
                .email(Email.of("test@example.com"))
                .build();
        User otherUser = User.builder()
                .userId(otherUserId)
                .email(Email.of("other@example.com"))
                .build();

        when(userDomainService.getUserOrThrow(eq(currentUserId))).thenReturn(currentUser);
        when(userDomainService.getUserOrThrow(eq(otherUserId))).thenReturn(otherUser);
    }

    @Nested
    @DisplayName("GET /api/v1/conversations/users/{otherUserId}")
    class GetConversationBetweenUsers {

        @Test
        @DisplayName("Should successfully get conversation between users")
        void shouldGetConversationBetweenUsers() throws Exception {
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

            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId)
                            .with(user(mockSecurityUser)))
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
        @DisplayName("Should return 404 when conversation not found")
        void shouldReturn404WhenConversationNotFound() throws Exception {
            when(conversationApplicationService.getConversationBetweenUsers(
                    eq(currentUserId), eq(otherUserId)
            )).thenThrow(new ResourceNotFoundException(
                    "Conversation",
                    "No conversation found between users"
            ));

            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId)
                            .with(user(mockSecurityUser)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when other user does not exist")
        void shouldReturn404WhenOtherUserDoesNotExist() throws Exception {
            when(conversationApplicationService.getConversationBetweenUsers(
                    eq(currentUserId), eq(otherUserId)
            )).thenThrow(new ResourceNotFoundException(
                    "User",
                    "User not found: " + otherUserId
            ));

            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId)
                            .with(user(mockSecurityUser)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/conversations/users/{otherUserId}", otherUserId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/conversations/{conversationId}/messages")
    class GetConversationMessages {

        @Test
        @DisplayName("Should successfully get messages with default pagination")
        void shouldGetMessagesWithDefaultPagination() throws Exception {
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(currentUserId), eq(null), eq(null), eq(null)
            )).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .with(user(mockSecurityUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Messages retrieved successfully"))
                    .andExpect(jsonPath("$.data.messages").isArray());

            verify(conversationApplicationService).getConversationMessages(
                    conversationId, currentUserId, null, null, null
            );
        }

        @Test
        @DisplayName("Should successfully get messages with custom limit")
        void shouldGetMessagesWithCustomLimit() throws Exception {
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(currentUserId), eq(20), eq(null), eq(null)
            )).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .with(user(mockSecurityUser))
                            .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(conversationApplicationService).getConversationMessages(
                    conversationId, currentUserId, 20, null, null
            );
        }

        @Test
        @DisplayName("Should successfully get messages with before cursor")
        void shouldGetMessagesWithBeforeCursor() throws Exception {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(1);
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(currentUserId), eq(null), eq(beforeTime), eq(null)
            )).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .with(user(mockSecurityUser))
                            .param("before", beforeTime.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should successfully get messages with after cursor")
        void shouldGetMessagesWithAfterCursor() throws Exception {
            LocalDateTime afterTime = LocalDateTime.now().minusHours(1);
            MessagePaginationResponse mockResponse = MessagePaginationResponse.builder()
                    .messages(Collections.emptyList())
                    .pagination(null)
                    .build();

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(currentUserId), eq(null), eq(null), eq(afterTime)
            )).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .with(user(mockSecurityUser))
                            .param("after", afterTime.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 when both before and after cursors provided")
        void shouldReturn400WhenBothCursorsProvided() throws Exception {
            LocalDateTime beforeTime = LocalDateTime.now();
            LocalDateTime afterTime = LocalDateTime.now().minusHours(1);

            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(currentUserId), eq(null), eq(beforeTime), eq(afterTime)
            )).thenThrow(new BusinessConflictException(
                    "Cannot use both 'before' and 'after' cursors simultaneously",
                    "INVALID_CURSOR_COMBINATION"
            ));

            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .with(user(mockSecurityUser))
                            .param("before", beforeTime.toString())
                            .param("after", afterTime.toString()))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 404 when conversation not found")
        void shouldReturn404WhenConversationNotFound() throws Exception {
            when(conversationApplicationService.getConversationMessages(
                    eq(conversationId), eq(currentUserId), eq(null), eq(null), eq(null)
            )).thenThrow(new ResourceNotFoundException(
                    "Conversation",
                    "Conversation not found: " + conversationId
            ));

            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                            .with(user(mockSecurityUser)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/conversations/messages")
    class SendMessage {

        @Test
        @DisplayName("Should successfully send TEXT message")
        void shouldSendTextMessage() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
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
        @DisplayName("Should successfully send message and create conversation")
        void shouldSendMessageAndCreateConversation() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.conversation_created").value(true));
        }

        @Test
        @DisplayName("Should successfully send LISTING_CARD message")
        void shouldSendListingCardMessage() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.message_type").value("LISTING_CARD"));
        }

        @Test
        @DisplayName("Should successfully send CONTRACT_CARD message")
        void shouldSendContractCardMessage() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.message_type").value("CONTRACT_CARD"));
        }

        @Test
        @DisplayName("Should successfully send reply message")
        void shouldSendReplyMessage() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.reply_to_message_id")
                            .value(replyToMessageId.toString()));
        }

        @Test
        @DisplayName("Should return 400 when recipient user ID is missing")
        void shouldReturn400WhenRecipientMissing() throws Exception {
            SendMessageRequest request = SendMessageRequest.builder()
                    .messageType(MessageType.TEXT)
                    .content("Hello")
                    .build();

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when message type is missing")
        void shouldReturn400WhenMessageTypeMissing() throws Exception {
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .content("Hello")
                    .build();

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 when sending message to self")
        void shouldReturn409WhenSendingToSelf() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 409 when TEXT message missing content")
        void shouldReturn409WhenTextMessageMissingContent() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 404 when recipient user not found")
        void shouldReturn404WhenRecipientNotFound() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when reply message not found")
        void shouldReturn404WhenReplyMessageNotFound() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when reply message from different conversation")
        void shouldReturn409WhenReplyFromDifferentConversation() throws Exception {
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

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            SendMessageRequest request = SendMessageRequest.builder()
                    .recipientUserId(otherUserId)
                    .messageType(MessageType.TEXT)
                    .content("Hello")
                    .build();

            mockMvc.perform(post("/api/v1/conversations/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/conversations/users/{targetUserId}")
    class CreateOrGetConversation {

        @Test
        @DisplayName("Should return 201 and conversationCreated=true when new conversation created")
        void shouldReturn201AndConversationCreatedTrueWhenNewConversationCreated() throws Exception {
            CreateConversationRequest request = CreateConversationRequest.builder()
                    .targetUserId(otherUserId)
                    .build();

            ConversationResponse mockResponse = ConversationResponse.builder()
                    .conversationId(conversationId)
                    .otherUserId(otherUserId)
                    .otherUserName("Other User")
                    .otherUserAvatarUrl("https://example.com/avatar.jpg")
                    .createdAt(LocalDateTime.now())
                    .conversationCreated(true)
                    .build();

            when(conversationApplicationService.createOrGetConversation(
                    eq(currentUserId), eq(otherUserId)
            )).thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/conversations/users/{targetUserId}", otherUserId)
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Conversation created successfully"))
                    .andExpect(jsonPath("$.data.conversation_id").value(conversationId.toString()))
                    .andExpect(jsonPath("$.data.other_user_id").value(otherUserId.toString()))
                    .andExpect(jsonPath("$.data.conversation_created").value(true));

            verify(conversationApplicationService).createOrGetConversation(currentUserId, otherUserId);
        }

        @Test
        @DisplayName("Should return 201 and conversationCreated=false when conversation already exists")
        void shouldReturn201AndConversationCreatedFalseWhenConversationAlreadyExists() throws Exception {
            CreateConversationRequest request = CreateConversationRequest.builder()
                    .targetUserId(otherUserId)
                    .build();

            ConversationResponse mockResponse = ConversationResponse.builder()
                    .conversationId(conversationId)
                    .otherUserId(otherUserId)
                    .otherUserName("Other User")
                    .createdAt(LocalDateTime.now())
                    .conversationCreated(false)
                    .build();

            when(conversationApplicationService.createOrGetConversation(
                    eq(currentUserId), eq(otherUserId)
            )).thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/conversations/users/{targetUserId}", otherUserId)
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.conversation_created").value(false))
                    .andExpect(jsonPath("$.data.conversation_id").value(conversationId.toString()));
        }

        @Test
        @DisplayName("Should return 400 when targetUserId is not a valid UUID")
        void shouldReturn400WhenTargetUserIdInvalidFormat() throws Exception {
            mockMvc.perform(post("/api/v1/conversations/users/{targetUserId}", "invalid-uuid-format")
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 when trying to create conversation with self")
        void shouldReturn409WhenCreatingConversationWithSelf() throws Exception {
            CreateConversationRequest request = CreateConversationRequest.builder()
                    .targetUserId(currentUserId)
                    .build();

            when(conversationApplicationService.createOrGetConversation(
                    eq(currentUserId), eq(currentUserId)
            )).thenThrow(new BusinessConflictException(
                    "Cannot create a conversation with yourself",
                    "SELF_CONVERSATION_NOT_ALLOWED"
            ));

            mockMvc.perform(post("/api/v1/conversations/users/{targetUserId}", currentUserId)
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 404 when target user does not exist")
        void shouldReturn404WhenTargetUserDoesNotExist() throws Exception {
            CreateConversationRequest request = CreateConversationRequest.builder()
                    .targetUserId(otherUserId)
                    .build();

            when(conversationApplicationService.createOrGetConversation(
                    eq(currentUserId), eq(otherUserId)
            )).thenThrow(new ResourceNotFoundException(
                    "User",
                    "User not found: " + otherUserId
            ));

            mockMvc.perform(post("/api/v1/conversations/users/{targetUserId}", otherUserId)
                            .with(user(mockSecurityUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            CreateConversationRequest request = CreateConversationRequest.builder()
                    .targetUserId(otherUserId)
                    .build();

            mockMvc.perform(post("/api/v1/conversations/users/{targetUserId}", otherUserId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
