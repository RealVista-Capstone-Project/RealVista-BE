package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.listing.dto.TenantRentalProfileDto;
import com.sep.realvista.application.service.engagement.TenantRentalProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenant-rental-profiles")
@RequiredArgsConstructor
@Tag(name = "Tenant Rental Profiles", description = "APIs for managing tenant rental profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class TenantRentalProfileController {

    private final TenantRentalProfileService tenantRentalProfileService;

    @GetMapping
    @Operation(summary = "Get my rental profiles", 
               description = "Retrieve all active rental profiles for the current user")
    public ResponseEntity<ApiResponse<List<TenantRentalProfileDto>>> getMyProfiles(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                tenantRentalProfileService.getMyProfiles(userDetails.getUserId())
        ));
    }

    @PostMapping
    @Operation(summary = "Create rental profile", description = "Create a new rental profile for the current user")
    public ResponseEntity<ApiResponse<TenantRentalProfileDto>> createProfile(
            @RequestBody TenantRentalProfileDto dto,
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                tenantRentalProfileService.createProfile(dto, userDetails.getUserId())
        ));
    }
}
