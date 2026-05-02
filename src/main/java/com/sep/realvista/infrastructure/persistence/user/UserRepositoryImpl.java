package com.sep.realvista.infrastructure.persistence.user;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.role.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserRepository using JPA.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByCreatedAtAfter(LocalDateTime date) {
        return jpaRepository.countByCreatedAtAfter(date);
    }

    @Override
    public long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public List<User> findTop10ByOrderByCreatedAtDesc() {
        return jpaRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    @Override
    public Page<User> findAll(Specification<User> spec, Pageable pageable) {
        return jpaRepository.findAll(spec, pageable);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<User> findAllByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllById(ids);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByEmailValue(String email) {
        return jpaRepository.findByEmailValue(email);
    }

    @Override
    public boolean hasRole(UUID userId, RoleCode roleCode) {
        return jpaRepository.hasRole(userId, roleCode);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone);
    }

    @Override
    public long countByStatusAndCreatedAtAfter(com.sep.realvista.domain.user.UserStatus status, LocalDateTime date) {
        return jpaRepository.countByStatusAndCreatedAtAfter(status, date);
    }
}


