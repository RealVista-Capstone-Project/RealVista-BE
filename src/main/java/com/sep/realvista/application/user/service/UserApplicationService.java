package com.sep.realvista.application.user.service;

import com.sep.realvista.application.user.dto.ChangePasswordRequest;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UpdateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.mapper.UserMapper;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.infrastructure.security.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service for User operations.
 * Orchestrates business logic and coordinates between domain and infrastructure layers.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final UserMapper userMapper;
    private final PasswordService passwordService;

    /**
     * Create a new user.
     */
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        // Validate unique email
        userDomainService.validateUniqueEmail(request.getEmail());

        // Build user entity
        User user = User.builder()
                .email(Email.of(request.getEmail()))
                .passwordHash(passwordService.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .businessName(request.getFirstName() + " " + request.getLastName())
                .status(UserStatus.ACTIVE)
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getUserId());

        return userMapper.toResponse(savedUser);
    }

    /**
     * Get user by ID.
     */
    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userDomainService.getUserOrThrow(userId);
        return userMapper.toResponse(user);
    }

    /**
     * Update user profile.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Updating user profile for ID: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);
        user.updateProfile(request.getFirstName(), request.getLastName(), request.getAvatarUrl());

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for ID: {}", userId);

        return userMapper.toResponse(updatedUser);
    }

    /**
     * Change user password.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);

        // Verify current password
        if (!passwordService.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessConflictException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }

        // Update password
        String newPasswordHash = passwordService.encode(request.getNewPassword());
        user.updatePassword(newPasswordHash);

        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Activate user account.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse activateUser(UUID userId) {
        log.info("Activating user ID: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);
        user.activate();

        User activatedUser = userRepository.save(user);
        log.info("User activated successfully: {}", userId);

        return userMapper.toResponse(activatedUser);
    }

    /**
     * Suspend user account.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse suspendUser(UUID userId) {
        log.info("Suspending user ID: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);
        user.suspend();

        User suspendedUser = userRepository.save(user);
        log.info("User suspended successfully: {}", userId);

        return userMapper.toResponse(suspendedUser);
    }

    /**
     * Delete user.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(UUID userId) {
        log.info("Deleting user ID: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);
        user.markAsDeleted();

        userRepository.save(user);
        log.info("User deleted successfully: {}", userId);
    }
}

