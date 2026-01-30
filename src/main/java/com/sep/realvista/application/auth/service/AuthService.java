package com.sep.realvista.application.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.GoogleIdTokenRequest;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.mapper.AuthenticationMapper;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import com.sep.realvista.infrastructure.security.oauth2.GoogleTokenVerifier;
import com.sep.realvista.infrastructure.security.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final UserApplicationService userApplicationService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final AuthenticationMapper authenticationMapper;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final PasswordUtil passwordUtil;

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        log.debug("Registering new user with email: {}", request.getEmail());

        UserResponse user = userApplicationService.createUser(request);

        log.info("User registered successfully with ID: {} and email: {}",
                user.getUserId(), user.getEmail());

        return user;
    }

    public AuthenticationResponse login(LoginRequest request) {
        log.debug("Authenticating user with email: {}", request.getEmail());

        // Step 1: Authenticate user credentials
        Authentication authentication = authenticateUser(request);

        // Step 2: Retrieve user details for role mapping
        User user = userRepository.findByEmailValue(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // Step 3: Generate JWT token with roles in claims
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        
        // Add roles to JWT claims
        java.util.List<String> roles = user.getUserRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> ur.getRole().getRoleCode().name())
                .toList();
        extraClaims.put("roles", roles);
        
        String token = tokenService.generateToken(extraClaims, userDetails);

        // Step 4: Build authentication response
        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(user, token);

        log.info("User authenticated successfully: {}", request.getEmail());

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
                        "GOOGLE_EMAIL_MISSING"
                );
            }

            // Step 3: Find or create user
            User user = findOrCreateGoogleUser(email, firstName, lastName, avatarUrl);

            // Step 4: Generate JWT token
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail().getValue())
                    .password(user.getPasswordHash())
                    .authorities(java.util.Collections.emptyList())
                    .build();

            String token = tokenService.generateToken(userDetails);

            // Step 5: Build authentication response
            AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(user, token);

            log.info("Mobile Google login successful for platform {} and user: {}",
                    request.getPlatform(), email);

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Invalid platform or token for mobile Google authentication: {}", e.getMessage());
            throw new BusinessConflictException(
                    "Invalid authentication request: " + e.getMessage(),
                    "INVALID_AUTH_REQUEST"
            );
        } catch (Exception e) {
            log.error("Mobile Google authentication failed for platform {}", request.getPlatform(), e);
            throw new BusinessConflictException(
                    "Google authentication failed: " + e.getMessage(),
                    "GOOGLE_AUTH_FAILED"
            );
        }
    }

    private User findOrCreateGoogleUser(String email, String firstName,
                                        String lastName, String avatarUrl) {
        return userRepository.findByEmailValue(email)
                .orElseGet(() -> createGoogleUser(email, firstName, lastName, avatarUrl));
    }

    private User createGoogleUser(String email, String firstName,
                                  String lastName, String avatarUrl) {
        log.info("Creating new user from Google login: {}", email);

        // Generate a random hashed password for Google users (they won't use it)
        String hashedPassword = passwordUtil.generateRandomHashedPassword();

        // Generate business name from user's name or email
        String businessName = (firstName != null && lastName != null)
                ? firstName + " " + lastName
                : email.split("@")[0];

        User newUser = User.builder()
                .email(Email.of(email))
                .passwordHash(hashedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .businessName(businessName)
                .avatarUrl(avatarUrl)
                .status(UserStatus.ACTIVE) // Google users are automatically active
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("New Google user created with ID: {}", savedUser.getUserId());

        return savedUser;
    }

    private Authentication authenticateUser(LoginRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }
}
