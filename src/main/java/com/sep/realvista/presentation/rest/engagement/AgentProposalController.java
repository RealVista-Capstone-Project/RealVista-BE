package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.AgentProposalDto;
import com.sep.realvista.application.engagement.dto.ApplyAgentProposalRequest;
import com.sep.realvista.application.engagement.service.AgentProposalApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent-proposals")
@RequiredArgsConstructor
@Tag(name = "Agent Proposal Management", 
     description = "Endpoints for agents to create and manage proposal templates (CV style)")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class AgentProposalController {

    private final AgentProposalApplicationService agentProposalApplicationService;

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Create a new proposal template",
               description = "Submit a proposal template. Set status to DRAFT for a partial save; omit or ACTIVE to publish.")
    public ResponseEntity<ApiResponse<AgentProposalDto>> createProposal(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @Valid @RequestBody ApplyAgentProposalRequest request) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("Create proposal template request - traceId: {}, userId: {}", traceId, userDetails.getUserId());

        AgentProposalDto response = agentProposalApplicationService.createProposal(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Proposal template created successfully", response));
    }

    @GetMapping("/my-proposals")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Get my proposal templates", 
               description = "Get a paginated list of proposal templates created by the logged-in agent")
    public ResponseEntity<ApiResponse<PageResponse<AgentProposalDto>>> getMyProposals(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("Get my proposal templates request - traceId: {}, userId: {}, page: {}, size: {}", 
                traceId, userDetails.getUserId(), page, size);

        PageResponse<AgentProposalDto> response = 
                agentProposalApplicationService.getMyProposals(userDetails.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Proposal templates retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Archive a proposal template", description = "Archive a previously created proposal template")
    public ResponseEntity<ApiResponse<Void>> archiveProposal(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID id) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("Archive proposal template request - traceId: {}, proposalId: {}, userId: {}", 
                traceId, id, userDetails.getUserId());

        agentProposalApplicationService.archiveProposal(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Proposal template archived successfully", null));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Update a proposal template",
               description = "Replace proposal fields. Use status DRAFT or ACTIVE to save as draft or publish.")
    public ResponseEntity<ApiResponse<AgentProposalDto>> updateProposal(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody ApplyAgentProposalRequest request) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("Update proposal template request - traceId: {}, proposalId: {}, userId: {}", 
                traceId, id, userDetails.getUserId());

        AgentProposalDto response = agentProposalApplicationService
                .updateProposal(id, userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Proposal template updated successfully", response));
    }
}
