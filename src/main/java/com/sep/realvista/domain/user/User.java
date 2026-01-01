package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.common.value.Email;
import jakarta.persistence.*;
import lombok.*;

/**
 * Domain Entity: User
 * Contains core business logic and domain rules.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "value")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(length = 500)
    private String avatarUrl;

    /**
     * Domain logic: Activate user account.
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Domain logic: Suspend user account.
     */
    public void suspend() {
        if (this.status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("User is already suspended");
        }
        this.status = UserStatus.SUSPENDED;
    }

    /**
     * Domain logic: Update user profile.
     */
    public void updateProfile(String firstName, String lastName, String avatarUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
    }

    /**
     * Domain logic: Update password.
     */
    public void updatePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = newPasswordHash;
    }

    /**
     * Check if user is active.
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * Get full name.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email.getValue();
        }
        return String.format("%s %s", firstName != null ? firstName : "",
                lastName != null ? lastName : "").trim();
    }
}

