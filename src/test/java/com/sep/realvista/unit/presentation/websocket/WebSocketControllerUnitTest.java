package com.sep.realvista.unit.presentation.websocket;

import com.sep.realvista.application.websocket.dto.WebSocketMessage;
import com.sep.realvista.presentation.websocket.WebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WebSocketController.
 * Tests message handling logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketControllerUnitTest {

    private WebSocketController webSocketController;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @Mock
    private Principal principal;

    @BeforeEach
    void setup() {
        webSocketController = new WebSocketController();
    }

    @Test
    @DisplayName("handlePublicMessage - should return message unchanged")
    void handlePublicMessage_withValidMessage_shouldReturnMessage() {
        // Given
        String sessionId = "test-session-123";
        when(headerAccessor.getSessionId()).thenReturn(sessionId);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("PUBLIC_ANNOUNCEMENT")
                .payload("Server maintenance at 10 PM")
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("PUBLIC_ANNOUNCEMENT");
        assertThat(result.getPayload()).isEqualTo("Server maintenance at 10 PM");
        assertThat(result.getSenderName()).isNull(); // No authentication in public endpoint
    }

    @Test
    @DisplayName("handlePublicMessage - should preserve all message fields")
    void handlePublicMessage_withCompleteMessage_shouldPreserveAllFields() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-456");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("DATA_UPDATE")
                .payload("Test data")
                .metadata("channel-1")
                .senderId(999L)
                .senderName("old-sender")
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("DATA_UPDATE");
        assertThat(result.getPayload()).isEqualTo("Test data");
        assertThat(result.getMetadata()).isEqualTo("channel-1");
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handlePublicMessage - should handle null payload")
    void handlePublicMessage_withNullPayload_shouldHandleGracefully() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-789");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("EMPTY_MESSAGE")
                .payload(null)
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("EMPTY_MESSAGE");
        assertThat(result.getPayload()).isNull();
    }

    @Test
    @DisplayName("handleSecuredMessage - should populate sender name from principal")
    void handleSecuredMessage_withAuthentication_shouldPopulateSenderName() {
        // Given
        String sessionId = "secured-session-123";
        String username = "user@example.com";

        when(headerAccessor.getSessionId()).thenReturn(sessionId);
        when(principal.getName()).thenReturn(username);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("USER_MESSAGE")
                .payload("This is a secured message")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("USER_MESSAGE");
        assertThat(result.getPayload()).isEqualTo("This is a secured message");
        assertThat(result.getSenderName()).isEqualTo(username);
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle null principal gracefully")
    void handleSecuredMessage_withNullPrincipal_shouldNotPopulateSenderName() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-no-auth");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("ANONYMOUS_MESSAGE")
                .payload("Message without auth")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("ANONYMOUS_MESSAGE");
        assertThat(result.getSenderName()).isNull();
    }

    @Test
    @DisplayName("handleSecuredMessage - should preserve existing sender name when principal is null")
    void handleSecuredMessage_withNullPrincipalAndExistingSenderName_shouldNotOverwrite() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-existing");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .senderName("existing-sender")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("existing-sender");
    }

    @Test
    @DisplayName("handleSecuredMessage - should override sender name when principal exists")
    void handleSecuredMessage_withPrincipalAndExistingSenderName_shouldOverwrite() {
        // Given
        String authenticatedUsername = "authenticated@example.com";
        when(headerAccessor.getSessionId()).thenReturn("session-override");
        when(principal.getName()).thenReturn(authenticatedUsername);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .senderName("old-sender")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo(authenticatedUsername);
    }

    @Test
    @DisplayName("handleSecuredMessage - should preserve all message fields")
    void handleSecuredMessage_withCompleteMessage_shouldPreserveAllFields() {
        // Given
        String username = "test@example.com";
        when(headerAccessor.getSessionId()).thenReturn("session-complete");
        when(principal.getName()).thenReturn(username);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("COMPLEX_MESSAGE")
                .payload(new TestData("value", 42))
                .metadata("metadata-info")
                .senderId(123L)
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("COMPLEX_MESSAGE");
        assertThat(result.getMetadata()).isEqualTo("metadata-info");
        assertThat(result.getSenderId()).isEqualTo(123L);
        assertThat(result.getSenderName()).isEqualTo(username);
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle different user principals")
    void handleSecuredMessage_withDifferentUsers_shouldUseCorrectSenderName() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-multi-user");

        WebSocketMessage message1 = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("From user 1")
                .build();

        WebSocketMessage message2 = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("From user 2")
                .build();

        Principal user1 = () -> "user1@example.com";
        Principal user2 = () -> "user2@example.com";

        // When
        WebSocketMessage result1 = webSocketController.handleSecuredMessage(
                message1, headerAccessor, user1);
        WebSocketMessage result2 = webSocketController.handleSecuredMessage(
                message2, headerAccessor, user2);

        // Then
        assertThat(result1.getSenderName()).isEqualTo("user1@example.com");
        assertThat(result2.getSenderName()).isEqualTo("user2@example.com");
    }

    /**
     * Helper record for testing complex payloads.
     */
    private record TestData(String name, int count) {
    }
}
