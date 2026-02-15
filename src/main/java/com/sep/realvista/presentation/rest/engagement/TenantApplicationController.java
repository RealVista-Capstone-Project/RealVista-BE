package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.listing.dto.TenantApplicationDto;
import com.sep.realvista.application.service.engagement.TenantApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenant-applications")
@RequiredArgsConstructor
@Tag(name = "Tenant Applications", description = "APIs for managing tenant applications")
@SecurityRequirement(name = "bearerAuth")
public class TenantApplicationController {

    private final TenantApplicationService tenantApplicationService;

    @GetMapping
    @Operation(summary = "Get my applications", description = "Retrieve all active applications for the current user")
    public ResponseEntity<ApiResponse<List<TenantApplicationDto>>> getMyApplications(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                tenantApplicationService.getMyApplications(userDetails.getUserId())
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete application", description = "Soft delete (archive) a tenant application")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        tenantApplicationService.softDeleteApplication(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
