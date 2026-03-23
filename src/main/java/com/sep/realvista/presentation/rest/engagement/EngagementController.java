package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.engagement.dto.EngagementDto;
import com.sep.realvista.application.service.engagement.EngagementService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/engagements")
@RequiredArgsConstructor
@Tag(name = "Engagements", description = "APIs for managing tenant engagements")
@SecurityRequirement(name = "Bearer Authentication")
public class EngagementController {

    private final EngagementService engagementService;

    @GetMapping
    @Operation(summary = "Get my engagements", description = "Retrieve all engagements initiated by the current user")
    public ResponseEntity<ApiResponse<List<EngagementDto>>> getMyEngagements(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
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
        return ResponseEntity.ok(ApiResponse.success(
                engagementService.cancelEngagement(id, userDetails.getUserId())
        ));
    }
}
