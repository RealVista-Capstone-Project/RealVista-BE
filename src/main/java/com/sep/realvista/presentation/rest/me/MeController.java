package com.sep.realvista.presentation.rest.me;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.user.dto.UpdateMeRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.application.user.dto.SendEmailOtpRequest;
import com.sep.realvista.application.user.dto.VerifyEmailRequest;
import com.sep.realvista.application.user.dto.VerifyPhoneRequest;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.presentation.common.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "Me", description = "Endpoints for current authenticated user")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class MeController {

    private final UserApplicationService userApplicationService;
    private final ControllerUtils controllerUtils;

    @GetMapping
    @Operation(summary = "Get current user", description = "Returns the current authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        UserResponse response = userApplicationService.getUserById(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping
    @Operation(summary = "Update current user", description = "Updates the current authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            Authentication authentication,
            @Valid @RequestBody UpdateMeRequest request
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        UserResponse response = userApplicationService.updateMe(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PostMapping("/send-email-otp")
    @Operation(
            summary = "Send email OTP",
            description = "Sends a 6-digit OTP to the provided email. The user's stored email is "
                    + "NOT changed here — the target address is only committed after a successful "
                    + "call to /me/verify-email with the matching OTP."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendEmailOtp(
            Authentication authentication,
            @Valid @RequestBody SendEmailOtpRequest request
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        userApplicationService.sendEmailOtp(currentUser.getUserId(), request.getEmail());
        long remaining = userApplicationService.emailOtpRemainingSeconds(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("OTP sent", Map.of(
                "expirySeconds", remaining > 0 ? remaining : 300L
        )));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP", description = "Verifies email address using the OTP sent by email")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(
            Authentication authentication,
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        UserResponse response = userApplicationService.verifyEmail(currentUser.getUserId(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", response));
    }

    @PostMapping("/verify-phone")
    @Operation(summary = "Verify phone", description = "Marks the current user's phone number as verified")
    public ResponseEntity<ApiResponse<UserResponse>> verifyPhone(
            Authentication authentication,
            @Valid @RequestBody VerifyPhoneRequest request
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        UserResponse response = userApplicationService.verifyPhone(currentUser.getUserId(), request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("Phone verified successfully", response));
    }

    @PostMapping("/add-role")
    @Operation(
            summary = "Add OWNER role",
            description = "Adds the OWNER role to the current user if not already assigned"
    )
    public ResponseEntity<ApiResponse<UserResponse>> addOwnerRole(Authentication authentication) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        UserResponse response = userApplicationService.addOwnerRole(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Owner role added successfully", response));
    }
}
