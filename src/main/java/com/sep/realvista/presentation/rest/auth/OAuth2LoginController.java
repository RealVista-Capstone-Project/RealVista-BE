package com.sep.realvista.presentation.rest.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * OAuth2 Login Controller.
 * Provides convenience endpoints for OAuth2 authentication.
 */
@RestController
@Tag(name = "OAuth2 Authentication", description = "OAuth2 login endpoints for third-party authentication")
@Slf4j
public class OAuth2LoginController {

    /**
     * Redirect to Google OAuth2 login.
     * This endpoint initiates the OAuth2 authorization flow with Google.
     * <p>
     * After successful authentication, the user will be redirected to the frontend
     * with a JWT token and user information.
     */
    @GetMapping("/login-google")
    @Operation(
            summary = "Login with Google",
            description = """
                    Initiates Google OAuth2 authentication flow.
                    
                    **Flow:**
                    1. Click 'Try it out' and 'Execute' to test
                    2. You will be redirected to Google's consent screen
                    3. After authentication, you'll be redirected to the frontend with:
                       - JWT access token
                       - User ID
                       - Email
                    
                    **Redirect URL:** `{frontend_url}/auth/callback?access_token={jwt}&user_id={id}&email={email}`
                    
                    **Note:** If this is your first login, a new user account will be automatically created.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirect to Google OAuth2 authorization page",
                    content = @Content(
                            mediaType = "text/html",
                            examples = @ExampleObject(
                                    value = "Redirecting to Google OAuth2..."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during OAuth2 initialization"
            )
    })
    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        log.info("Initiating Google OAuth2 login");
        response.sendRedirect("/login-google/google");
    }
}

