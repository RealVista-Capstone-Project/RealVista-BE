package com.sep.realvista.presentation.rest.me;

import com.sep.realvista.application.agent.dto.AgentProfileResponse;
import com.sep.realvista.application.agent.dto.UpdateAgentProfileRequest;
import com.sep.realvista.application.agent.service.AgentProfileApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.presentation.common.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/agent-profile")
@RequiredArgsConstructor
@Tag(name = "Me — Agent profile", description = "Current user's agent professional profile")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class AgentProfileController {

    private final AgentProfileApplicationService agentProfileApplicationService;
    private final ControllerUtils controllerUtils;

    @GetMapping
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Get my agent profile", description = "Returns the agent profile for the authenticated user")
    public ResponseEntity<ApiResponse<AgentProfileResponse>> getMine(Authentication authentication) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        AgentProfileResponse response = agentProfileApplicationService.getMine(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Update my agent profile",
            description = "Updates bio, specialties, service areas, and years of experience")
    public ResponseEntity<ApiResponse<AgentProfileResponse>> updateMine(
            Authentication authentication,
            @Valid @RequestBody UpdateAgentProfileRequest request
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        AgentProfileResponse response = agentProfileApplicationService.updateMine(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Agent profile updated successfully", response));
    }
}
