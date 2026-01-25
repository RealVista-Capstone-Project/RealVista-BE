package com.sep.realvista.domain.user.role;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles", indexes = {
        @Index(name = "idx_user_role_user", columnList = "user_id"),
        @Index(name = "idx_user_role_role", columnList = "role_id")
})
@IdClass(UserRoleId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"user", "role"})
public class UserRole extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    public static UserRole create(UUID userId, UUID roleId) {
        return UserRole.builder()
                .userId(userId)
                .roleId(roleId)
                .build();
    }

    public static UserRole create(User user, Role role) {
        return UserRole.builder()
                .userId(user.getUserId())
                .roleId(role.getRoleId())
                .build();
    }
}
