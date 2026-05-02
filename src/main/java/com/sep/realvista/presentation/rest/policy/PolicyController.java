package com.sep.realvista.presentation.rest.policy;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.policy.PolicyService;
import com.sep.realvista.application.policy.dto.CreatePolicyCommand;
import com.sep.realvista.application.policy.dto.PolicyDto;
import com.sep.realvista.application.policy.dto.UpdatePolicyCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin/policies")
@RequiredArgsConstructor
@Tag(name = "Admin Policy", description = "Endpoints for managing policies")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all policies", description = "Retrieves a list of all policies (Admin only)")
    public ResponseEntity<ApiResponse<List<PolicyDto>>> getAllPolicies() {
        log.info("Fetching all policies for admin");
        List<PolicyDto> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create policy", description = "Creates a new policy (Admin only)")
    public ResponseEntity<ApiResponse<PolicyDto>> createPolicy(
            @Valid @RequestBody CreatePolicyCommand command
    ) {
        log.info("Creating new policy: {}", command.getTitle());
        PolicyDto created = policyService.createPolicy(command.getTitle(), command.getSlug(),
                command.getContent(), command.getIsActive());
        return ResponseEntity.ok(ApiResponse.success("Policy created successfully", created));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update policy", description = "Updates a policy (Admin only)")
    public ResponseEntity<ApiResponse<PolicyDto>> updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePolicyCommand command
    ) {
        log.info("Updating policy id: {}", id);
        PolicyDto updated = policyService.updatePolicy(id, command.getTitle(),
                command.getContent(), command.getIsActive());
        return ResponseEntity.ok(ApiResponse.success("Policy updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete policy", description = "Deletes a policy (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable UUID id) {
        log.info("Deleting policy id: {}", id);
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Policy deleted successfully", null));
    }


    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle policy status", description = "Toggles policy active/inactive status (Admin only)")
    public ResponseEntity<ApiResponse<PolicyDto>> togglePolicyStatus(@PathVariable UUID id) {
        log.info("Toggling status for policy id: {}", id);
        PolicyDto updated = policyService.togglePolicyStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Policy status toggled successfully", updated));
    }
}


