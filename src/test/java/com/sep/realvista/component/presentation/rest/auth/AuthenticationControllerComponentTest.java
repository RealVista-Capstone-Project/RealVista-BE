package com.sep.realvista.component.presentation.rest.auth;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.service.AuthService;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.infrastructure.config.security.JwtAuthenticationFilter;
import com.sep.realvista.infrastructure.config.security.JwtService;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.auth.AuthenticationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for AuthenticationController.
 * 
 * Tests the web layer (controller) with Spring MVC infrastructure while mocking
 * the business layer (AuthService).
 * 
 * These are component tests because they:
 * - Use Spring Test Context (@WebMvcTest)
 * - Test HTTP request/response handling
 * - Test JSON serialization/deserialization
 * - Test validation annotations
 * - Use MockMvc for integration with Spring MVC
 */
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters for testing
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthenticationController Component Tests (Web Layer)")
class AuthenticationControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock the application service layer
    @MockitoBean
    private AuthService authService;

    // Mock security components - required by SecurityConfig
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UserResponse mockUserResponse;
    private AuthenticationResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
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
}

