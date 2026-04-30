package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.role.RoleCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    long count();

    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<User> findTop10ByOrderByCreatedAtDesc();

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAllByIdIn(Collection<UUID> ids);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void deleteById(UUID id);

    Optional<User> findByEmailValue(String email);

    boolean hasRole(UUID userId, RoleCode roleCode);

    Optional<User> findByPhone(String phone);
}

