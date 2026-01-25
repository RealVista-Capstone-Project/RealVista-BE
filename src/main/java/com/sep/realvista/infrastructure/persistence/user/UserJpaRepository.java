package com.sep.realvista.infrastructure.persistence.user;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
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
}

