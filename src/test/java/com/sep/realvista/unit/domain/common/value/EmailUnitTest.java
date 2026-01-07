package com.sep.realvista.unit.domain.common.value;

import com.sep.realvista.domain.common.value.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Email value object.
 * 
 * These are pure unit tests that verify email validation logic
 * and value object behavior in isolation.
 */
@DisplayName("Email Value Object Unit Tests")
class EmailUnitTest {

    @Test
    @DisplayName("Should create email with valid format")
    void shouldCreateEmailWithValidFormat() {
        // Act
        Email email = Email.of("test@example.com");

        // Assert
        assertThat(email).isNotNull();
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeEmailToLowercase() {
        // Act
        Email email = Email.of("TEST@EXAMPLE.COM");

        // Assert
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should trim whitespace from email")
    void shouldTrimWhitespaceFromEmail() {
        // Act
        Email email = Email.of("  test@example.com  ");

        // Assert
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception for null email")
    void shouldThrowExceptionForNullEmail() {
        // Act & Assert
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty email")
    void shouldThrowExceptionForEmptyEmail() {
        // Act & Assert
        assertThatThrownBy(() -> Email.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for blank email")
    void shouldThrowExceptionForBlankEmail() {
        // Act & Assert
        assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",
            "test",
            "@example.com",
            "test@",
            "test@@example.com",
            "test @example.com",
            "test@example",
            "test@.com"
    })
    @DisplayName("Should reject invalid email formats")
    void shouldRejectInvalidEmailFormats(String invalidEmail) {
        // Act & Assert
        assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_123@test.example.com",
            "first.last@sub.domain.com"
    })
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmailFormats(String validEmail) {
        // Act
        Email email = Email.of(validEmail);

        // Assert
        assertThat(email).isNotNull();
        assertThat(email.getValue()).isEqualTo(validEmail.toLowerCase().trim());
    }

    @Test
    @DisplayName("Should return email value in toString()")
    void shouldReturnEmailValueInToString() {
        // Arrange
        Email email = Email.of("test@example.com");

        // Act
        String result = email.toString();

        // Assert
        assertThat(result).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should be equal when values are the same")
    void shouldBeEqualWhenValuesAreTheSame() {
        // Arrange
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("TEST@EXAMPLE.COM");  // Will be normalized

        // Act & Assert
        assertThat(email1.getValue()).isEqualTo(email2.getValue());
    }
}

