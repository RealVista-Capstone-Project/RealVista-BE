package com.sep.realvista.presentation.rest.billing;

import com.sep.realvista.application.billing.dto.ApplyBoostRequest;
import com.sep.realvista.application.billing.dto.ListingBoostResponseDto;
import com.sep.realvista.application.billing.service.ListingBoostApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings/{listingId}/boosts")
@RequiredArgsConstructor
@Tag(name = "Listing Boosts", description = "Apply and manage boosts on listings")
public class ListingBoostController {

    private final ListingBoostApplicationService listingBoostService;

    @GetMapping
    @Operation(summary = "Get active boosts for a listing")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<ListingBoostResponseDto>>> getActiveBoosts(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        List<ListingBoostResponseDto> boosts = listingBoostService.getActiveBoostsForListing(listingId);
        return ResponseEntity.ok(ApiResponse.success(boosts));
    }

    @PostMapping
    @Operation(summary = "Apply a boost to a listing")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ListingBoostResponseDto>> applyBoost(
            @PathVariable UUID listingId,
            @Valid @RequestBody ApplyBoostRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        ListingBoostResponseDto response = listingBoostService.applyBoostToListing(
                currentUser.getUserId(), listingId, request.getBoostType());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Boost applied successfully", response));
    }

    @DeleteMapping("/{boostType}")
    @Operation(summary = "Remove a boost from a listing")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> removeBoost(
            @PathVariable UUID listingId,
            @PathVariable String boostType,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        listingBoostService.removeBoostFromListing(currentUser.getUserId(), listingId, boostType);
        return ResponseEntity.noContent().build();
    }
}
