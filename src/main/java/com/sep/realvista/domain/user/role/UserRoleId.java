package com.sep.realvista.domain.user.role;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite Primary Key for {@link UserRole} entity.
 * <p>
 * Represents a unique combination of (user_id, role_id) to track
 * which user has which role. Required by JPA when an entity
 * has multiple @Id fields.
 *
 * @see UserRole
 */
public class UserRoleId implements Serializable {

    private UUID userId;
    private UUID roleId;

    public UserRoleId() {
    }

    public UserRoleId(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
