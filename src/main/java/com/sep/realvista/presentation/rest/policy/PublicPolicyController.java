package com.sep.realvista.presentation.rest.policy;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.policy.PolicyService;
import com.sep.realvista.application.policy.dto.PolicyDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/policies")
@RequiredArgsConstructor
@Tag(name = "Public Policy", description = "Endpoints for viewing policies")
@Slf4j
public class PublicPolicyController {

    private final PolicyService policyService;

    @GetMapping
    @Operation(summary = "Get all policies", description = "Retrieves a list of all active policies")
    public ResponseEntity<ApiResponse<List<PolicyDto>>> getAllPolicies() {
        log.info("Fetching all active policies");
        return ResponseEntity.ok(ApiResponse.success(policyService.getAllActivePolicies()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get policy by slug", description = "Retrieves active policy content by slug")
    public ResponseEntity<ApiResponse<PolicyDto>> getPolicyBySlug(@PathVariable String slug) {
        log.info("Fetching active policy by slug: {}", slug);
        return ResponseEntity.ok(ApiResponse.success(policyService.getActivePolicyBySlug(slug)));
    }
}
