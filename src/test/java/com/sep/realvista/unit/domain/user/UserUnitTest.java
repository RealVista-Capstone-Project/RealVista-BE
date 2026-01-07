package com.sep.realvista.unit.domain.user;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for User domain entity.
 * 
 * These are pure unit tests (no Spring context) that test the business logic
 * and domain rules of the User entity in isolation.
 */
@DisplayName("User Domain Entity Unit Tests")
class UserUnitTest {

    @Test
    @DisplayName("Should activate pending user successfully")
    void shouldActivatePendingUser() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();

        // Act
        user.activate();

        // Assert
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw exception when activating already active user")
    void shouldThrowExceptionWhenActivatingActiveUser() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> user.activate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already active");
    }

    @Test
    @DisplayName("Should suspend active user successfully")
    void shouldSuspendActiveUser() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act
        user.suspend();

        // Assert
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should throw exception when suspending already suspended user")
    void shouldThrowExceptionWhenSuspendingAlreadySuspendedUser() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.SUSPENDED)
                .role(UserRole.USER)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> user.suspend())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already suspended");
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void shouldUpdateUserProfile() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act
        user.updateProfile("Jane", "Smith", "https://example.com/avatar.jpg");

        // Assert
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
    }

    @Test
    @DisplayName("Should update password successfully")
    void shouldUpdatePassword() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("oldHashedPassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act
        user.updatePassword("newHashedPassword");

        // Assert
        assertThat(user.getPasswordHash()).isEqualTo("newHashedPassword");
    }

    @Test
    @DisplayName("Should throw exception when updating with null password")
    void shouldThrowExceptionWhenUpdatingWithNullPassword() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> user.updatePassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password hash cannot be null or empty");
    }

    @Test
    @DisplayName("Should return true when user is active")
    void shouldReturnTrueWhenUserIsActive() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act & Assert
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return false when user is not active")
    void shouldReturnFalseWhenUserIsNotActive() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();

        // Act & Assert
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return full name when both first and last names exist")
    void shouldReturnFullNameWhenBothNamesExist() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act & Assert
        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return email when names are null")
    void shouldReturnEmailWhenNamesAreNull() {
        // Arrange
        User user = User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        // Act & Assert
        assertThat(user.getFullName()).isEqualTo("test@example.com");
    }
}

