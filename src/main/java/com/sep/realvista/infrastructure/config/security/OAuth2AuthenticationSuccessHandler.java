package com.sep.realvista.infrastructure.config.security;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.infrastructure.config.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

/**
 * Custom OAuth2 authentication success handler.
 * Handles the success flow for Google OAuth2 login.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    @Value("${spring.application.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String userEmail = oAuth2User.getAttribute("email");
        log.info("OAuth2 login successful for user: {}", userEmail != null ? userEmail : "unknown");

        try {
            // Extract user information from OAuth2 provider
            String email = oAuth2User.getAttribute("email");
            String firstName = oAuth2User.getAttribute("given_name");
            String lastName = oAuth2User.getAttribute("family_name");
            String avatarUrl = oAuth2User.getAttribute("picture");

            if (email == null) {
                log.error("Email not provided by OAuth2 provider");
                String errorUrl = buildErrorRedirectUrl(SecurityConstants.OAuth2.ERROR_NO_EMAIL);
                response.sendRedirect(errorUrl);
                return;
            }

            // Find or create user
            User user = findOrCreateUser(email, firstName, lastName, avatarUrl);

            // Generate JWT token
            String jwtToken = jwtService.generateToken(
                    new org.springframework.security.core.userdetails.User(
                            user.getEmail().getValue(),
                            user.getPasswordHash(),
                            java.util.Collections.emptyList()
                    )
            );

            // Redirect to frontend with token
            String redirectUrl = buildSuccessRedirectUrl(jwtToken, user.getId(), email);

            log.info("Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error during OAuth2 authentication success handling", e);
            String errorUrl = buildErrorRedirectUrl(SecurityConstants.OAuth2.ERROR_AUTH_FAILED);
            response.sendRedirect(errorUrl);
        }
    }

    private User findOrCreateUser(String email, String firstName, String lastName, String avatarUrl) {
        return userRepository.findByEmailValue(email)
                .orElseGet(() -> createNewOAuth2User(email, firstName, lastName, avatarUrl));
    }

    private User createNewOAuth2User(String email, String firstName, String lastName, String avatarUrl) {
        log.info("Creating new user from OAuth2 login: {}", email);

        // Generate a random password for OAuth2 users (they won't use it)
        String randomPassword = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(randomPassword);

        User newUser = User.builder()
                .email(Email.of(email))
                .passwordHash(hashedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl(avatarUrl)
                .status(UserStatus.ACTIVE) // OAuth2 users are automatically active
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        Long userId = savedUser.getId();
        log.info("New OAuth2 user created with ID: {}", userId != null ? userId : "unknown");

        return savedUser;
    }

    private String buildSuccessRedirectUrl(String jwtToken, Long userId, String email) {
        return UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path(SecurityConstants.OAuth2.CALLBACK_PATH)
                .queryParam(SecurityConstants.OAuth2.PARAM_ACCESS_TOKEN, jwtToken)
                .queryParam(SecurityConstants.OAuth2.PARAM_USER_ID, userId)
                .queryParam(SecurityConstants.OAuth2.PARAM_EMAIL, email)
                .build()
                .toUriString();
    }

    private String buildErrorRedirectUrl(String errorType) {
        return UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path(SecurityConstants.OAuth2.ERROR_PATH)
                .queryParam(SecurityConstants.OAuth2.PARAM_ERROR, errorType)
                .build()
                .toUriString();
    }
}

