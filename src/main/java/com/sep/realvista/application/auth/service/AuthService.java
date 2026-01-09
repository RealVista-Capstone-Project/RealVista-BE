package com.sep.realvista.application.auth.service;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserNotFoundException;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.infrastructure.config.security.JwtService;
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
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        log.debug("Registering new user with email: {}", request.getEmail());

        UserResponse user = userApplicationService.createUser(request);

        log.info("User registered successfully with ID: {} and email: {}",
                user.getId(), user.getEmail());

        return user;
    }

    public AuthenticationResponse login(LoginRequest request) {
        log.debug("Authenticating user with email: {}", request.getEmail());

        // Step 1: Authenticate user credentials
        Authentication authentication = authenticateUser(request);

        // Step 2: Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        // Step 3: Retrieve user details
        User user = userRepository.findByEmailValue(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // Step 4: Build authentication response
        AuthenticationResponse response = buildAuthenticationResponse(token, user);

        log.info("User authenticated successfully: {}", request.getEmail());

        return response;
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

    private AuthenticationResponse buildAuthenticationResponse(String token, User user) {
        return AuthenticationResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail().getValue())
                .build();
    }
}
