package com.sep.realvista.infrastructure.persistence.user;

import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.role.Role;
import com.sep.realvista.domain.user.role.RoleCode;
import com.sep.realvista.domain.user.role.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to build dynamic JPA Specifications for User entity.
 */
public final class UserSpecification {

    private UserSpecification() {
        // Utility class
    }

    public static Specification<User> filterBy(String search, UserStatus status, RoleCode role) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter out deleted users by default (matching project pattern)
            predicates.add(cb.equal(root.get("deleted"), false));

            // Search by text (firstName, lastName, businessName, email, phone)
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), searchPattern),
                        cb.like(cb.lower(root.get("lastName")), searchPattern),
                        cb.like(cb.lower(root.get("businessName")), searchPattern),
                        cb.like(cb.lower(root.get("email").get("value")), searchPattern),
                        cb.like(cb.lower(root.get("phone")), searchPattern)
                ));
            }

            // Filter by status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter by role
            if (role != null) {
                Join<User, UserRole> userRolesJoin = root.join("userRoles");
                Join<UserRole, Role> roleJoin = userRolesJoin.join("role");
                predicates.add(cb.equal(roleJoin.get("roleCode"), role));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
