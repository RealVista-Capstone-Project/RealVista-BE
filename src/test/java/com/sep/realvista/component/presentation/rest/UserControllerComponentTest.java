package com.sep.realvista.component.presentation.rest;

import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.user.UserRole;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.infrastructure.config.security.JwtAuthenticationFilter;
import com.sep.realvista.infrastructure.config.security.JwtService;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.user.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for UserController.
 * 
 * Tests the web layer (controller) with Spring MVC infrastructure while mocking
 * the business layer (services) and security components.
 * 
 * These are component tests (not unit tests) because they:
 * - Use Spring Test Context (@WebMvcTest)
 * - Test HTTP request/response handling
 * - Test JSON serialization/deserialization
 * - Test validation annotations
 * - Use MockMvc for integration with Spring MVC
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters for testing
@Import(GlobalExceptionHandler.class)
@DisplayName("UserController Component Tests (Web Layer)")
class UserControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock the business layer - we're testing the controller layer
    @MockitoBean
    private UserApplicationService userApplicationService;

    // Mock security components - required by SecurityConfig
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        // Arrange: Prepare test data
        mockUserResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    /**
     * Test Case: Create user with valid request
     * Expected: 201 Created with user data
     */
    @Test
    @DisplayName("Should return 201 Created when creating user with valid request")
    void createUser_withValidRequest_shouldReturnCreated() throws Exception {
        // Arrange: Mock service behavior
        when(userApplicationService.createUser(any(CreateUserRequest.class)))
                .thenReturn(mockUserResponse);

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "SecurePass123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        // Act & Assert: Perform request and verify response
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    /**
     * Test Case: Create user with invalid email format
     * Expected: 400 Bad Request with validation error
     */
    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid")
    void createUser_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange: Prepare invalid request
        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "SecurePass123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        // Act & Assert: Expect validation error
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    /**
     * Test Case: Create user with missing required fields
     * Expected: 400 Bad Request with validation errors
     */
    @Test
    @DisplayName("Should return 400 Bad Request when required fields are missing")
    void createUser_withMissingFields_shouldReturnBadRequest() throws Exception {
        // Arrange: Prepare request with missing fields
        String requestBody = """
                {
                    "email": "test@example.com"
                }
                """;

        // Act & Assert: Expect validation error
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    /**
     * Test Case: Create user with empty email
     * Expected: 400 Bad Request with validation error
     */
    @Test
    @DisplayName("Should return 400 Bad Request when email is empty")
    void createUser_withEmptyEmail_shouldReturnBadRequest() throws Exception {
        // Arrange: Prepare request with empty email
        String requestBody = """
                {
                    "email": "",
                    "password": "SecurePass123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        // Act & Assert: Expect validation error
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}

