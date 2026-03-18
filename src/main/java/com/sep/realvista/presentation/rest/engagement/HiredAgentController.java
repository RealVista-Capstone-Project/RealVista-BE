package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.service.EngagementApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for managing hired agents.
 *
 * Provides endpoints for property owners to view agents
 * they have hired through accepted engagements.
 */
@RestController
@RequestMapping("/api/v1/engagements/hired-agents")
@RequiredArgsConstructor
@Tag(name = "Engagement Management", description = "Endpoints for managing engagements between owners and agents")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class HiredAgentController {

    private final EngagementApplicationService engagementApplicationService;

    /**
     * Get all hired agents for the authenticated property owner.
     *
     * Returns a paginated list of agents that have been hired through
     * accepted AGENT_PROPOSAL or OWNER_INVITATION engagements.
     *
     * @param userDetails the authenticated user
     * @param page        page number (0-indexed)
     * @param size        page size
     * @return paginated list of hired agents
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Get hired agents",
            description = "Retrieves all agents hired by the authenticated property owner. "
                    + "Includes agent profile information, property details, and engagement metadata. "
                    + "Results are sorted by hire date (most recent first)."
    )
    public ResponseEntity<ApiResponse<PageResponse<HiredAgentResponse>>> getHiredAgents(
            @AuthenticationPrincipal SecurityUserDetails userDetails,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        UUID ownerId = userDetails.getUserId();

        log.info("Get hired agents request - traceId: {}, ownerId: {}, page: {}, size: {}",
                traceId, ownerId, page, size);

        PageResponse<HiredAgentResponse> response = engagementApplicationService
                .getHiredAgents(ownerId, page, size);

        return ResponseEntity.ok(ApiResponse.success("Hired agents retrieved successfully", response));
    }
}
