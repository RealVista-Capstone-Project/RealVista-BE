package com.sep.realvista.presentation.rest.profile;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.profile.dto.SaveSearchRequest;
import com.sep.realvista.application.profile.dto.SavedSearchDto;
import com.sep.realvista.application.service.profile.SavedSearchService;
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
@RequestMapping("/api/v1/saved-searches")
@RequiredArgsConstructor
@Tag(name = "Saved Searches", description = "APIs for managing saved property searches")
@SecurityRequirement(name = "Bearer Authentication")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    @PostMapping
    @Operation(summary = "Save search", description = "Saves the current search criteria for the authenticated user")
    public ResponseEntity<ApiResponse<SavedSearchDto>> saveSearch(
            @Valid @RequestBody SaveSearchRequest request,
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        SavedSearchDto savedSearch = savedSearchService.saveSearch(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedSearch));
    }

    @GetMapping
    @Operation(summary = "Get my saved searches",
            description = "Retrieves all saved searches for the authenticated user")
    public ResponseEntity<ApiResponse<List<SavedSearchDto>>> getMySavedSearches(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                savedSearchService.getMySavedSearches(userDetails.getUserId())
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete saved search", description = "Deletes a saved search")
    public ResponseEntity<ApiResponse<Void>> deleteSavedSearch(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        savedSearchService.deleteSavedSearch(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Saved search deleted successfully", null));
    }
}
