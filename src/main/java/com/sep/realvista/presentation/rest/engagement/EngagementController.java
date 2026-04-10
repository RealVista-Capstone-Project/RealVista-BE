package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.CancelEngagementRequest;
import com.sep.realvista.application.engagement.dto.CreateReviewRequest;
import com.sep.realvista.application.engagement.dto.EngagementDto;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.dto.ReviewResponse;
import com.sep.realvista.application.engagement.dto.SubmitAgentProposalRequest;
import com.sep.realvista.application.engagement.service.AgentReviewApplicationService;
import com.sep.realvista.application.engagement.service.EngagementApplicationService;
import com.sep.realvista.application.service.engagement.EngagementService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/engagements")
@RequiredArgsConstructor
@Tag(name = "Engagements", description = "APIs for managing tenant engagements")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class EngagementController {

    private final EngagementService engagementService;
    private final EngagementApplicationService engagementApplicationService;
    private final AgentReviewApplicationService agentReviewApplicationService;

    @GetMapping
    @Operation(summary = "Get my engagements", description = "Retrieve all engagements initiated by the current user")
    public ResponseEntity<ApiResponse<List<EngagementDto>>> getMyEngagements(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        return ResponseEntity.ok(ApiResponse.success(
                engagementService.getMyEngagements(userDetails.getUserId())
        ));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel engagement",
            description = "Cancel a SUBMITTED engagement. Only the initiator can cancel.")
    public ResponseEntity<ApiResponse<EngagementDto>> cancelEngagement(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        return ResponseEntity.ok(ApiResponse.success(
                engagementService.cancelEngagement(id, userDetails.getUserId())
        ));
    }

    /**
     * Submit an agent proposal for a specific property.
     * Initiates a new engagement by an agent.
     *
     * @param userDetails the authenticated agent
     * @param request     the proposal template ID and property ID
     * @return the created engagement ID
     */
    @PostMapping("/agent-proposal")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(
            summary = "Submit agent proposal",
            description = "Submits a proposal for a property as an agent. "
                    + "Creates a new SUBMITTED engagement between the agent and property owner. "
                    + "The content is cloned from the specified proposal template."
    )
    public ResponseEntity<ApiResponse<Map<String, UUID>>> submitAgentProposal(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @Valid @RequestBody SubmitAgentProposalRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        UUID agentId = userDetails.getUserId();

        log.info("Submit agent proposal request - traceId: {}, agentId: {}, proposalId: {}, propertyId: {}",
                traceId, agentId, request.getAgentProposalId(), request.getPropertyId());

        UUID engagementId = engagementApplicationService.submitAgentProposal(agentId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Proposal submitted successfully", Map.of("engagement_id", engagementId)));
    }
}
