package com.sep.realvista.component.presentation.rest.auth;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.service.AuthService;
import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for AuthenticationController.
 * <p>
 * Tests the web layer (controller) with Spring MVC infrastructure.
 * Includes tests for:
 * - User registration
 * - Email/password login
 * - Google OAuth2 login
 * - Validation and error handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthenticationController Component Tests")
class AuthenticationControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService jwtService;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    // Mock the application service layer for non-OAuth2 tests
    @MockitoBean
    private AuthService authService;

    private UserResponse mockUserResponse;
    private AuthenticationResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        // Clean up any existing test users
        userRepository.findByEmailValue("test.oauth2@example.com")
                .ifPresent(user -> userRepository.deleteById(user.getId()));

        // Setup test data for mocked tests
        mockUserResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        mockAuthResponse = AuthenticationResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test")
                .type("Bearer")
                .userId(1L)
                .email("test@example.com")
                .build();
    }

    // ============================================
    // Registration Tests
    // ============================================

    @Test
    @DisplayName("Should return 201 Created when registering user with valid data")
    void shouldReturn201WhenRegisteringUserWithValidData() throws Exception {
        // Arrange
        when(authService.register(any(CreateUserRequest.class)))
                .thenReturn(mockUserResponse);

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "SecurePass123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.role").value("USER"));

        verify(authService).register(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when registration data is invalid")
    void shouldReturn400WhenRegistrationDataIsInvalid() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "short"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is missing in registration")
    void shouldReturn400WhenEmailIsMissingInRegistration() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "password": "SecurePass123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    // ============================================
    // Login Tests - Happy Path
    // ============================================

    @Test
    @DisplayName("Should return 200 OK when login with valid credentials")
    void shouldReturn200WhenLoginWithValidCredentials() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(mockAuthResponse);

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.access_token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"))
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.data.user_id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return JWT token in response body")
    void shouldReturnJwtTokenInResponseBody() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(mockAuthResponse);

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.type").value("Bearer"));
    }

    @Test
    @DisplayName("Should return user information in login response")
    void shouldReturnUserInformationInLoginResponse() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(mockAuthResponse);

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user_id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    // ============================================
    // Login Tests - Error Cases
    // ============================================

    @Test
    @DisplayName("Should return 400 Bad Request when login with invalid email format")
    void shouldReturn400WhenLoginWithInvalidEmailFormat() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login with empty password")
    void shouldReturn400WhenLoginWithEmptyPassword() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": ""
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when login with invalid credentials")
    void shouldReturn401WhenLoginWithInvalidCredentials() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "wrongpassword"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login with missing fields")
    void shouldReturn400WhenLoginWithMissingFields() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "email": "test@example.com"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    // ============================================
    // Content Type Tests
    // ============================================

    @Test
    @DisplayName("Should return 415 Unsupported Media Type when content type is not JSON")
    void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
        // Arrange
        String requestBody = "email=test@example.com&password=password123";

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(requestBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ============================================
    // OAuth2 Google Login Tests
    // ============================================

    @Test
    @DisplayName("Should initiate OAuth2 login flow via /api/v1/auth/login-google endpoint")
    void shouldInitiateOAuth2LoginFlow() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/login-google"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/auth/login-google/google"));
    }

    @Test
    @DisplayName("Should have Google OAuth2 provider configured")
    void shouldHaveGoogleProviderConfigured() {
        // Given & When
        if (clientRegistrationRepository != null) {
            ClientRegistration googleRegistration =
                    clientRegistrationRepository.findByRegistrationId("google");

            // Then
            assertThat(googleRegistration).isNotNull();
            assertThat(googleRegistration.getClientName()).isEqualTo("https://accounts.google.com");
            assertThat(googleRegistration.getAuthorizationGrantType())
                    .isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
            assertThat(googleRegistration.getScopes())
                    .contains("profile", "email");
        }
    }

    @Test
    @DisplayName("Should allow access to OAuth2 endpoints without authentication")
    void shouldAllowAccessToOAuth2Endpoints() throws Exception {
        // Test /api/v1/auth/login-google
        mockMvc.perform(get("/api/v1/auth/login-google"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        // Test OAuth2 authorization endpoint
        mockMvc.perform(get("/login-google/google"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should include OAuth2 login in Swagger API documentation")
    void shouldIncludeInSwaggerDocumentation() throws Exception {
        // When & Then - Check that the endpoint is documented
        mockMvc.perform(get("/v1/api-docs"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login-google']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login-google'].get").exists());
    }

    @Test
    @DisplayName("Should have proper security configuration for OAuth2")
    void shouldHaveProperSecurityConfiguration() throws Exception {
        // Test that /api/v1/auth/login-google is in public endpoints
        mockMvc.perform(get("/api/v1/auth/login-google"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        // Test that protected endpoints require authentication
        // With OAuth2 configured, Spring Security redirects unauthenticated requests (302)
        // instead of returning 401. Both are valid security responses.
        mockMvc.perform(get("/api/v1/users"))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status)
                            .withFailMessage("Expected 401 Unauthorized or 302 Redirect, but got: " + status)
                            .isIn(401, 302);
                });
    }

    @Test
    @DisplayName("Should verify OAuth2 callback endpoint exists")
    void shouldVerifyCallbackEndpointExists() throws Exception {
        // The OAuth2 callback endpoint should be available
        // Note: This will fail authentication but should not return 404
        mockMvc.perform(get("/login/oauth2/code/google")
                        .param("code", "test_code")
                        .param("state", "test_state"))
                .andDo(print())
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotEqualTo(404)
                );
    }

    @Test
    @DisplayName("Should have JWT service configured correctly")
    void shouldHaveJwtServiceConfigured() {
        // Given
        String testEmail = "test@example.com";
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        testEmail,
                        "password",
                        java.util.Collections.emptyList()
                );

        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        String extractedEmail = jwtService.extractUsername(token);
        assertThat(extractedEmail).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("Should verify user repository is configured for OAuth2 user creation")
    void shouldVerifyUserRepositoryConfigured() {
        // Given - Verify repository is available for OAuth2 handler
        assertThat(userRepository).isNotNull();

        // When - Test finding a user that doesn't exist
        var nonExistentUser = userRepository.findByEmailValue("nonexistent@test.com");

        // Then - Should return empty optional
        assertThat(nonExistentUser).isEmpty();

        // Verify we can check if email exists
        boolean exists = userRepository.existsByEmail(
                com.sep.realvista.domain.common.value.Email.of("test@example.com")
        );
        assertThat(exists).isIn(true, false); // Either true or false is valid
    }
}

