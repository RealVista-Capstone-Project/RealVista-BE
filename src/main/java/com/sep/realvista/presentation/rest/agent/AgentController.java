package com.sep.realvista.presentation.rest.agent;

import com.sep.realvista.application.agent.dto.AgentListItemResponse;
import com.sep.realvista.application.agent.service.AgentProfileApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.engagement.dto.ReviewResponse;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Agents", description = "Endpoints for browsing agent profiles")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class AgentController {

    private final AgentProfileApplicationService agentProfileApplicationService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "List all agents",
            description = "Returns all agent profiles. "
                    + "If property_id is provided, each item includes the engagement "
                    + "status that agent has with that property (if any). "
                    + "Supports search by name (LIKE) and min_rating filter.")
    public ResponseEntity<ApiResponse<List<AgentListItemResponse>>> listAgents(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(name = "property_id", required = false) UUID propertyId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "min_rating", required = false) BigDecimal minRating
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("List agents request - ownerId: {}, propertyId: {}, search: {}, minRating: {}",
                ownerId, propertyId, search, minRating);
        List<AgentListItemResponse> response =
                agentProfileApplicationService.listAgentsForProperty(propertyId, ownerId, search, minRating);
        return ResponseEntity.ok(ApiResponse.success("Agents retrieved successfully", response));
    }

    @GetMapping("/{agentId}/reviews")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Get agent reviews",
            description = "Returns all reviews for the specified agent, newest first.")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAgentReviews(
            @PathVariable UUID agentId
    ) {
        log.info("Get agent reviews request - agentId: {}", agentId);
        List<ReviewResponse> reviews = agentProfileApplicationService.getReviewsForAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }
}
