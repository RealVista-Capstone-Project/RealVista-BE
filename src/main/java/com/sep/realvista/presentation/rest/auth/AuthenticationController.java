package com.sep.realvista.presentation.rest.auth;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.application.auth.dto.LoginRequest;
import com.sep.realvista.application.auth.service.AuthService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Authentication operations.
 * 
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
}

