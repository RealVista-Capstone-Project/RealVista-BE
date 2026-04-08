package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.CancelEngagementRequest;
import com.sep.realvista.application.engagement.dto.CreateReviewRequest;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.dto.ReviewResponse;
import com.sep.realvista.application.engagement.dto.SubmitAgentProposalRequest;
import com.sep.realvista.application.engagement.service.AgentReviewApplicationService;
import com.sep.realvista.application.engagement.service.EngagementApplicationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for managing engagements between property owners and agents.
 *
 * Provides endpoints for viewing hired agents, finishing/cancelling contracts,
 * and submitting agent reviews.
 */
@RestController
@RequestMapping("/api/v1/engagements")
@RequiredArgsConstructor
@Tag(name = "Engagement Management", description = "Endpoints for managing engagements between owners and agents")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class EngagementController {

    private final EngagementApplicationService engagementApplicationService;
    private final AgentReviewApplicationService agentReviewApplicationService;

    /**
     * Get a single engagement by ID, scoped to the authenticated owner.
     *
     * Returns full agent and property details for the engagement.
     * The caller must be the owner of the engagement (403 otherwise).
     *
     * @param userDetails the authenticated user
     * @param id          the engagement ID
     * @return full engagement detail
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Get engagement by ID",
            description = "Retrieves full detail for a single engagement by ID, including agent profile, "
                    + "property information, engagement metadata, and review status. "
                    + "Only the owner of the engagement can access this endpoint."
    )
    public ResponseEntity<ApiResponse<HiredAgentResponse>> getEngagementById(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID id
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        UUID ownerId = userDetails.getUserId();

        log.info("Get engagement detail request - traceId: {}, engagementId: {}, ownerId: {}",
                traceId, id, ownerId);

        HiredAgentResponse response = engagementApplicationService.getEngagementById(id, ownerId);

        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }

    /**
     * Get all hired agents for the authenticated property owner.
     *
     * Returns a paginated list of agents that have been hired through
     * engagements. Supports optional status filter and search by agent name.
     *
     * @param userDetails the authenticated user
     * @param page        page number (0-indexed)
     * @param size        page size
     * @param status      optional status filter (ACCEPTED, FINISHED, CANCELLED)
     * @param search      optional search query for agent name
     * @return paginated list of hired agents
     */
    @GetMapping("/hired-agents")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Get hired agents",
            description = "Retrieves all agents hired by the authenticated property owner. "
                    + "Includes agent profile information, property details, engagement metadata, "
                    + "and review status. Supports filtering by status and searching by agent name. "
                    + "Results are sorted by most recently updated first."
    )
    public ResponseEntity<ApiResponse<PageResponse<HiredAgentResponse>>> getHiredAgents(
            @AuthenticationPrincipal SecurityUserDetails userDetails,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Filter by engagement status (ACCEPTED, FINISHED, CANCELLED)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Search by agent name")
            @RequestParam(required = false) String search
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        UUID ownerId = userDetails.getUserId();

        log.info("Get hired agents request - traceId: {}, ownerId: {}, status: {}, search: {}, page: {}, size: {}",
                traceId, ownerId, status, search, page, size);

        PageResponse<HiredAgentResponse> response = engagementApplicationService
                .getHiredAgents(ownerId, status, search, page, size);

        return ResponseEntity.ok(ApiResponse.success("Hired agents retrieved successfully", response));
    }

    /**
     * Finish an engagement (mark contract as completed).
     * Only ACCEPTED engagements can be finished.
     *
     * @param userDetails the authenticated user
     * @param id          the engagement ID
     * @return success response
     */
    @PutMapping("/{id}/finish")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Finish engagement",
            description = "Marks an accepted engagement as finished (contract completed). "
                    + "Only the owner of the engagement can perform this action. "
                    + "Only engagements with ACCEPTED status can be finished."
    )
    public ResponseEntity<ApiResponse<Void>> finishEngagement(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID id
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        UUID ownerId = userDetails.getUserId();

        log.info("Finish engagement request - traceId: {}, engagementId: {}, ownerId: {}",
                traceId, id, ownerId);

        engagementApplicationService.finishEngagement(id, ownerId);

        return ResponseEntity.ok(ApiResponse.success("Engagement finished successfully", null));
    }

    /**
     * Cancel an engagement.
     * ACCEPTED engagements require a cancellation reason.
     * SUBMITTED engagements can be cancelled without a reason.
     *
     * @param userDetails the authenticated user
     * @param id          the engagement ID
     * @param request     the cancellation request (reason)
     * @return success response
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Cancel engagement",
            description = "Cancels an engagement. ACCEPTED engagements require a cancellation reason. "
                    + "SUBMITTED engagements can be cancelled without a reason. "
                    + "Only the owner of the engagement can perform this action."
    )
    public ResponseEntity<ApiResponse<Void>> cancelEngagement(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelEngagementRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        UUID ownerId = userDetails.getUserId();

        log.info("Cancel engagement request - traceId: {}, engagementId: {}, ownerId: {}",
                traceId, id, ownerId);

        engagementApplicationService.cancelEngagement(id, ownerId, request);

        return ResponseEntity.ok(ApiResponse.success("Engagement cancelled successfully", null));
    }

    /**
     * Submit a review for an agent after engagement completion.
     * Only FINISHED or CANCELLED engagements can be reviewed.
     * Only one review per engagement is allowed.
     *
     * @param userDetails the authenticated user
     * @param id          the engagement ID
     * @param request     the review details (rating, optional comment)
     * @return the created review
     */
    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Submit agent review",
            description = "Submits a review and rating for an agent after an engagement is finished or cancelled. "
                    + "Rating is required (1.0-5.0). Review comment is optional. "
                    + "Only one review per engagement is allowed. "
                    + "The agent's average rating is automatically recalculated."
    )
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        UUID ownerId = userDetails.getUserId();

        log.info("Submit review request - traceId: {}, engagementId: {}, ownerId: {}, rating: {}",
                traceId, id, ownerId, request.getRating());

        ReviewResponse response = agentReviewApplicationService.submitReview(id, ownerId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", response));
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
