package com.sep.realvista.unit.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sep.realvista.infrastructure.security.oauth2.GoogleTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for GoogleTokenVerifier.
 * <p>
 * Note: These tests use reflection to set test client ID.
 * Integration tests would require actual Google ID tokens.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleTokenVerifier Unit Tests")
class GoogleTokenVerifierUnitTest {

    private static final String TEST_CLIENT_ID = "test-client-id.apps.googleusercontent.com";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_PICTURE_URL = "https://example.com/avatar.jpg";

    private GoogleTokenVerifier googleTokenVerifier;

    @BeforeEach
    void setUp() {
        googleTokenVerifier = new GoogleTokenVerifier(TEST_CLIENT_ID, TEST_CLIENT_ID);
    }

    @Test
    @DisplayName("Should be instantiated with client ID")
    void shouldBeInstantiatedWithClientId() {
        // Then
        assertThat(googleTokenVerifier).isNotNull();
    }

    @Test
    @DisplayName("Should extract email from payload")
    void shouldExtractEmailFromPayload() {
        // Given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(TEST_EMAIL);

        // When
        String email = googleTokenVerifier.getEmail(payload);

        // Then
        assertThat(email).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should extract given name from payload")
    void shouldExtractGivenNameFromPayload() {
        // Given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.set("given_name", TEST_FIRST_NAME);

        // When
        String givenName = googleTokenVerifier.getGivenName(payload);

        // Then
        assertThat(givenName).isEqualTo(TEST_FIRST_NAME);
    }

    @Test
    @DisplayName("Should extract family name from payload")
    void shouldExtractFamilyNameFromPayload() {
        // Given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.set("family_name", TEST_LAST_NAME);

        // When
        String familyName = googleTokenVerifier.getFamilyName(payload);

        // Then
        assertThat(familyName).isEqualTo(TEST_LAST_NAME);
    }

    @Test
    @DisplayName("Should extract picture URL from payload")
    void shouldExtractPictureUrlFromPayload() {
        // Given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.set("picture", TEST_PICTURE_URL);

        // When
        String pictureUrl = googleTokenVerifier.getPictureUrl(payload);

        // Then
        assertThat(pictureUrl).isEqualTo(TEST_PICTURE_URL);
    }

    @Test
    @DisplayName("Should return null for missing optional fields")
    void shouldReturnNullForMissingOptionalFields() {
        // Given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(TEST_EMAIL);

        // When & Then
        assertThat(googleTokenVerifier.getGivenName(payload)).isNull();
        assertThat(googleTokenVerifier.getFamilyName(payload)).isNull();
        assertThat(googleTokenVerifier.getPictureUrl(payload)).isNull();
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void shouldThrowExceptionForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When & Then
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception for null token")
    void shouldThrowExceptionForNullToken() {
        // When & Then
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception for empty token")
    void shouldThrowExceptionForEmptyToken() {
        // When & Then
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(""))
                .isInstanceOf(Exception.class);
    }
}
