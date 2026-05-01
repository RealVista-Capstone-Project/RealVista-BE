package com.sep.realvista.application.user.service;

import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.OtpService;
import com.sep.realvista.application.user.dto.ChangePasswordRequest;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UpdateMeRequest;
import com.sep.realvista.application.user.dto.UpdateUserRequest;
import com.sep.realvista.application.user.dto.UserFilterRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.dto.UserSearchResponse;
import com.sep.realvista.application.user.mapper.UserMapper;
import com.sep.realvista.infrastructure.persistence.user.UserSpecification;
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
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import com.sep.realvista.infrastructure.security.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

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
    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final AppointmentRepository appointmentRepository;
    private final ListingBoostRepository listingBoostRepository;
    private final EngagementRepository engagementRepository;
    private final AgentProposalRepository agentProposalRepository;
    private final UserFeatureSubscriptionRepository userFeatureSubscriptionRepository;

    /**
     * Get paginated list of users with search and filters (Admin only).
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getPagedUsers(UserFilterRequest filter, Pageable pageable) {
        log.info("Fetching paginated users with filter: {}", filter);
        Specification<User> spec = UserSpecification.filterBy(
                filter.getSearch(),
                filter.getStatus(),
                filter.getRole()
        );
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toResponse);
    }

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final String EMAIL_OTP_PREFIX = "email-otp:";
    private static final String PENDING_EMAIL_PREFIX = "pending-email:";
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
                    .orElseThrow(() -> new BusinessConflictException("Role AGENT not found", "ERROR_ROLE_NOT_FOUND"));
            UserRole userRole = UserRole.create(savedUser, agentRole);
            userRoleRepository.save(userRole);
        } else {
            // User gets both BUYER and TENANT roles
            Role buyerRole = roleRepository.findByRoleCode(RoleCode.BUYER)
                    .orElseThrow(() -> new BusinessConflictException("Role BUYER not found", "ERROR_ROLE_NOT_FOUND"));
            Role tenantRole = roleRepository.findByRoleCode(RoleCode.TENANT)
                    .orElseThrow(() -> new BusinessConflictException("Role TENANT not found", "ERROR_ROLE_NOT_FOUND"));

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
                .orElseThrow(() -> new BusinessConflictException("Role not found: BUYER", "ERROR_ROLE_NOT_FOUND"));
        Role tenantRole = roleRepository.findByRoleCode(RoleCode.TENANT)
                .orElseThrow(() -> new BusinessConflictException("Role not found: TENANT", "ERROR_ROLE_NOT_FOUND"));

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
        // Email must not be changed via PATCH /me — only through send-email-otp + verify-email.
        // Otherwise a client could persist a new address without completing OTP verification.
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
            String currentEmail = user.getEmail() != null ? user.getEmail().getValue() : null;
            if (!Objects.equals(currentEmail, normalizedEmail)) {
                userRepository.findByEmailValue(normalizedEmail).ifPresent(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        throw new BusinessConflictException(
                                "Email already exists: " + normalizedEmail,
                                "ERROR_EMAIL_ALREADY_EXISTS",
                                new Object[]{normalizedEmail}
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
            throw new BusinessConflictException("Current password is incorrect", "ERROR_INVALID_CURRENT_PASSWORD");
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
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessConflictException(
                    "Không thể kích hoạt tài khoản đã bị khóa vĩnh viễn",
                    "CANNOT_ACTIVATE_BANNED_USER"
            );
        }
        user.activate();

        User activatedUser = userRepository.save(user);
        log.info("User activated successfully: {}", userId);

        return userMapper.toResponse(activatedUser);
    }

    /**
     * Suspend user account with cascading cleanup of their properties and listings.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse suspendUser(UUID userId) {
        log.info("Suspending user ID and cascading cleanup: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessConflictException(
                    "Không thể đình chỉ tài khoản đã bị khóa vĩnh viễn",
                    "CANNOT_SUSPEND_BANNED_USER"
            );
        }
        user.suspend();
        User suspendedUser = userRepository.save(user);

        performCascadingCleanup(userId, false);

        log.info("User {} suspension cascade completed successfully", userId);
        return userMapper.toResponse(suspendedUser);
    }

    /**
     * Ban user account permanently with cascading cleanup of all associated entities.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse banUser(UUID userId) {
        log.info("Banning user ID and cascading cleanup: {}", userId);

        User user = userDomainService.getUserOrThrow(userId);
        user.ban();
        User bannedUser = userRepository.save(user);

        performCascadingCleanup(userId, true);

        log.info("User {} ban cascade completed successfully", userId);
        return userMapper.toResponse(bannedUser);
    }

    private void performCascadingCleanup(UUID userId, boolean isBan) {
        // 1. Fetch properties owned by user
        List<Property> properties = propertyRepository.findByOwnerId(userId);
        List<UUID> propertyIds = properties.stream().map(Property::getPropertyId).collect(Collectors.toList());

        // 2. Fetch listings created by user or for user's properties
        List<Listing> listings = listingRepository.findByUserIdOrPropertyOwnerId(userId);
        List<UUID> listingIds = listings.stream().map(Listing::getListingId).collect(Collectors.toList());

        // 3. Update properties to DRAFT
        if (!properties.isEmpty()) {
            log.info("Cleanup cascade: moving {} properties to DRAFT for user {}", properties.size(), userId);
            properties.forEach(p -> p.updateStatus(PropertyStatus.DRAFT));
            propertyRepository.saveAll(properties);
        }

        // 4. Update listings to DRAFT (unpublish)
        if (!listings.isEmpty()) {
            log.info("Cleanup cascade: unpublishing {} listings for user {}", listings.size(), userId);
            listings.stream()
                    .filter(Listing::isActive)
                    .forEach(Listing::unpublish);
            listingRepository.saveAll(listings);
        }

        // 5. Cascade cleanup from listings
        if (!listingIds.isEmpty()) {
            // Cancel Appointments
            List<Appointment> appointments = appointmentRepository.findByListingIdInAndStatusIn(
                    listingIds,
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED)
            );
            if (!appointments.isEmpty()) {
                log.info("Cleanup cascade: cancelling {} appointments", appointments.size());
                appointments.forEach(a -> a.cancel(userId, isBan ? "Owner account banned" : "Owner account suspended"));
                appointmentRepository.saveAll(appointments);
            }

            // Cancel ListingBoosts
            List<ListingBoost> boosts = listingBoostRepository.findActiveByListingIds(listingIds);
            if (!boosts.isEmpty()) {
                log.info("Cleanup cascade: cancelling {} listing boosts", boosts.size());
                boosts.forEach(ListingBoost::cancel);
                boosts.forEach(listingBoostRepository::save);
            }
        }

        // 6. Cancel Engagements (linked to user OR drafted property/listing)
        List<Engagement> initiatorEngagements = engagementRepository.findByInitiatorId(userId);
        List<Engagement> relatedEngagements = engagementRepository
                .findByListingIdInOrPropertyIdIn(listingIds, propertyIds);

        Set<Engagement> allEngagements = new HashSet<>(initiatorEngagements);
        allEngagements.addAll(relatedEngagements);

        List<Engagement> toCancel = allEngagements.stream()
                .filter(e -> e.getStatus() == EngagementStatus.SUBMITTED || e.getStatus() == EngagementStatus.ACCEPTED)
                .toList();

        if (!toCancel.isEmpty()) {
            log.info("Cleanup cascade: cancelling {} pending/accepted engagements", toCancel.size());
            toCancel.forEach(e -> e.cancel(isBan ? "User account banned" : "User account suspended"));
            toCancel.forEach(engagementRepository::save);
        }

        // 7. Revert Agent Proposal Templates to DRAFT
        List<AgentProposal> proposals = agentProposalRepository
                .findByUserId(userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<AgentProposal> activeProposals = proposals.stream()
                .filter(p -> p.getStatus() == com.sep.realvista.domain.engagement.proposal.AgentProposalStatus.ACTIVE)
                .toList();
        if (!activeProposals.isEmpty()) {
            log.info("Cleanup cascade: reverting {} agent proposal templates to DRAFT", activeProposals.size());
            activeProposals.forEach(AgentProposal::setAsDraft);
            activeProposals.forEach(agentProposalRepository::save);
        }

        // 8. Cancel Subscriptions (Only for Ban)
        if (isBan) {
            List<com.sep.realvista.domain.billing.subscription.UserFeatureSubscription> activeSubs =
                    userFeatureSubscriptionRepository.findAllActiveByUserId(userId);
            if (!activeSubs.isEmpty()) {
                log.info("Cleanup cascade: cancelling {} active subscriptions for banned user {}",
                        activeSubs.size(), userId);
                activeSubs.forEach(com.sep.realvista.domain.billing.subscription.UserFeatureSubscription::cancel);
                activeSubs.forEach(userFeatureSubscriptionRepository::save);
            }
        }
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
        Hibernate.initialize(user.getUserRoles());
        user.getUserRoles().forEach(ur -> {
            if (ur.getRole() != null) {
                Hibernate.initialize(ur.getRole());
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
                .avatarUrl(user.getAvatarUrl())
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
     * <p>
     * The user's email is NOT updated here — it is only stashed as a "pending email"
     * in the OTP cache. The email record in the database is changed only after the
     * user successfully submits the matching OTP via {@link #verifyEmail(UUID, String)}.
     * This prevents an unverified email from silently replacing the user's real address
     * if they abandon the flow.
     */
    public void sendEmailOtp(UUID userId, String targetEmail) {
        User user = userDomainService.getUserOrThrow(userId);

        String normalizedEmail = targetEmail.trim().toLowerCase(Locale.ROOT);

        String currentEmail = user.getEmail() != null ? user.getEmail().getValue() : null;
        boolean isChange = !normalizedEmail.equals(currentEmail);
        if (isChange) {
            userRepository.findByEmailValue(normalizedEmail).ifPresent(existing -> {
                if (!existing.getUserId().equals(userId)) {
                    throw new BusinessConflictException(
                            "Email already exists: " + normalizedEmail,
                            "ERROR_EMAIL_ALREADY_EXISTS",
                            new Object[]{normalizedEmail}
                    );
                }
            });
        }

        // Stash the target email so we can commit it after OTP verification.
        // If the address is unchanged (re-verification), we still store it so the
        // verify step can idempotently detect "no-op" vs "update".
        otpService.store(PENDING_EMAIL_PREFIX + userId, normalizedEmail, OTP_EXPIRY_MINUTES);

        String otp = otpService.generateAndStore(EMAIL_OTP_PREFIX + userId, OTP_EXPIRY_MINUTES);
        String fullName = user.getFullName();
        
        SettingPreference prefs = settingPreferenceRepository.findByUserId(userId)
                .orElse(null);
        String lang = (prefs != null && prefs.getPreferredLanguage() != null) ? prefs.getPreferredLanguage() : "vi";

        emailService.sendDbTemplateMessageAsync(
                normalizedEmail,
                "EMAIL_OTP",
                lang,
                Map.of(
                        "userName", fullName != null && !fullName.isBlank() ? fullName : normalizedEmail,
                        "otp", otp,
                        "expiryMinutes", OTP_EXPIRY_MINUTES
                )
        );
        log.info("Email OTP sent to {} for user {} (pendingChange={})", normalizedEmail, userId, isChange);
    }

    /**
     * Verify the email OTP. Only on a valid OTP do we commit the pending email
     * change (if any) and stamp {@code emailVerifiedAt}. If the OTP is invalid or
     * expired, the user's email remains unchanged.
     */
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse verifyEmail(UUID userId, String otp) {
        if (!otpService.verify(EMAIL_OTP_PREFIX + userId, otp)) {
            throw new BusinessConflictException("OTP không hợp lệ hoặc đã hết hạn", "ERROR_INVALID_OTP");
        }
        User user = userDomainService.getUserOrThrow(userId);

        String pendingEmail = otpService.get(PENDING_EMAIL_PREFIX + userId);
        if (pendingEmail != null && !pendingEmail.isBlank()) {
            String currentEmail = user.getEmail() != null ? user.getEmail().getValue() : null;
            if (!pendingEmail.equals(currentEmail)) {
                // Re-check uniqueness at commit time: another user may have claimed
                // this address between sendOtp and verify.
                userRepository.findByEmailValue(pendingEmail).ifPresent(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        throw new BusinessConflictException(
                                "Email already exists: " + pendingEmail,
                                "EMAIL_ALREADY_EXISTS"
                        );
                    }
                });
                user.updateEmail(pendingEmail);
            }
            otpService.remove(PENDING_EMAIL_PREFIX + userId);
        }

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
                .orElseThrow(() -> new BusinessConflictException("Role OWNER not found", "ERROR_ROLE_NOT_FOUND"));

        UserRole userRole = UserRole.create(user, ownerRole);
        userRoleRepository.save(userRole);

        // Assign LISTING_FREE and 3D_TOUR_FREE packages for new owner
        billingApplicationService.assignDefaultOwnerPackages(userId);

        log.info("OWNER role added successfully to user ID: {}", userId);
        return userMapper.toResponse(userDomainService.getUserOrThrow(userId));
    }
}
