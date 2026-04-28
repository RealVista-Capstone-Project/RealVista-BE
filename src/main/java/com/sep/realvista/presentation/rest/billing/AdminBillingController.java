package com.sep.realvista.presentation.rest.billing;

import com.sep.realvista.application.billing.AdminBillingApplicationService;
import com.sep.realvista.application.billing.dto.admin.BoostPackageAdminResponse;
import com.sep.realvista.application.billing.dto.admin.CreateBoostPackageRequest;
import com.sep.realvista.application.billing.dto.admin.CreateFeaturePackageRequest;
import com.sep.realvista.application.billing.dto.admin.FeaturePackageAdminResponse;
import com.sep.realvista.application.billing.dto.admin.PackageSnapshotResponse;
import com.sep.realvista.application.billing.dto.admin.UpdateBoostPackageRequest;
import com.sep.realvista.application.billing.dto.admin.UpdateFeaturePackageRequest;
import com.sep.realvista.application.common.dto.ApiResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Admin endpoints for managing FeaturePackages and BoostPackages.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/admin/billing")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Billing", description = "Admin CRUD for FeaturePackages and BoostPackages with snapshot/audit trail")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminBillingController {

    private final AdminBillingApplicationService adminBillingService;
    private final ControllerUtils controllerUtils;

    // =========================================================================
    // FEATURE PACKAGES
    // =========================================================================

    @PostMapping("/feature-packages")
    @Operation(summary = "Create a new FeaturePackage")
    public ResponseEntity<ApiResponse<FeaturePackageAdminResponse>> createFeaturePackage(
            @Valid @RequestBody CreateFeaturePackageRequest request) {

        FeaturePackageAdminResponse response = adminBillingService.createFeaturePackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/feature-packages")
    @Operation(summary = "List all FeaturePackages (active + optionally inactive)")
    public ResponseEntity<ApiResponse<List<FeaturePackageAdminResponse>>> getAllFeaturePackages(
            @RequestParam(defaultValue = "true") boolean includeInactive) {

        List<FeaturePackageAdminResponse> list = adminBillingService.getAllFeaturePackages(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/feature-packages/{id}")
    @Operation(summary = "Get a FeaturePackage by ID")
    public ResponseEntity<ApiResponse<FeaturePackageAdminResponse>> getFeaturePackageById(
            @PathVariable UUID id) {

        FeaturePackageAdminResponse response = adminBillingService.getFeaturePackageById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/feature-packages/{id}")
    @Operation(summary = "Update a FeaturePackage (snapshot taken before update)")
    public ResponseEntity<ApiResponse<FeaturePackageAdminResponse>> updateFeaturePackage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFeaturePackageRequest request,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        FeaturePackageAdminResponse response = adminBillingService.updateFeaturePackage(
                id, request, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/feature-packages/{id}/activate")
    @Operation(summary = "Activate a FeaturePackage")
    public ResponseEntity<ApiResponse<FeaturePackageAdminResponse>> activateFeaturePackage(
            @PathVariable UUID id,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        FeaturePackageAdminResponse response = adminBillingService.activateFeaturePackage(id, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/feature-packages/{id}/deactivate")
    @Operation(summary = "Deactivate a FeaturePackage (snapshot taken; existing subscriptions run to completion)")
    public ResponseEntity<ApiResponse<FeaturePackageAdminResponse>> deactivateFeaturePackage(
            @PathVariable UUID id,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        FeaturePackageAdminResponse response = adminBillingService.deactivateFeaturePackage(id, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/feature-packages/{id}")
    @Operation(summary = "Soft-delete a FeaturePackage (blocked if active subscriptions exist)")
    public ResponseEntity<ApiResponse<Void>> deleteFeaturePackage(
            @PathVariable UUID id,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        adminBillingService.deleteFeaturePackage(id, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/feature-packages/{id}/history")
    @Operation(summary = "Get snapshot/audit history for a FeaturePackage")
    public ResponseEntity<ApiResponse<List<PackageSnapshotResponse>>> getFeaturePackageHistory(
            @PathVariable UUID id) {

        List<PackageSnapshotResponse> history = adminBillingService.getFeaturePackageHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    // =========================================================================
    // BOOST PACKAGES
    // =========================================================================

    @PostMapping("/boost-packages")
    @Operation(summary = "Create a new BoostPackage")
    public ResponseEntity<ApiResponse<BoostPackageAdminResponse>> createBoostPackage(
            @Valid @RequestBody CreateBoostPackageRequest request) {

        BoostPackageAdminResponse response = adminBillingService.createBoostPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/boost-packages")
    @Operation(summary = "List all BoostPackages (active + optionally inactive)")
    public ResponseEntity<ApiResponse<List<BoostPackageAdminResponse>>> getAllBoostPackages(
            @RequestParam(defaultValue = "true") boolean includeInactive) {

        List<BoostPackageAdminResponse> list = adminBillingService.getAllBoostPackages(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/boost-packages/{id}")
    @Operation(summary = "Get a BoostPackage by ID")
    public ResponseEntity<ApiResponse<BoostPackageAdminResponse>> getBoostPackageById(
            @PathVariable UUID id) {

        BoostPackageAdminResponse response = adminBillingService.getBoostPackageById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/boost-packages/{id}")
    @Operation(summary = "Update a BoostPackage (snapshot taken before update)")
    public ResponseEntity<ApiResponse<BoostPackageAdminResponse>> updateBoostPackage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBoostPackageRequest request,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        BoostPackageAdminResponse response = adminBillingService.updateBoostPackage(
                id, request, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/boost-packages/{id}/activate")
    @Operation(summary = "Activate a BoostPackage")
    public ResponseEntity<ApiResponse<BoostPackageAdminResponse>> activateBoostPackage(
            @PathVariable UUID id,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        BoostPackageAdminResponse response = adminBillingService.activateBoostPackage(id, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/boost-packages/{id}/deactivate")
    @Operation(summary = "Deactivate a BoostPackage (snapshot taken; existing purchases run to completion)")
    public ResponseEntity<ApiResponse<BoostPackageAdminResponse>> deactivateBoostPackage(
            @PathVariable UUID id,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        BoostPackageAdminResponse response = adminBillingService.deactivateBoostPackage(id, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/boost-packages/{id}")
    @Operation(summary = "Soft-delete a BoostPackage (blocked if active purchases or boosts exist)")
    public ResponseEntity<ApiResponse<Void>> deleteBoostPackage(
            @PathVariable UUID id,
            Authentication authentication) {

        User admin = controllerUtils.getCurrentUser(authentication);
        adminBillingService.deleteBoostPackage(id, admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/boost-packages/{id}/history")
    @Operation(summary = "Get snapshot/audit history for a BoostPackage")
    public ResponseEntity<ApiResponse<List<PackageSnapshotResponse>>> getBoostPackageHistory(
            @PathVariable UUID id) {

        List<PackageSnapshotResponse> history = adminBillingService.getBoostPackageHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
