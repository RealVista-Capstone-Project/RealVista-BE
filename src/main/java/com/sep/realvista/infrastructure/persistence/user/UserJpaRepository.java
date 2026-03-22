package com.sep.realvista.infrastructure.persistence.user;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.role.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity.
 */
public interface UserJpaRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    @Query("SELECT u FROM User u WHERE u.email.value = :email")
    Optional<User> findByEmailValue(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END "
           + "FROM UserRole ur "
           + "JOIN ur.role r "
           + "WHERE ur.userId = :userId AND r.roleCode = :roleCode")
    boolean hasRole(@Param("userId") UUID userId, @Param("roleCode") RoleCode roleCode);
}

