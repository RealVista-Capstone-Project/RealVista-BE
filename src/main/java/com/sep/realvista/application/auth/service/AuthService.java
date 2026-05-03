package com.sep.realvista.application.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.GoogleIdTokenRequest;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.dto.ResetPasswordRequest;
import com.sep.realvista.application.auth.mapper.AuthenticationMapper;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.OtpService;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.exception.AccountStatusException;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import com.sep.realvista.infrastructure.security.oauth2.GoogleTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for authentication operations.
 * <p>
 * This service handles the business logic for authentication,
 * following clean architecture and DDD principles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String PASSWORD_RESET_KEY_PREFIX = "pwdreset:";
    private static final int PASSWORD_RESET_EXPIRY_MINUTES = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserApplicationService userApplicationService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final AuthenticationMapper authenticationMapper;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final OtpService otpService;
    private final EmailService emailService;

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        log.debug("Registering new user with email: {}", request.getEmail());

        UserResponse user = userApplicationService.createUser(request);

        log.info("User registered successfully with ID: {} and email: {}",
                user.getUserId(), user.getEmail());

        return user;
    }

    public AuthenticationResponse login(LoginRequest request) {
        boolean loginByEmail = request.getEmail() != null && !request.getEmail().isBlank();
        String identifier = loginByEmail ? request.getEmail() : request.getPhone();
        log.debug("Authenticating user with {}: {}", loginByEmail ? "email" : "phone", identifier);

        // Step 1: Check if user exists and their account status BEFORE authentication
        // This avoids password hashing for blocked accounts and returns specific error codes
        User user = loginByEmail
                ? userRepository.findByEmailValue(identifier)
                        .orElseThrow(() -> new UserNotFoundException(identifier))
                : userRepository.findByPhone(identifier)
                        .orElseThrow(() -> new UserNotFoundException(identifier));

        checkUserStatus(user);

        // Step 2: Authenticate user credentials
        Authentication authentication = authenticateUser(identifier, request.getPassword());

        // Step 3: Generate JWT token with roles in claims
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();

        java.util.List<String> roles = user.getUserRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> ur.getRole().getRoleCode().name())
                .toList();
        extraClaims.put("roles", roles);

        String token = tokenService.generateToken(extraClaims, userDetails);

        // Step 4: Build authentication response
        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(user, token);

        log.info("User authenticated successfully: {}", identifier);

        return response;
    }

    /**
     * Authenticate user using Google ID token (for mobile apps).
     * <p>
     * Mobile apps should use Google Sign-In SDK to obtain an ID token,
     * then send it to this endpoint for validation and authentication.
     * <p>
     * This approach avoids the private IP redirect issue that occurs
     * with standard OAuth2 web flows on mobile devices.
     * <p>
     * Platform-specific validation:
     * - Android tokens are validated against Android OAuth client ID
     * - iOS tokens are validated against iOS OAuth client ID
     *
     * @param request the Google ID token request with platform information
     * @return authentication response with JWT token
     */
    @Transactional
    public AuthenticationResponse loginWithGoogleMobile(GoogleIdTokenRequest request) {
        log.debug("Processing mobile Google login with ID token for platform: {}", request.getPlatform());

        try {
            // Step 1: Verify Google ID token for specific platform
            GoogleIdToken.Payload payload = googleTokenVerifier.verifyTokenForPlatform(
                    request.getIdToken(),
                    request.getPlatform()
            );

            // Step 2: Extract user information
            String email = googleTokenVerifier.getEmail(payload);
            String firstName = googleTokenVerifier.getGivenName(payload);
            String lastName = googleTokenVerifier.getFamilyName(payload);
            String avatarUrl = googleTokenVerifier.getPictureUrl(payload);

            if (email == null || email.isBlank()) {
                throw new BusinessConflictException(
                        "Email not provided by Google",
                        "ERROR_GOOGLE_EMAIL_MISSING"
                );
            }

            // Step 3: Find or create user
            User user = findOrCreateGoogleUser(email, firstName, lastName, avatarUrl);

            // Step 3.5: Check account status
            checkUserStatus(user);

            // Step 4: Generate JWT token with roles in claims
            java.util.List<String> roles = user.getUserRoles().stream()
                    .filter(ur -> ur.getRole() != null)
                    .map(ur -> ur.getRole().getRoleCode().name())
                    .toList();
            java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
            extraClaims.put("roles", roles);
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail().getValue())
                    .password(user.getPasswordHash())
                    .authorities(java.util.Collections.emptyList())
                    .build();

            String token = tokenService.generateToken(extraClaims, userDetails);

            // Step 5: Build authentication response
            AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(user, token);

            log.info("Mobile Google login successful for platform {} and user: {}",
                    request.getPlatform(), email);

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Invalid platform or token for mobile Google authentication: {}", e.getMessage());
            throw new BusinessConflictException(
                    "Invalid authentication request: " + e.getMessage(),
                    "ERROR_INVALID_AUTH_REQUEST"
            );
        } catch (Exception e) {
            log.error("Mobile Google authentication failed for platform {}", request.getPlatform(), e);
            throw new BusinessConflictException(
                    "Google authentication failed: " + e.getMessage(),
                    "ERROR_GOOGLE_AUTH_FAILED"
            );
        }
    }

    /**
     * Request a password reset email. Always completes without revealing whether the email exists.
     * <p>
     * Reset emails are only sent for {@link UserStatus#ACTIVE} and {@link UserStatus#VERIFIED} accounts.
     * {@link UserStatus#SUSPENDED} and {@link UserStatus#BANNED} are ignored (same generic response).
     */
    public void requestPasswordReset(String rawEmail, String uiLocale, String frontendBaseUrl) {
        if (rawEmail == null || rawEmail.isBlank()) {
            return;
        }
        String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
        Optional<User> userOpt = userRepository.findByEmailValue(normalized);
        if (userOpt.isEmpty()) {
            log.debug("Password reset requested for unknown email (generic response)");
            return;
        }
        User user = userOpt.get();
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.BANNED) {
            log.warn("Password reset ignored for ineligible status {} user {}", user.getStatus(), normalized);
            return;
        }

        String token = generatePasswordResetToken();
        otpService.store(PASSWORD_RESET_KEY_PREFIX + token, user.getUserId().toString(), PASSWORD_RESET_EXPIRY_MINUTES);

        String base = frontendBaseUrl == null ? "" : frontendBaseUrl.replaceAll("/+$", "");
        String locale = "en".equalsIgnoreCase(uiLocale) ? "en" : "vi";
        String resetLink = base + "/" + locale + "/reset-password?token=" + token;

        String displayName = user.getFullName();
        if (displayName == null || displayName.isBlank()) {
            displayName = normalized;
        }

        emailService.sendDbTemplateMessageAsync(
                normalized,
                "PASSWORD_RESET",
                locale,
                Map.of(
                        "userName", displayName,
                        "resetLink", resetLink,
                        "expiryMinutes", PASSWORD_RESET_EXPIRY_MINUTES
                )
        );
        log.info("Password reset email queued for {}", normalized);
    }

    /**
     * Complete password reset using a one-time token from email.
     */
    @Transactional
    public void resetPasswordWithToken(ResetPasswordRequest request) {
        String token = request.getToken() == null ? "" : request.getToken().trim();
        if (token.isEmpty()) {
            throw new BusinessConflictException(
                    "Invalid or expired reset link",
                    "ERROR_INVALID_OR_EXPIRED_RESET_TOKEN"
            );
        }
        String userIdStr = otpService.get(PASSWORD_RESET_KEY_PREFIX + token);
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new BusinessConflictException(
                    "Invalid or expired reset link",
                    "ERROR_INVALID_OR_EXPIRED_RESET_TOKEN"
            );
        }
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessConflictException(
                    "Invalid or expired reset link",
                    "ERROR_INVALID_OR_EXPIRED_RESET_TOKEN"
            );
        }

        userApplicationService.resetPasswordForgotten(userId, request.getNewPassword());
        otpService.remove(PASSWORD_RESET_KEY_PREFIX + token);
        log.info("Password reset with token completed for user {}", userId);
    }

    private static String generatePasswordResetToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private User findOrCreateGoogleUser(String email, String firstName,
                                        String lastName, String avatarUrl) {
        return userRepository.findByEmailValue(email)
                .map(user -> {
                    if (!user.isEmailVerified()) {
                        user.verifyEmail();
                        return userRepository.save(user);
                    }
                    return user;
                })
                .orElseGet(() -> userApplicationService.createGoogleUser(email, firstName, lastName, avatarUrl));
    }

    private Authentication authenticateUser(String identifier, String password) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, password)
            );
        } catch (Exception e) {
            log.error("Authentication failed for identifier: {}", identifier, e);
            throw e;
        }
    }

    private void checkUserStatus(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Login attempt for suspended user: {}", user.getEmail().getValue());
            throw new AccountStatusException(
                    "Your account has been suspended. Please contact support.",
                    "ACCOUNT_SUSPENDED"
            );
        }
        if (user.getStatus() == UserStatus.BANNED) {
            log.warn("Login attempt for banned user: {}", user.getEmail().getValue());
            throw new AccountStatusException("Your account has been permanently banned.", "ACCOUNT_BANNED");
        }
    }
}
