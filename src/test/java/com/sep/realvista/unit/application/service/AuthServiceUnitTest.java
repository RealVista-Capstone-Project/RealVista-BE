package com.sep.realvista.unit.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.GoogleIdTokenRequest;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.dto.MobilePlatform;
import com.sep.realvista.application.auth.mapper.AuthenticationMapper;
import com.sep.realvista.application.auth.service.AuthService;
import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import com.sep.realvista.infrastructure.security.oauth2.GoogleTokenVerifier;
import com.sep.realvista.infrastructure.security.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService.
 * <p>
 * These are pure unit tests with all dependencies mocked.
 * Tests the authentication business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceUnitTest {

    @Mock
    private UserApplicationService userApplicationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private AuthService authService;

    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;
    private LoginRequest loginRequest;
    private User user;
    private UserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup test data
        createUserRequest = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .email(Email.of("test@example.com"))
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        userDetails = mock(UserDetails.class);
        authentication = mock(Authentication.class);
    }

    // ============================================
    // Registration Tests
    // ============================================

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(userApplicationService.createUser(createUserRequest))
                .thenReturn(userResponse);

        // Act
        UserResponse result = authService.register(createUserRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");

        verify(userApplicationService).createUser(createUserRequest);
    }

    @Test
    @DisplayName("Should propagate exception when registration fails")
    void shouldPropagateExceptionWhenRegistrationFails() {
        // Arrange
        when(userApplicationService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(createUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");

        verify(userApplicationService).createUser(createUserRequest);
    }

    // ============================================
    // Login Tests - Happy Path
    // ============================================

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfullyWithValidCredentials() {
        // Arrange
        String expectedToken = "jwt.token.here";
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token(expectedToken)
                .type("Bearer")
                .userId(1L)
                .email("test@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationMapper.toAuthenticationResponse(user, expectedToken))
                .thenReturn(expectedResponse);

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(expectedToken);
        assertThat(result.getType()).isEqualTo("Bearer");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
        verify(userRepository).findByEmailValue("test@example.com");
        verify(authenticationMapper).toAuthenticationResponse(user, expectedToken);
    }

    @Test
    @DisplayName("Should generate JWT token after successful authentication")
    void shouldGenerateJwtTokenAfterSuccessfulAuthentication() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token(expectedToken)
                .type("Bearer")
                .userId(1L)
                .email("test@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationMapper.toAuthenticationResponse(user, expectedToken))
                .thenReturn(expectedResponse);

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result.getToken()).isEqualTo(expectedToken);
        verify(jwtService).generateToken(userDetails);
        verify(authenticationMapper).toAuthenticationResponse(user, expectedToken);
    }

    @Test
    @DisplayName("Should return user details in authentication response")
    void shouldReturnUserDetailsInAuthenticationResponse() {
        // Arrange
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token("token")
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail().getValue())
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationMapper.toAuthenticationResponse(user, "token"))
                .thenReturn(expectedResponse);

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getEmail().getValue());
        verify(authenticationMapper).toAuthenticationResponse(user, "token");
    }

    // ============================================
    // Login Tests - Error Cases
    // ============================================

    @Test
    @DisplayName("Should throw exception when credentials are invalid")
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found after authentication")
    void shouldThrowExceptionWhenUserNotFoundAfterAuthentication() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: " + "test@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmailValue("test@example.com");
    }

    @Test
    @DisplayName("Should use correct email in authentication token")
    void shouldUseCorrectEmailInAuthenticationToken() {
        // Arrange
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token("token")
                .type("Bearer")
                .userId(1L)
                .email("test@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationMapper.toAuthenticationResponse(user, "token"))
                .thenReturn(expectedResponse);

        // Act
        authService.login(loginRequest);

        // Assert - Verify the authentication manager was called with correct credentials
        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test
    @DisplayName("Should return Bearer token type")
    void shouldReturnBearerTokenType() {
        // Arrange
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token("token")
                .type("Bearer")
                .userId(1L)
                .email("test@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationMapper.toAuthenticationResponse(user, "token"))
                .thenReturn(expectedResponse);

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result.getType()).isEqualTo("Bearer");
        verify(authenticationMapper).toAuthenticationResponse(user, "token");
    }

    // ============================================
    // Integration Between Methods
    // ============================================

    @Test
    @DisplayName("Should handle authentication flow correctly")
    void shouldHandleAuthenticationFlowCorrectly() {
        // Arrange
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token("token")
                .type("Bearer")
                .userId(1L)
                .email("test@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationMapper.toAuthenticationResponse(user, "token"))
                .thenReturn(expectedResponse);

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert - Verify correct order of operations
        // 1. Authentication
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        // 2. JWT generation
        verify(jwtService).generateToken(userDetails);
        // 3. User retrieval
        verify(userRepository).findByEmailValue("test@example.com");
        // 4. Response mapping
        verify(authenticationMapper).toAuthenticationResponse(user, "token");

        // Final result is complete
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getUserId()).isNotNull();
        assertThat(result.getEmail()).isNotNull();
    }

    // ============================================
    // Google Mobile Login Tests
    // ============================================

    @Test
    @DisplayName("Should login successfully with valid Google ID token")
    void shouldLoginSuccessfullyWithValidGoogleIdToken() throws Exception {
        // Arrange
        String idToken = "valid.google.id.token";
        String email = "google.user@gmail.com";
        String firstName = "Jane";
        String lastName = "Smith";
        String avatarUrl = "https://example.com/avatar.jpg";
        String hashedPassword = "hashed_random_password";
        String jwtToken = "generated.jwt.token";
        MobilePlatform platform = MobilePlatform.ANDROID;

        GoogleIdTokenRequest request = GoogleIdTokenRequest.builder()
                .idToken(idToken)
                .platform(platform)  // Added platform
                .build();

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.set("given_name", firstName);
        payload.set("family_name", lastName);
        payload.set("picture", avatarUrl);

        User googleUser = User.builder()
                .id(2L)
                .email(Email.of(email))
                .passwordHash(hashedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl(avatarUrl)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token(jwtToken)
                .type("Bearer")
                .userId(2L)
                .email(email)
                .build();

        when(googleTokenVerifier.verifyTokenForPlatform(idToken, platform)).thenReturn(payload);  // Updated method
        when(googleTokenVerifier.getEmail(payload)).thenReturn(email);
        when(googleTokenVerifier.getGivenName(payload)).thenReturn(firstName);
        when(googleTokenVerifier.getFamilyName(payload)).thenReturn(lastName);
        when(googleTokenVerifier.getPictureUrl(payload)).thenReturn(avatarUrl);
        when(userRepository.findByEmailValue(email)).thenReturn(Optional.empty());
        when(passwordUtil.generateRandomHashedPassword()).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(googleUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(jwtToken);
        when(authenticationMapper.toAuthenticationResponse(googleUser, jwtToken))
                .thenReturn(expectedResponse);

        // Act
        AuthenticationResponse result = authService.loginWithGoogleMobile(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(jwtToken);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getUserId()).isEqualTo(2L);

        verify(googleTokenVerifier).verifyTokenForPlatform(idToken, platform);  // Updated verification
        verify(userRepository).save(any(User.class));
        verify(passwordUtil).generateRandomHashedPassword();
        verify(jwtService).generateToken(any(UserDetails.class));
        verify(authenticationMapper).toAuthenticationResponse(googleUser, jwtToken);
    }

    @Test
    @DisplayName("Should use existing user for Google login")
    void shouldUseExistingUserForGoogleLogin() throws Exception {
        // Arrange
        String idToken = "valid.google.id.token";
        String email = "existing.user@gmail.com";
        String jwtToken = "generated.jwt.token";
        MobilePlatform platform = MobilePlatform.IOS;

        GoogleIdTokenRequest request = GoogleIdTokenRequest.builder()
                .idToken(idToken)
                .platform(platform)  // Added platform
                .build();

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);

        User existingUser = User.builder()
                .id(3L)
                .email(Email.of(email))
                .passwordHash("existing_hash")
                .firstName("Existing")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token(jwtToken)
                .type("Bearer")
                .userId(3L)
                .email(email)
                .build();

        when(googleTokenVerifier.verifyTokenForPlatform(idToken, platform)).thenReturn(payload);  // Updated method
        when(googleTokenVerifier.getEmail(payload)).thenReturn(email);
        when(googleTokenVerifier.getGivenName(payload)).thenReturn(null);
        when(googleTokenVerifier.getFamilyName(payload)).thenReturn(null);
        when(googleTokenVerifier.getPictureUrl(payload)).thenReturn(null);
        when(userRepository.findByEmailValue(email)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(jwtToken);
        when(authenticationMapper.toAuthenticationResponse(existingUser, jwtToken))
                .thenReturn(expectedResponse);

        // Act
        AuthenticationResponse result = authService.loginWithGoogleMobile(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(3L);
        assertThat(result.getEmail()).isEqualTo(email);

        verify(googleTokenVerifier).verifyTokenForPlatform(idToken, platform);  // Updated verification
        verify(userRepository).findByEmailValue(email);
        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
        verify(passwordUtil, org.mockito.Mockito.never()).generateRandomHashedPassword();
    }

    @Test
    @DisplayName("Should throw exception when Google ID token is invalid")
    void shouldThrowExceptionWhenGoogleIdTokenIsInvalid() throws Exception {
        // Arrange
        String invalidToken = "invalid.token";
        MobilePlatform platform = MobilePlatform.ANDROID;
        
        GoogleIdTokenRequest request = GoogleIdTokenRequest.builder()
                .idToken(invalidToken)
                .platform(platform)  // Added platform
                .build();

        when(googleTokenVerifier.verifyTokenForPlatform(invalidToken, platform))  // Updated method
                .thenThrow(new IllegalArgumentException("Invalid ID token"));

        // Act & Assert
        assertThatThrownBy(() -> authService.loginWithGoogleMobile(request))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Invalid authentication request");  // Updated to match actual message

        verify(googleTokenVerifier).verifyTokenForPlatform(invalidToken, platform);  // Updated verification
    }

    @Test
    @DisplayName("Should throw exception when email is missing from Google token")
    void shouldThrowExceptionWhenEmailIsMissingFromGoogleToken() throws Exception {
        // Arrange
        String idToken = "valid.token.without.email";
        MobilePlatform platform = MobilePlatform.IOS;
        
        GoogleIdTokenRequest request = GoogleIdTokenRequest.builder()
                .idToken(idToken)
                .platform(platform)  // Added platform
                .build();

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();

        when(googleTokenVerifier.verifyTokenForPlatform(idToken, platform)).thenReturn(payload);  // Updated method
        when(googleTokenVerifier.getEmail(payload)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> authService.loginWithGoogleMobile(request))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Email not provided by Google");

        verify(googleTokenVerifier).verifyTokenForPlatform(idToken, platform);  // Updated verification
        verify(googleTokenVerifier).getEmail(payload);
    }

    @Test
    @DisplayName("Should create new user with ACTIVE status for Google login")
    void shouldCreateNewUserWithActiveStatusForGoogleLogin() throws Exception {
        // Arrange
        String idToken = "valid.google.id.token";
        String email = "new.google.user@gmail.com";
        String hashedPassword = "hashed_password";
        String jwtToken = "jwt.token";
        MobilePlatform platform = MobilePlatform.ANDROID;

        GoogleIdTokenRequest request = GoogleIdTokenRequest.builder()
                .idToken(idToken)
                .platform(platform)  // Added platform
                .build();

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);

        User newGoogleUser = User.builder()
                .id(4L)
                .email(Email.of(email))
                .passwordHash(hashedPassword)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .token(jwtToken)
                .type("Bearer")
                .userId(4L)
                .email(email)
                .build();

        when(googleTokenVerifier.verifyTokenForPlatform(idToken, platform)).thenReturn(payload);  // Updated method
        when(googleTokenVerifier.getEmail(payload)).thenReturn(email);
        when(googleTokenVerifier.getGivenName(payload)).thenReturn(null);
        when(googleTokenVerifier.getFamilyName(payload)).thenReturn(null);
        when(googleTokenVerifier.getPictureUrl(payload)).thenReturn(null);
        when(userRepository.findByEmailValue(email)).thenReturn(Optional.empty());
        when(passwordUtil.generateRandomHashedPassword()).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(newGoogleUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(jwtToken);
        when(authenticationMapper.toAuthenticationResponse(newGoogleUser, jwtToken))
                .thenReturn(expectedResponse);

        // Act
        authService.loginWithGoogleMobile(request);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(passwordUtil).generateRandomHashedPassword();
    }
}

