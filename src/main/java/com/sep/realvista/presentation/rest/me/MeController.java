package com.sep.realvista.presentation.rest.me;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.user.dto.UpdateMeRequest;
import com.sep.realvista.application.user.dto.UserResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
