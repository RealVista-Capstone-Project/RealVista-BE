package com.sep.realvista.presentation.rest.auth;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.GoogleIdTokenRequest;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.service.AuthService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * REST Controller for Authentication operations.
 * <p>
 * This controller is a thin layer that handles HTTP concerns only.
 * All business logic is delegated to the AuthService.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and registration")
@Slf4j
public class AuthenticationController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody CreateUserRequest request
    ) {
        log.info("Registration request received for email: {}", request.getEmail());

        UserResponse user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request received for email: {}", request.getEmail());

        AuthenticationResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/login-google")
    @Operation(
            summary = "Login with Google (Web)",
            description = "Initiates Google OAuth2 authentication flow for web browsers. "
                    + "Open this URL directly in your browser (not via Swagger's Execute button). "
                    + "Redirects to Google sign-in and then back to the frontend with JWT token. "
                    + "This endpoint only works for web browsers. "
                    + "Mobile apps should use POST /api/v1/auth/login-google-mobile instead."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302",
                    description = "Redirect to Google OAuth2 authorization page",
                    content = @Content(
                            mediaType = "text/html",
                            examples = @ExampleObject(
                                    value = "Redirecting to Google OAuth2..."
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during OAuth2 initialization"
            )
    })
    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        log.info("Initiating Google OAuth2 login via /api/v1/auth/login-google");
        response.sendRedirect("/api/v1/auth/login-google/google");
    }

    @PostMapping("/login-google-mobile")
    @Operation(
            summary = "Login with Google (Mobile)",
            description = "Authenticates mobile users using Google ID token. "
                    + "Mobile apps should: "
                    + "1. Integrate Google Sign-In SDK "
                    + "2. Obtain ID token from Google "
                    + "3. Send the ID token to this endpoint "
                    + "4. Receive JWT token in the response. "
                    + "Platform must be either 'android' or 'ios'. "
                    + "This approach avoids the private IP redirect issue with OAuth2 on mobile."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated with Google",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired ID token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during authentication"
            )
    })
    public ResponseEntity<ApiResponse<AuthenticationResponse>> loginWithGoogleMobile(
            @Valid @RequestBody GoogleIdTokenRequest request
    ) {
        log.info("Mobile Google login request received");

        AuthenticationResponse response = authService.loginWithGoogleMobile(request);

        return ResponseEntity.ok(
                ApiResponse.success("Google authentication successful", response)
        );
    }
}

