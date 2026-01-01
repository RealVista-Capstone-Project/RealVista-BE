package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.value.Email;

import java.util.Optional;

/**
 * Repository interface for User domain.
 * Defines the contract for data access without exposing implementation details.
 */
public interface UserRepository {

    /**
     * Save a user.
     */
    User save(User user);

    /**
     * Find user by ID.
     */
    Optional<User> findById(Long id);

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(Email email);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(Email email);

    /**
     * Delete user by ID.
     */
    void deleteById(Long id);

    /**
     * Find user by email string.
     */
    Optional<User> findByEmailValue(String email);
}

