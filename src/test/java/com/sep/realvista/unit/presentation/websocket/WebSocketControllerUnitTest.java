package com.sep.realvista.unit.presentation.websocket;

import com.sep.realvista.application.conversation.dto.response.SendMessageResponse;
import com.sep.realvista.application.conversation.service.ConversationApplicationService;
import com.sep.realvista.application.websocket.dto.ChatWebSocketMessage;
import com.sep.realvista.application.websocket.dto.WebSocketMessage;
import com.sep.realvista.domain.conversation.MessageType;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import com.sep.realvista.domain.common.value.Email;

import com.sep.realvista.presentation.websocket.WebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketController.
 * Tests message handling logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketControllerUnitTest {

    private WebSocketController webSocketController;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ConversationApplicationService conversationApplicationService;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @Mock
    private Principal principal;

    @BeforeEach
    void setup() {
        webSocketController = new WebSocketController(
                messagingTemplate,
                conversationApplicationService,
                userDomainService
        );
    }

    // ==================== PUBLIC ENDPOINT TESTS ====================

    @Nested
    @DisplayName("handlePublicMessage")
    class HandlePublicMessage {

        @Test
        @DisplayName("should return message unchanged")
        void withValidMessage_shouldReturnMessage() {
            when(headerAccessor.getSessionId()).thenReturn("test-session-123");

            WebSocketMessage inputMessage = WebSocketMessage.builder()
                    .type("PUBLIC_ANNOUNCEMENT")
                    .payload("Server maintenance at 10 PM")
                    .build();

            WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo("PUBLIC_ANNOUNCEMENT");
            assertThat(result.getPayload()).isEqualTo("Server maintenance at 10 PM");
        }

        @Test
        @DisplayName("should preserve all message fields")
        void withCompleteMessage_shouldPreserveAllFields() {
            when(headerAccessor.getSessionId()).thenReturn("session-456");

            WebSocketMessage inputMessage = WebSocketMessage.builder()
                    .type("DATA_UPDATE")
                    .payload("Test data")
                    .metadata("channel-1")
                    .senderId(999L)
                    .senderName("old-sender")
                    .build();

            WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo("DATA_UPDATE");
            assertThat(result.getPayload()).isEqualTo("Test data");
            assertThat(result.getMetadata()).isEqualTo("channel-1");
        }

        @Test
        @DisplayName("should handle null payload")
        void withNullPayload_shouldHandleGracefully() {
            when(headerAccessor.getSessionId()).thenReturn("session-789");

            WebSocketMessage inputMessage = WebSocketMessage.builder()
                    .type("EMPTY_MESSAGE")
                    .payload(null)
                    .build();

            WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo("EMPTY_MESSAGE");
            assertThat(result.getPayload()).isNull();
        }

        @Test
        @DisplayName("should handle null session ID")
        void withNullSessionId_shouldHandleGracefully() {
            when(headerAccessor.getSessionId()).thenReturn(null);

            WebSocketMessage inputMessage = WebSocketMessage.builder()
                    .type("NO_SESSION")
                    .payload("Message without session")
                    .build();

            WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo("NO_SESSION");
        }
    }

    // ==================== CHAT MESSAGE TESTS ====================

    @Nested
    @DisplayName("handleChatMessage")
    class HandleChatMessage {

        private final UUID senderId = UUID.randomUUID();
        private final UUID recipientId = UUID.randomUUID();
        private final UUID conversationId = UUID.randomUUID();

        private User senderUser;
        private User recipientUser;

        @BeforeEach
        void setupUsers() {
            senderUser = mock(User.class);
            recipientUser = mock(User.class);

            Email recipientEmail = mock(Email.class);

            lenient().when(senderUser.getUserId()).thenReturn(senderId);
            lenient().when(recipientUser.getEmail()).thenReturn(recipientEmail);
            lenient().when(recipientEmail.getValue()).thenReturn("recipient@example.com");
        }

        @Test
        @DisplayName("should return early when principal is null")
        void withNullPrincipal_shouldReturnEarly() {
            ChatWebSocketMessage message = ChatWebSocketMessage.builder()
                    .conversationId(conversationId)
                    .recipientUserId(recipientId)
                    .messageType("TEXT")
                    .content("Hello")
                    .build();

            webSocketController.handleChatMessage(message, null);

            verifyNoInteractions(conversationApplicationService);
            verifyNoInteractions(messagingTemplate);
        }

        @Test
        @DisplayName("should persist message and broadcast to both users")
        void withValidMessage_shouldPersistAndBroadcast() {
            String senderEmailStr = "sender@example.com";
            when(principal.getName()).thenReturn(senderEmailStr);
            when(userDomainService.getUserByEmailOrThrow(senderEmailStr)).thenReturn(senderUser);
            when(userDomainService.getUserOrThrow(recipientId)).thenReturn(recipientUser);

            SendMessageResponse response = SendMessageResponse.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .build();
            when(conversationApplicationService.sendMessage(eq(senderId), any())).thenReturn(response);

            ChatWebSocketMessage message = ChatWebSocketMessage.builder()
                    .conversationId(conversationId)
                    .recipientUserId(recipientId)
                    .messageType("TEXT")
                    .content("Hello World")
                    .build();

            webSocketController.handleChatMessage(message, principal);

            // Verify message was persisted
            verify(conversationApplicationService).sendMessage(eq(senderId), any());

            // Verify broadcast to sender
            verify(messagingTemplate).convertAndSendToUser(
                    eq(senderEmailStr),
                    eq("/queue/messages"),
                    eq(response)
            );

            // Verify broadcast to recipient
            verify(messagingTemplate).convertAndSendToUser(
                    eq("recipient@example.com"),
                    eq("/queue/messages"),
                    eq(response)
            );
        }

        @Test
        @DisplayName("should handle service exception gracefully")
        void withServiceException_shouldHandleGracefully() {
            String senderEmailStr = "sender@example.com";
            when(principal.getName()).thenReturn(senderEmailStr);
            when(userDomainService.getUserByEmailOrThrow(senderEmailStr))
                    .thenThrow(new RuntimeException("User not found"));

            ChatWebSocketMessage message = ChatWebSocketMessage.builder()
                    .conversationId(conversationId)
                    .recipientUserId(recipientId)
                    .messageType("TEXT")
                    .content("Hello")
                    .build();

            // Should not throw
            webSocketController.handleChatMessage(message, principal);

            verifyNoInteractions(messagingTemplate);
        }

        @Test
        @DisplayName("should build correct SendMessageRequest from WebSocket message")
        void shouldBuildCorrectSendMessageRequest() {
            String senderEmailStr = "sender@example.com";
            UUID replyToId = UUID.randomUUID();

            when(principal.getName()).thenReturn(senderEmailStr);
            when(userDomainService.getUserByEmailOrThrow(senderEmailStr)).thenReturn(senderUser);
            when(userDomainService.getUserOrThrow(recipientId)).thenReturn(recipientUser);

            SendMessageResponse response = SendMessageResponse.builder()
                    .messageId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .build();
            when(conversationApplicationService.sendMessage(eq(senderId), any())).thenReturn(response);

            ChatWebSocketMessage message = ChatWebSocketMessage.builder()
                    .conversationId(conversationId)
                    .recipientUserId(recipientId)
                    .messageType("TEXT")
                    .content("Test content")
                    .metadata("{\"key\":\"value\"}")
                    .replyToMessageId(replyToId)
                    .build();

            webSocketController.handleChatMessage(message, principal);

            ArgumentCaptor<com.sep.realvista.application.conversation.dto.request.SendMessageRequest> captor =
                    ArgumentCaptor.forClass(com.sep.realvista.application.conversation.dto.request.SendMessageRequest.class);
            verify(conversationApplicationService).sendMessage(eq(senderId), captor.capture());

            var capturedRequest = captor.getValue();
            assertThat(capturedRequest.getRecipientUserId()).isEqualTo(recipientId);
            assertThat(capturedRequest.getMessageType()).isEqualTo(MessageType.TEXT);
            assertThat(capturedRequest.getContent()).isEqualTo("Test content");
            assertThat(capturedRequest.getMetadata()).isEqualTo("{\"key\":\"value\"}");
            assertThat(capturedRequest.getReplyToMessageId()).isEqualTo(replyToId);
        }
    }

    // ==================== TYPING INDICATOR TESTS ====================

    @Nested
    @DisplayName("handleTypingIndicator")
    class HandleTypingIndicator {

        private final UUID recipientId = UUID.randomUUID();
        private final UUID conversationId = UUID.randomUUID();

        @Test
        @DisplayName("should return early when principal is null")
        void withNullPrincipal_shouldReturnEarly() {
            ChatWebSocketMessage message = ChatWebSocketMessage.builder()
                    .conversationId(conversationId)
                    .recipientUserId(recipientId)
                    .build();

            webSocketController.handleTypingIndicator(message, null);

            verifyNoInteractions(messagingTemplate);
            verifyNoInteractions(userDomainService);
        }

        @Test
        @DisplayName("should forward typing event to recipient")
        void withValidMessage_shouldForwardToRecipient() {
            String senderEmailStr = "sender@example.com";
            when(principal.getName()).thenReturn(senderEmailStr);

            User recipientUser = mock(User.class);
            Email recipientEmail = mock(Email.class);
            when(recipientUser.getEmail()).thenReturn(recipientEmail);
            when(recipientEmail.getValue()).thenReturn("recipient@example.com");
            when(userDomainService.getUserOrThrow(recipientId)).thenReturn(recipientUser);

            ChatWebSocketMessage message = ChatWebSocketMessage.builder()
                    .conversationId(conversationId)
                    .recipientUserId(recipientId)
                    .build();

            webSocketController.handleTypingIndicator(message, principal);

            ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq("recipient@example.com"),
                    eq("/queue/typing"),
                    captor.capture()
            );

            WebSocketMessage typingEvent = captor.getValue();
            assertThat(typingEvent.getType()).isEqualTo("TYPING");
            assertThat(typingEvent.getSenderName()).isEqualTo(senderEmailStr);
            assertThat(typingEvent.getPayload()).isEqualTo(conversationId);
        }

        @Test
        @DisplayName("should handle recipient lookup failure gracefully")
        void withRecipientNotFound_shouldHandleGracefully() {
            when(principal.getName()).thenReturn("sender@example.com");
            when(userDomainService.getUserOrThrow(recipientId))
                    .thenThrow(new RuntimeException("User not found"));

            ChatWebSocketMessage message = ChatWebSocketMessage.builder()
                    .conversationId(conversationId)
                    .recipientUserId(recipientId)
                    .build();

            // Should not throw
            webSocketController.handleTypingIndicator(message, principal);

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }
    }
}
