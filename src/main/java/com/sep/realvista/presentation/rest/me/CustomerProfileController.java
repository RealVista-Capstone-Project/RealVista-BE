package com.sep.realvista.presentation.rest.me;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.profile.dto.CreateCustomerProfileRequest;
import com.sep.realvista.application.profile.dto.CustomerProfileResponse;
import com.sep.realvista.application.profile.service.CustomerProfileApplicationService;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.presentation.common.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/profiles")
@RequiredArgsConstructor
@Tag(name = "Customer Profiles", description = "Endpoints for managing customer profiles")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class CustomerProfileController {

    private final CustomerProfileApplicationService customerProfileApplicationService;
    private final ControllerUtils controllerUtils;

    @GetMapping
    @Operation(summary = "Get all profiles", description = "Returns all customer profiles for the current user")
    public ResponseEntity<ApiResponse<List<CustomerProfileResponse>>> getAllProfiles(Authentication authentication) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        List<CustomerProfileResponse> profiles =
                customerProfileApplicationService.getAllProfiles(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @PostMapping
    @Operation(summary = "Create profile", description = "Creates a new customer profile for the current user")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> createProfile(
            Authentication authentication,
            @Valid @RequestBody CreateCustomerProfileRequest request
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        CustomerProfileResponse profile =
                customerProfileApplicationService.createProfile(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully", profile));
    }

    @DeleteMapping("/{profileId}")
    @Operation(summary = "Delete profile", description = "Deletes a customer profile (cannot delete active profile)")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            Authentication authentication,
            @PathVariable UUID profileId
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        customerProfileApplicationService.deleteProfile(profileId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Profile deleted successfully", null));
    }

    @PutMapping("/{profileId}/switch")
    @Operation(summary = "Switch active profile", description = "Switches the active customer profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> switchProfile(
            Authentication authentication,
            @PathVariable UUID profileId
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        CustomerProfileResponse profile =
                customerProfileApplicationService.switchMainProfile(profileId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Profile switched successfully", profile));
    }
}
