package com.sep.realvista.presentation.rest.auth;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
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
            summary = "Login with Google",
            description = """
                    Initiates Google OAuth2 authentication flow.
                    
                    ⚠️ **IMPORTANT: This endpoint CANNOT be tested using Swagger's "Execute" button!**
                    
                    **Why?** Swagger UI uses AJAX/fetch which cannot handle OAuth2 redirects due to CORS restrictions.
                    
                    **How to Test:**
                    1. Open this URL directly in your browser:
                       ```
                       http://localhost:8080/api/v1/auth/login-google
                       ```
                    2. Or click this link in Swagger UI documentation
                    3. You will be redirected to Google's sign-in page
                    4. After authentication, you'll be redirected to your frontend
                    
                    **OAuth2 Flow:**
                    1. User visits: `http://localhost:8080/api/v1/auth/login-google`
                    2. Redirects to: `/api/v1/auth/login-google/google`
                    3. Spring Security redirects to: `https://accounts.google.com/o/oauth2/v2/auth`
                    4. User authenticates with Google
                    5. Google redirects back to: `http://localhost:8080/login/oauth2/code/google`
                    6. Success handler processes user and generates JWT
                    7. Final redirect to: `{frontend_url}/auth/callback?token={jwt}&userId={id}&email={email}`
                    
                    **Note:** If this is your first login, a new user account will be automatically created.
                    
                    **For Frontend Integration:**
                    ```javascript
                    // React/JavaScript
                    window.location.href = 'http://localhost:8080/api/v1/auth/login-google';
                    ```
                    
                    ```html
                    <!-- HTML Link -->
                    <a href="http://localhost:8080/api/v1/auth/login-google">Login with Google</a>
                    ```
                    """
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
}

