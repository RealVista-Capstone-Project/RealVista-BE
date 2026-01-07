package com.sep.realvista.unit.application.service;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.service.AuthService;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.infrastructure.config.security.JwtService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService.
 * 
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
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

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
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));

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
    }

    @Test
    @DisplayName("Should generate JWT token after successful authentication")
    void shouldGenerateJwtTokenAfterSuccessfulAuthentication() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result.getToken()).isEqualTo(expectedToken);
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    @DisplayName("Should return user details in authentication response")
    void shouldReturnUserDetailsInAuthenticationResponse() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getEmail().getValue());
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
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found after authentication");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmailValue("test@example.com");
    }

    @Test
    @DisplayName("Should use correct email in authentication token")
    void shouldUseCorrectEmailInAuthenticationToken() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));

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
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result.getType()).isEqualTo("Bearer");
    }

    // ============================================
    // Integration Between Methods
    // ============================================

    @Test
    @DisplayName("Should handle authentication flow correctly")
    void shouldHandleAuthenticationFlowCorrectly() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByEmailValue("test@example.com"))
                .thenReturn(Optional.of(user));

        // Act
        AuthenticationResponse result = authService.login(loginRequest);

        // Assert - Verify correct order of operations
        // 1. Authentication
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        // 2. JWT generation
        verify(jwtService).generateToken(userDetails);
        // 3. User retrieval
        verify(userRepository).findByEmailValue("test@example.com");
        
        // Final result is complete
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getUserId()).isNotNull();
        assertThat(result.getEmail()).isNotNull();
    }
}

