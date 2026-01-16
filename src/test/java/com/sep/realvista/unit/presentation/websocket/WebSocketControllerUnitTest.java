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

    // ==================== EDGE CASE TESTS - AUTHENTICATION ====================

    @Test
    @DisplayName("handleSecuredMessage - should handle principal with empty username")
    void handleSecuredMessage_withEmptyUsername_shouldSetEmptySenderName() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-empty-user");
        when(principal.getName()).thenReturn("");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEmpty();
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle principal with whitespace-only username")
    void handleSecuredMessage_withWhitespaceUsername_shouldSetWhitespaceSenderName() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-whitespace");
        when(principal.getName()).thenReturn("   ");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("   ");
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle principal with null username")
    void handleSecuredMessage_withNullUsernameFromPrincipal_shouldSetNullSenderName() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-null-username");
        when(principal.getName()).thenReturn(null);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isNull();
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle very long username")
    void handleSecuredMessage_withVeryLongUsername_shouldPreserveLongUsername() {
        // Given
        String longUsername = "a".repeat(500) + "@example.com";
        when(headerAccessor.getSessionId()).thenReturn("session-long-user");
        when(principal.getName()).thenReturn(longUsername);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo(longUsername);
        assertThat(result.getSenderName().length()).isEqualTo(512);
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle username with special characters")
    void handleSecuredMessage_withSpecialCharactersInUsername_shouldPreserveSpecialChars() {
        // Given
        String specialUsername = "user+test@example.com!#$%&*";
        when(headerAccessor.getSessionId()).thenReturn("session-special-chars");
        when(principal.getName()).thenReturn(specialUsername);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo(specialUsername);
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle username with unicode characters")
    void handleSecuredMessage_withUnicodeUsername_shouldPreserveUnicode() {
        // Given
        String unicodeUsername = "用户@example.com";
        when(headerAccessor.getSessionId()).thenReturn("session-unicode");
        when(principal.getName()).thenReturn(unicodeUsername);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MESSAGE")
                .payload("Test")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo(unicodeUsername);
    }

    // ==================== EDGE CASE TESTS - PUBLIC ENDPOINT ====================

    @Test
    @DisplayName("handlePublicMessage - should not set sender name even if called with authentication context")
    void handlePublicMessage_whenCalledByAuthenticatedUser_shouldNotSetSenderName() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-auth-public");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("PUBLIC_MESSAGE")
                .payload("Public message from authenticated user")
                .senderName("authenticated@example.com") // Pre-set sender name
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("authenticated@example.com");
        // Public endpoint doesn't modify sender name, preserves original
    }

    @Test
    @DisplayName("handlePublicMessage - should handle message with all fields null except type")
    void handlePublicMessage_withMinimalMessage_shouldHandleGracefully() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-minimal");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("MINIMAL")
                .payload(null)
                .metadata(null)
                .senderId(null)
                .senderName(null)
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("MINIMAL");
        assertThat(result.getPayload()).isNull();
        assertThat(result.getMetadata()).isNull();
        assertThat(result.getSenderId()).isNull();
        assertThat(result.getSenderName()).isNull();
    }

    @Test
    @DisplayName("handlePublicMessage - should handle empty string type")
    void handlePublicMessage_withEmptyType_shouldPreserveEmptyType() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-empty-type");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("")
                .payload("Test payload")
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEmpty();
        assertThat(result.getPayload()).isEqualTo("Test payload");
    }

    @Test
    @DisplayName("handlePublicMessage - should handle very large payload")
    void handlePublicMessage_withLargePayload_shouldHandleLargeData() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-large-payload");
        String largePayload = "x".repeat(10000);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("LARGE_DATA")
                .payload(largePayload)
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isEqualTo(largePayload);
        assertThat(result.getPayload().toString().length()).isEqualTo(10000);
    }

    // ==================== EDGE CASE TESTS - SESSION HANDLING ====================

    @Test
    @DisplayName("handlePublicMessage - should handle null session ID")
    void handlePublicMessage_withNullSessionId_shouldHandleGracefully() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn(null);

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("NO_SESSION")
                .payload("Message without session")
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("NO_SESSION");
        // Controller should still process message even without session ID
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle null session ID with authentication")
    void handleSecuredMessage_withNullSessionId_shouldStillPopulateSenderName() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn(null);
        when(principal.getName()).thenReturn("user@example.com");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("NO_SESSION_AUTH")
                .payload("Authenticated message without session")
                .build();

        // When
        WebSocketMessage result = webSocketController.handleSecuredMessage(
                inputMessage, headerAccessor, principal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("user@example.com");
        // Sender name should still be populated even without session ID
    }

    @Test
    @DisplayName("handleSecuredMessage - should handle rapid successive messages from same user")
    void handleSecuredMessage_withRapidMessages_shouldHandleIndependently() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-rapid");
        when(principal.getName()).thenReturn("rapid@example.com");

        WebSocketMessage message1 = WebSocketMessage.builder().type("MSG1").payload("First").build();
        WebSocketMessage message2 = WebSocketMessage.builder().type("MSG2").payload("Second").build();
        WebSocketMessage message3 = WebSocketMessage.builder().type("MSG3").payload("Third").build();

        // When
        WebSocketMessage result1 = webSocketController.handleSecuredMessage(message1, headerAccessor, principal);
        WebSocketMessage result2 = webSocketController.handleSecuredMessage(message2, headerAccessor, principal);
        WebSocketMessage result3 = webSocketController.handleSecuredMessage(message3, headerAccessor, principal);

        // Then
        assertThat(result1.getSenderName()).isEqualTo("rapid@example.com");
        assertThat(result2.getSenderName()).isEqualTo("rapid@example.com");
        assertThat(result3.getSenderName()).isEqualTo("rapid@example.com");
        assertThat(result1.getPayload()).isEqualTo("First");
        assertThat(result2.getPayload()).isEqualTo("Second");
        assertThat(result3.getPayload()).isEqualTo("Third");
    }

    // ==================== EDGE CASE TESTS - PAYLOAD TYPES ====================

    @Test
    @DisplayName("handlePublicMessage - should handle numeric payload")
    void handlePublicMessage_withNumericPayload_shouldPreserveNumber() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-numeric");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("NUMERIC_DATA")
                .payload(12345)
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isEqualTo(12345);
    }

    @Test
    @DisplayName("handlePublicMessage - should handle boolean payload")
    void handlePublicMessage_withBooleanPayload_shouldPreserveBoolean() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-boolean");

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("BOOLEAN_DATA")
                .payload(true)
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isEqualTo(true);
    }

    @Test
    @DisplayName("handlePublicMessage - should handle array payload")
    void handlePublicMessage_withArrayPayload_shouldPreserveArray() {
        // Given
        when(headerAccessor.getSessionId()).thenReturn("session-array");
        String[] arrayPayload = {"item1", "item2", "item3"};

        WebSocketMessage inputMessage = WebSocketMessage.builder()
                .type("ARRAY_DATA")
                .payload(arrayPayload)
                .build();

        // When
        WebSocketMessage result = webSocketController.handlePublicMessage(inputMessage, headerAccessor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isEqualTo(arrayPayload);
        assertThat(((String[]) result.getPayload()).length).isEqualTo(3);
    }

    /**
     * Helper record for testing complex payloads.
     */
    private record TestData(String name, int count) {
    }
}
