package com.sep.realvista.application.user.service;

import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.OtpService;
import com.sep.realvista.application.user.dto.ChangePasswordRequest;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UpdateMeRequest;
import com.sep.realvista.application.user.dto.UpdateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.dto.UserSearchResponse;
import com.sep.realvista.application.user.mapper.UserMapper;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.repository.AgentProfileRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.profile.CustomerProfile;
import com.sep.realvista.domain.profile.repository.CustomerProfileRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.domain.user.role.Role;
import com.sep.realvista.domain.user.role.RoleCode;
import com.sep.realvista.domain.user.role.RoleRepository;
import com.sep.realvista.domain.user.role.UserRole;
import com.sep.realvista.domain.user.role.UserRoleRepository;
import com.sep.realvista.infrastructure.security.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Locale;
import java.util.Objects;
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
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final SettingPreferenceRepository settingPreferenceRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final EmailService emailService;
    private final OtpService otpService;
    private final com.sep.realvista.application.billing.service.BillingApplicationService billingApplicationService;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final String EMAIL_OTP_PREFIX = "email-otp:";
    private static final String EMAIL_OTP_TARGET_PREFIX = "email-otp-target:";

    /**
     * Create a new user.
     */
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        // Validate unique email and phone
        userDomainService.validateUniqueEmail(request.getEmail());
        userDomainService.validateUniquePhone(request.getPhoneNumber());

        // Build user entity
        User user = User.builder()
                .email(Email.of(request.getEmail()))
                .passwordHash(passwordService.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhoneNumber())
                .businessName(request.getFirstName() + " " + request.getLastName())
                .status(UserStatus.ACTIVE)
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getUserId());

        // 1. Assign Role(s)
        if ("AGENT".equalsIgnoreCase(request.getRole())) {
            // Agent role only
            Role agentRole = roleRepository.findByRoleCode(RoleCode.AGENT)
                    .orElseThrow(() -> new BusinessConflictException("Role AGENT not found", "ROLE_NOT_FOUND"));
            UserRole userRole = UserRole.create(savedUser, agentRole);
            userRoleRepository.save(userRole);
        } else {
            // User gets both BUYER and TENANT roles
            Role buyerRole = roleRepository.findByRoleCode(RoleCode.BUYER)
                    .orElseThrow(() -> new BusinessConflictException("Role BUYER not found", "ROLE_NOT_FOUND"));
            Role tenantRole = roleRepository.findByRoleCode(RoleCode.TENANT)
                    .orElseThrow(() -> new BusinessConflictException("Role TENANT not found", "ROLE_NOT_FOUND"));

            UserRole buyerUserRole = UserRole.create(savedUser, buyerRole);
            UserRole tenantUserRole = UserRole.create(savedUser, tenantRole);
            userRoleRepository.save(buyerUserRole);
            userRoleRepository.save(tenantUserRole);
        }

        // 2. Create full true preferences
        SettingPreference preference = SettingPreference.builder()
                .userId(savedUser.getUserId())
                .inAppEnabled(true)
                .emailEnabled(true)
                .pushEnabled(true)
                .contactViaEmail(true)
                .contactViaPhone(true)
                .hidePhoneNumber(false)
                .hideEmail(false)
                .build();
        settingPreferenceRepository.save(preference);

        // 3. Create default profile
        if ("AGENT".equalsIgnoreCase(request.getRole())) {
            AgentProfile profile = AgentProfile.builder()
                    .userId(savedUser.getUserId())
                    .rating(java.math.BigDecimal.ZERO)
                    .propertiesSold(0)
                    .build();
            agentProfileRepository.save(profile);
        } else {
            CustomerProfile profile = CustomerProfile.builder()
                    .userId(savedUser.getUserId())
                    .profileName(savedUser.getFullName())
                    .isActive(true)
                    .build();
            customerProfileRepository.save(profile);
        }

        // 4. Assign default packages
        if ("AGENT".equalsIgnoreCase(request.getRole())) {
            billingApplicationService.assignAllDefaultFreePackages(savedUser.getUserId());
        } else {
            billingApplicationService.assignDefaultAiPackage(savedUser.getUserId());
        }

        return userMapper.toResponse(savedUser);
    }

    /**
     * Create a new user from Google login.
     * Sets email verified to true and assigns default BUYER role.
     */
    public User createGoogleUser(String email, String firstName, String lastName, String avatarUrl) {
        log.info("Creating new Google user with email: {}", email);

        // Build user entity
        User user = User.builder()
                .email(Email.of(email))
                .passwordHash(passwordService.encode(UUID.randomUUID().toString())) // Random password for Google
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl(avatarUrl)
                .businessName((firstName != null && lastName != null)
                        ? firstName + " " + lastName : email.split("@")[0])
                .status(UserStatus.ACTIVE)
                .emailVerifiedAt(java.time.LocalDateTime.now()) // Auto-verify email for Google
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("Google user created successfully with ID: {}", savedUser.getUserId());

        // 1. Assign default BUYER and TENANT roles
        Role buyerRole = roleRepository.findByRoleCode(RoleCode.BUYER)
                .orElseThrow(() -> new BusinessConflictException("Role not found: BUYER", "ROLE_NOT_FOUND"));
        Role tenantRole = roleRepository.findByRoleCode(RoleCode.TENANT)
                .orElseThrow(() -> new BusinessConflictException("Role not found: TENANT", "ROLE_NOT_FOUND"));

        UserRole buyerUserRole = UserRole.create(savedUser, buyerRole);
        UserRole tenantUserRole = UserRole.create(savedUser, tenantRole);
        userRoleRepository.save(buyerUserRole);
        userRoleRepository.save(tenantUserRole);

        // 2. Create default preferences
        SettingPreference preference = SettingPreference.builder()
                .userId(savedUser.getUserId())
                .inAppEnabled(true)
                .emailEnabled(true)
                .pushEnabled(true)
                .contactViaEmail(true)
                .contactViaPhone(true)
                .hidePhoneNumber(false)
                .hideEmail(false)
                .build();
        settingPreferenceRepository.save(preference);

        // 3. Create default customer profile
        CustomerProfile profile = CustomerProfile.builder()
                .userId(savedUser.getUserId())
                .profileName(savedUser.getFullName())
                .isActive(true)
                .build();
        customerProfileRepository.save(profile);

        // 4. Assign default packages
        billingApplicationService.assignDefaultAiPackage(savedUser.getUserId());

        return savedUser;
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
     * Update current user (me) profile including phone.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse updateMe(UUID userId, UpdateMeRequest request) {
        log.info("Updating me profile for ID: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
            String currentEmail = user.getEmail() != null ? user.getEmail().getValue() : null;
            if (!Objects.equals(currentEmail, normalizedEmail)) {
                userRepository.findByEmailValue(normalizedEmail).ifPresent(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        throw new BusinessConflictException(
                                "Email already exists: " + normalizedEmail,
                                "EMAIL_ALREADY_EXISTS"
                        );
                    }
                });
                user.updateEmail(normalizedEmail);
            }
        }
        user.updateProfile(request.getFirstName(), request.getLastName(), request.getAvatarUrl());
        if (request.getBusinessName() != null) {
            user.updateBusinessName(request.getBusinessName());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            user.updatePhone(request.getPhone());
        }

        User updatedUser = userRepository.save(user);
        log.info("Me profile updated successfully for ID: {}", userId);

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

    /**
     * Find user ID by email.
     */
    @Transactional(readOnly = true)
    public UUID findUserIdByEmail(String email) {
        log.debug("Finding user ID for email: {}", email);
        User user = userRepository.findByEmailValue(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return user.getUserId();
    }

    /**
     * Check if user has specific role.
     */
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, RoleCode roleCode) {
        log.debug("Checking if user {} has role: {}", userId, roleCode);
        return userRepository.hasRole(userId, roleCode);
    }

    /**
     * Check if user has any of the specified roles.
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRole(UUID userId, RoleCode... roleCodes) {
        log.debug("Checking if user {} has any of roles: {}", userId, roleCodes);
        for (RoleCode roleCode : roleCodes) {
            if (userRepository.hasRole(userId, roleCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process OAuth2 user: find existing or create new, and initialize roles.
     * Transactional to avoid LazyInitializationException.
     */
    @Transactional
    public User processOAuth2User(String email, String firstName, String lastName, String avatarUrl) {
        User user = userRepository.findByEmailValue(email)
                .map(existingUser -> {
                    if (!existingUser.isEmailVerified()) {
                        existingUser.verifyEmail();
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> createGoogleUser(email, firstName, lastName, avatarUrl));

        // Eagerly initialize roles while session is open
        user.getUserRoles().size();
        user.getUserRoles().forEach(ur -> {
            if (ur.getRole() != null) {
                ur.getRole().getRoleCode();
            }
        });

        return user;
    }

    /**
     * Search user by email for owner assignment.
     * Returns masked phone number for security.
     */
    @Transactional(readOnly = true)
    public UserSearchResponse searchUserByEmail(String email) {
        log.info("Searching user by email: {}", email);
        User user = userRepository.findByEmailValue(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return UserSearchResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail().getValue())
                .fullName(user.getFullName())
                .maskedPhone(maskPhone(user.getPhone()))
                .phone(user.getPhone())
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        // Example: 0912345678 -> 09******78
        return phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
    }

    /**
     * Generate a 6-digit OTP and send it to the provided email address.
     * If the email differs from the user's current email, update it first
     * (which also resets emailVerifiedAt so the new address must be verified).
     */
    @CacheEvict(value = "users", key = "#userId")
    public void sendEmailOtp(UUID userId, String targetEmail) {
        User user = userDomainService.getUserOrThrow(userId);

        String normalizedEmail = targetEmail.trim().toLowerCase(Locale.ROOT);

        // Update email on the user record if it has changed
        String currentEmail = user.getEmail() != null ? user.getEmail().getValue() : null;
        if (!normalizedEmail.equals(currentEmail)) {
            userRepository.findByEmailValue(normalizedEmail).ifPresent(existing -> {
                if (!existing.getUserId().equals(userId)) {
                    throw new BusinessConflictException(
                            "Email already exists: " + normalizedEmail,
                            "EMAIL_ALREADY_EXISTS"
                    );
                }
            });
            user.updateEmail(normalizedEmail);
            userRepository.save(user);
        }

        String otp = otpService.generateAndStore(EMAIL_OTP_PREFIX + userId, OTP_EXPIRY_MINUTES);
        String fullName = user.getFullName();
        emailService.sendTemplateMessageAsync(
                normalizedEmail,
                "Mã xác minh email RealVista",
                "email-otp",
                Map.of(
                        "userName", fullName != null && !fullName.isBlank() ? fullName : normalizedEmail,
                        "otp", otp,
                        "expiryMinutes", OTP_EXPIRY_MINUTES
                )
        );
        log.info("Email OTP sent to {} for user {}", normalizedEmail, userId);
    }

    /**
     * Verify the email OTP and stamp emailVerifiedAt.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse verifyEmail(UUID userId, String otp) {
        if (!otpService.verify(EMAIL_OTP_PREFIX + userId, otp)) {
            throw new BusinessConflictException("OTP không hợp lệ hoặc đã hết hạn", "INVALID_OTP");
        }
        User user = userDomainService.getUserOrThrow(userId);
        user.verifyEmail();
        User saved = userRepository.save(user);
        log.info("Email verified for user {}", userId);
        return userMapper.toResponse(saved);
    }

    /**
     * Mark current phone as verified. Optionally updates phone before verification.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse verifyPhone(UUID userId, String phone) {
        User user = userDomainService.getUserOrThrow(userId);
        if (phone != null && !phone.isBlank() && !phone.equals(user.getPhone())) {
            user.updatePhone(phone);
        }
        user.verifyPhone();
        User saved = userRepository.save(user);
        log.info("Phone verified for user {}", userId);
        return userMapper.toResponse(saved);
    }

    /**
     * Returns remaining OTP TTL in seconds (-1 if none).
     */
    @Transactional(readOnly = true)
    public long emailOtpRemainingSeconds(UUID userId) {
        return otpService.remainingSeconds(EMAIL_OTP_PREFIX + userId);
    }

    /**
     * Add OWNER role to a user if they don't already have it.
     * Idempotent: does nothing if the user already has the OWNER role.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse addOwnerRole(UUID userId) {
        log.info("Adding OWNER role to user ID: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);

        // Skip if user already has OWNER role
        if (userRepository.hasRole(userId, RoleCode.OWNER)) {
            log.info("User {} already has OWNER role, skipping", userId);
            return userMapper.toResponse(user);
        }

        Role ownerRole = roleRepository.findByRoleCode(RoleCode.OWNER)
                .orElseThrow(() -> new BusinessConflictException("Role OWNER not found", "ROLE_NOT_FOUND"));

        UserRole userRole = UserRole.create(user, ownerRole);
        userRoleRepository.save(userRole);

        // Assign LISTING_FREE and 3D_TOUR_FREE packages for new owner
        billingApplicationService.assignDefaultOwnerPackages(userId);

        log.info("OWNER role added successfully to user ID: {}", userId);
        return userMapper.toResponse(userDomainService.getUserOrThrow(userId));
    }
}
