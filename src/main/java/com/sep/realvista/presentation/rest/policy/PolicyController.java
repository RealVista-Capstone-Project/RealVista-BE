package com.sep.realvista.presentation.rest.policy;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.policy.PolicyService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/policies")
@RequiredArgsConstructor
@Tag(name = "Admin Policy", description = "Endpoints for managing policies")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class PolicyController {

    private final PolicyService policyService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update policy", description = "Updates a policy (Admin only)")
    public ResponseEntity<ApiResponse<PolicyDto>> updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePolicyCommand command
    ) {
        log.info("Updating policy id: {}", id);
        PolicyDto updated = policyService.updatePolicy(id, command.getTitle(), command.getContent());
        return ResponseEntity.ok(ApiResponse.success("Policy updated successfully", updated));
    }
}
