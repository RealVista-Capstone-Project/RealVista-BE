package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Domain Service for User.
 * Contains business logic that doesn't naturally fit in the entity.
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    /**
     * Validate that email is unique before creating user.
     */
    public void validateUniqueEmail(String email) {
        if (userRepository.findByEmailValue(email).isPresent()) {
            throw new BusinessConflictException("Email already exists: " + email, "EMAIL_ALREADY_EXISTS");
        }
    }

    /**
     * Find user by ID or throw exception.
     */
    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Find user by email or throw exception.
     */
    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmailValue(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}

