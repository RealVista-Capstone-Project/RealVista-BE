package com.sep.realvista.unit.domain.user;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
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

    private User createTestUser(UserStatus status) {
        return User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .businessName("Test Business")
                .status(status)
                .build();
    }

    private User createTestUserWithNames(String firstName, String lastName, UserStatus status) {
        return User.builder()
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .businessName("Test Business")
                .firstName(firstName)
                .lastName(lastName)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("Should activate pending user successfully")
    void shouldActivatePendingUser() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);
        user.suspend(); // First suspend to test activate

        // Act
        user.activate();

        // Assert
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw exception when activating already active user")
    void shouldThrowExceptionWhenActivatingActiveUser() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act & Assert
        assertThatThrownBy(() -> user.activate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already active");
    }

    @Test
    @DisplayName("Should suspend active user successfully")
    void shouldSuspendActiveUser() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act
        user.suspend();

        // Assert
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should throw exception when suspending already suspended user")
    void shouldThrowExceptionWhenSuspendingAlreadySuspendedUser() {
        // Arrange
        User user = createTestUser(UserStatus.SUSPENDED);

        // Act & Assert
        assertThatThrownBy(() -> user.suspend())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already suspended");
    }

    @Test
    @DisplayName("Should ban active user successfully")
    void shouldBanActiveUser() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act
        user.ban();

        // Assert
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    @DisplayName("Should throw exception when banning already banned user")
    void shouldThrowExceptionWhenBanningAlreadyBannedUser() {
        // Arrange
        User user = createTestUser(UserStatus.BANNED);

        // Act & Assert
        assertThatThrownBy(() -> user.ban())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already banned");
    }

    @Test
    @DisplayName("Should verify active user successfully")
    void shouldVerifyActiveUser() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act
        user.verify();

        // Assert
        assertThat(user.getStatus()).isEqualTo(UserStatus.VERIFIED);
    }

    @Test
    @DisplayName("Should throw exception when verifying already verified user")
    void shouldThrowExceptionWhenVerifyingAlreadyVerifiedUser() {
        // Arrange
        User user = createTestUser(UserStatus.VERIFIED);

        // Act & Assert
        assertThatThrownBy(() -> user.verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already verified");
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void shouldUpdateUserProfile() {
        // Arrange
        User user = createTestUserWithNames("John", "Doe", UserStatus.ACTIVE);

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
        User user = createTestUser(UserStatus.ACTIVE);

        // Act
        user.updatePassword("newHashedPassword");

        // Assert
        assertThat(user.getPasswordHash()).isEqualTo("newHashedPassword");
    }

    @Test
    @DisplayName("Should throw exception when updating with null password")
    void shouldThrowExceptionWhenUpdatingWithNullPassword() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act & Assert
        assertThatThrownBy(() -> user.updatePassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password hash cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when updating with empty password")
    void shouldThrowExceptionWhenUpdatingWithEmptyPassword() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act & Assert
        assertThatThrownBy(() -> user.updatePassword("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password hash cannot be null or empty");
    }

    @Test
    @DisplayName("Should return true when user is active")
    void shouldReturnTrueWhenUserIsActive() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act & Assert
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return false when user is not active")
    void shouldReturnFalseWhenUserIsNotActive() {
        // Arrange
        User user = createTestUser(UserStatus.SUSPENDED);

        // Act & Assert
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return full name when both first and last names exist")
    void shouldReturnFullNameWhenBothNamesExist() {
        // Arrange
        User user = createTestUserWithNames("John", "Doe", UserStatus.ACTIVE);

        // Act & Assert
        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return business name when names are null")
    void shouldReturnEmailWhenNamesAreNull() {
        // Arrange
        User user = createTestUser(UserStatus.ACTIVE);

        // Act & Assert - User.getFullName() returns businessName when firstName/lastName are null
        assertThat(user.getFullName()).isEqualTo("Test Business");
    }
}
