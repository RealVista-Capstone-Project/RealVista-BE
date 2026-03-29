package com.sep.realvista.presentation.rest.notification;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.notification.dto.RegisterDeviceTokenRequest;
import com.sep.realvista.application.notification.service.DeviceTokenApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-tokens")
@RequiredArgsConstructor
@Tag(name = "Device Tokens", description = "Endpoints for managing FCM device tokens for push notifications")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class DeviceTokenController {

    private final DeviceTokenApplicationService deviceTokenApplicationService;

    @PostMapping
    @Operation(summary = "Register device token",
            description = "Registers or updates an FCM device token for the current user. "
                    + "Call this on app startup or when the FCM token refreshes.")
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(
            @Valid @RequestBody RegisterDeviceTokenRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        log.info("Registering device token for user: {}, deviceType: {}",
                currentUser.getUserId(), request.getDeviceType());
        deviceTokenApplicationService.registerDeviceToken(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Device token registered successfully", null));
    }

    @DeleteMapping
    @Operation(summary = "Unregister device token",
            description = "Deactivates an FCM device token. Call this on user logout.")
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @RequestParam("fcm_token") String fcmToken,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        log.info("Unregistering device token for user: {}", currentUser.getUserId());
        deviceTokenApplicationService.unregisterDeviceToken(currentUser.getUserId(), fcmToken);
        return ResponseEntity.ok(ApiResponse.success("Device token unregistered successfully", null));
    }
}
