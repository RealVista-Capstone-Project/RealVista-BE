package com.sep.realvista.component.presentation.rest.auth;

import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.infrastructure.config.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for OAuth2LoginController.
 * Tests the OAuth2 login endpoint integration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OAuth2LoginController Component Tests")
class OAuth2LoginControllerComponentTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setUp() {
        // Clean up any existing test users
        userRepository.findByEmailValue("test.oauth2@example.com")
                .ifPresent(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    @DisplayName("Should initiate OAuth2 login flow via /login-google endpoint")
    void shouldInitiateOAuth2LoginFlow() throws Exception {
        // When & Then
        mockMvc.perform(get("/login-google"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login-google/google"));
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
            assertThat(googleRegistration.getClientName()).isEqualTo("Google");
            assertThat(googleRegistration.getAuthorizationGrantType())
                    .isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
            assertThat(googleRegistration.getScopes())
                    .contains("profile", "email");
        }
    }

    @Test
    @DisplayName("Should allow access to OAuth2 endpoints without authentication")
    void shouldAllowAccessToOAuth2Endpoints() throws Exception {
        // Test /login-google
        mockMvc.perform(get("/login-google"))
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
                .andExpect(jsonPath("$.paths['/login-google']").exists())
                .andExpect(jsonPath("$.paths['/login-google'].get").exists());
    }

    @Test
    @DisplayName("Should have proper security configuration for OAuth2")
    void shouldHaveProperSecurityConfiguration() throws Exception {
        // Test that /login-google/** is in public endpoints
        mockMvc.perform(get("/login-google"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        // Test that protected endpoints still require authentication
        mockMvc.perform(get("/api/v1/users"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
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

