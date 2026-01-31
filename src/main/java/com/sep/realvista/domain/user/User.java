package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.role.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Embedded
    private Email email;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        this.status = UserStatus.ACTIVE;
    }

    public void suspend() {
        if (this.status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("User is already suspended");
        }
        this.status = UserStatus.SUSPENDED;
    }

    public void ban() {
        if (this.status == UserStatus.BANNED) {
            throw new IllegalStateException("User is already banned");
        }
        this.status = UserStatus.BANNED;
    }

    public void verify() {
        if (this.status == UserStatus.VERIFIED) {
            throw new IllegalStateException("User is already verified");
        }
        this.status = UserStatus.VERIFIED;
    }

    public void updateProfile(String firstName, String lastName, String avatarUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
    }

    public void updatePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = newPasswordHash;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isEmailVerified() {
        return this.emailVerifiedAt != null;
    }

    public boolean isPhoneVerified() {
        return this.phoneVerifiedAt != null;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return businessName != null ? businessName : (email != null ? email.getValue() : "");
        }
        return String.format("%s %s", firstName != null ? firstName : "",
                lastName != null ? lastName : "").trim();
    }

    public void addUserRole(UserRole userRole) {
        this.userRoles.add(userRole);
    }

    public void removeUserRole(UserRole userRole) {
        this.userRoles.remove(userRole);
    }

    public boolean hasRole(String roleCode) {
        return this.userRoles.stream()
                .filter(ur -> ur.getRole() != null)
                .anyMatch(ur -> ur.getRole().getRoleCode().name().equals(roleCode));
    }
}

