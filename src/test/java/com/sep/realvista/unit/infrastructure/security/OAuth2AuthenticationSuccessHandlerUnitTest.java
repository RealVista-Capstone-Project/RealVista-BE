package com.sep.realvista.unit.infrastructure.security;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.infrastructure.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.sep.realvista.infrastructure.security.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OAuth2AuthenticationSuccessHandler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2AuthenticationSuccessHandler Unit Tests")
class OAuth2AuthenticationSuccessHandlerUnitTest {

    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_AVATAR_URL = "https://example.com/avatar.jpg";
    private static final String TEST_JWT_TOKEN = "test.jwt.token";
    private static final Long TEST_USER_ID = 1L;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oAuth2User;
    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        successHandler = new OAuth2AuthenticationSuccessHandler(
                userRepository,
                tokenService,
                passwordService,
                FRONTEND_URL
        );
    }

    @Test
    @DisplayName("Should create new user and redirect on first-time OAuth2 login")
    void shouldCreateNewUserOnFirstLogin() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(oAuth2User.getAttribute("given_name")).thenReturn(TEST_FIRST_NAME);
        when(oAuth2User.getAttribute("family_name")).thenReturn(TEST_LAST_NAME);
        when(oAuth2User.getAttribute("picture")).thenReturn(TEST_AVATAR_URL);

        when(userRepository.findByEmailValue(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordService.encode(anyString())).thenReturn("hashed_password");

        User savedUser = createTestUser();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenService.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRepository).findByEmailValue(TEST_EMAIL);
        verify(userRepository).save(any(User.class));
        verify(tokenService).generateToken(any(UserDetails.class));

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl)
                .contains(FRONTEND_URL)
                .contains("/auth/callback")
                .contains("access_token=" + TEST_JWT_TOKEN)
                .contains("user_id=" + TEST_USER_ID)
                .contains("email=" + TEST_EMAIL);
    }

    @Test
    @DisplayName("Should use existing user and redirect on subsequent OAuth2 login")
    void shouldUseExistingUserOnSubsequentLogin() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(oAuth2User.getAttribute("given_name")).thenReturn(TEST_FIRST_NAME);
        when(oAuth2User.getAttribute("family_name")).thenReturn(TEST_LAST_NAME);
        when(oAuth2User.getAttribute("picture")).thenReturn(TEST_AVATAR_URL);

        User existingUser = createTestUser();
        when(userRepository.findByEmailValue(TEST_EMAIL)).thenReturn(Optional.of(existingUser));
        when(tokenService.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRepository).findByEmailValue(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class)); // Should not create new user
        verify(tokenService).generateToken(any(UserDetails.class));

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl)
                .contains(FRONTEND_URL)
                .contains("/auth/callback")
                .contains("access_token=" + TEST_JWT_TOKEN);
    }

    @Test
    @DisplayName("Should redirect to error page when email is null")
    void shouldRedirectToErrorWhenEmailIsNull() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRepository, never()).findByEmailValue(anyString());
        verify(userRepository, never()).save(any(User.class));

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl)
                .contains(FRONTEND_URL)
                .contains("/login")
                .contains("error=no_email");
    }

    @Test
    @DisplayName("Should redirect to error page on exception during authentication")
    void shouldHandleExceptionDuringAuthentication() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmailValue(TEST_EMAIL)).thenThrow(new RuntimeException("Database error"));

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl)
                .contains(FRONTEND_URL)
                .contains("/login")
                .contains("error=auth_failed");
    }

    @Test
    @DisplayName("Should create user with ACTIVE status for OAuth2 users")
    void shouldCreateUserWithActiveStatus() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(oAuth2User.getAttribute("given_name")).thenReturn(TEST_FIRST_NAME);
        when(oAuth2User.getAttribute("family_name")).thenReturn(TEST_LAST_NAME);
        when(oAuth2User.getAttribute("picture")).thenReturn(TEST_AVATAR_URL);

        when(userRepository.findByEmailValue(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordService.encode(anyString())).thenReturn("hashed_password");

        User savedUser = createTestUser();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenService.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(capturedUser.getEmail().getValue()).isEqualTo(TEST_EMAIL);
        assertThat(capturedUser.getFirstName()).isEqualTo(TEST_FIRST_NAME);
        assertThat(capturedUser.getLastName()).isEqualTo(TEST_LAST_NAME);
        assertThat(capturedUser.getAvatarUrl()).isEqualTo(TEST_AVATAR_URL);
    }

    @Test
    @DisplayName("Should handle OAuth2 login with missing optional fields")
    void shouldHandleLoginWithMissingOptionalFields() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(oAuth2User.getAttribute("given_name")).thenReturn(null); // Missing first name
        when(oAuth2User.getAttribute("family_name")).thenReturn(null); // Missing last name
        when(oAuth2User.getAttribute("picture")).thenReturn(null); // Missing avatar

        when(userRepository.findByEmailValue(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordService.encode(anyString())).thenReturn("hashed_password");

        User savedUser = User.builder()
                .id(TEST_USER_ID)
                .email(Email.of(TEST_EMAIL))
                .passwordHash("hashed_password")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenService.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail().getValue()).isEqualTo(TEST_EMAIL);
        assertThat(capturedUser.getFirstName()).isNull();
        assertThat(capturedUser.getLastName()).isNull();
        assertThat(capturedUser.getAvatarUrl()).isNull();

        verify(response).sendRedirect(contains("/auth/callback"));
    }


    private User createTestUser() {
        return User.builder()
                .id(TEST_USER_ID)
                .email(Email.of(TEST_EMAIL))
                .passwordHash("hashed_password")
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .avatarUrl(TEST_AVATAR_URL)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }
}


