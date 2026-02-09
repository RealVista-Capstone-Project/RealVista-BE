package com.sep.realvista.presentation.rest.listing.bookmark;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.bookmark.dto.BookmarkListingCardDTO;
import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.application.listing.bookmark.dto.GetBookmarksRequest;
import com.sep.realvista.application.listing.bookmark.service.BookmarkApplicationService;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.listing.ListingType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Bookmark operations.
 *
 * Handles HTTP requests for bookmarking/unbookmarking listings.
 * Follows Clean Architecture by delegating business logic to application service.
 */
@RestController
@RequestMapping("/api/v1/listings/bookmark")
@RequiredArgsConstructor
@Tag(name = "Listing Management", description = "Endpoints for managing listing bookmarks")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class BookmarkController {

    private final BookmarkApplicationService bookmarkApplicationService;
    private final UserApplicationService userApplicationService;

    /**
     * Get all bookmarks for the authenticated user with filters and pagination.
     *
     * @param propertyTypeIds optional list of property type IDs to filter by
     * @param listingType optional listing type filter (SALE or RENT)
     * @param sortDirection sort direction (NEWEST or OLDEST)
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated list of bookmarked listings
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'TENANT')")
    @Operation(
            summary = "Get all bookmarked listings",
            description = "Retrieves all bookmarked listings for the authenticated user with optional filters. "
                    + "Supports filtering by property type and listing type, with pagination and sorting."
    )
    public ResponseEntity<ApiResponse<PageResponse<BookmarkListingCardDTO>>> getBookmarks(
            @Parameter(description = "Property type IDs to filter by (multiple selection)")
            @RequestParam(required = false) List<UUID> propertyTypeIds,

            @Parameter(description = "Listing type filter: SALE or RENT")
            @RequestParam(required = false) ListingType listingType,

            @Parameter(description = "Sort direction based on bookmark date")
            @RequestParam(required = false, defaultValue = "NEWEST") GetBookmarksRequest.SortDirection sortDirection,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        // Get authenticated user email from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        UUID userId = userApplicationService.findUserIdByEmail(userEmail);

        log.info("Get bookmarks request - traceId: {}, userEmail: {}, userId: {}, "
                        + "propertyTypeIds: {}, listingType: {}, sort: {}, page: {}, size: {}",
                traceId, userEmail, userId, propertyTypeIds, listingType, sortDirection, page, size);

        GetBookmarksRequest request = GetBookmarksRequest.builder()
                .propertyTypeIds(propertyTypeIds)
                .listingType(listingType)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        PageResponse<BookmarkListingCardDTO> response = bookmarkApplicationService.getBookmarks(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Bookmarks retrieved successfully", response));
    }

    /**
     * Toggle bookmark status for a listing.
     *
     * If the authenticated user has already bookmarked the listing, removes the bookmark.
     * If the user hasn't bookmarked the listing, creates a new bookmark.
     *
     * @param listingId the listing ID to bookmark/unbookmark
     * @return BookmarkResponse with action details
     */
    @PostMapping("/{listingId}")
    @PreAuthorize("hasAnyRole('BUYER', 'TENANT')")
    @Operation(
            summary = "Toggle listing bookmark",
            description = "Bookmarks a listing if not already bookmarked, or removes bookmark if already bookmarked. "
                         + "Returns details about the user and listing involved in the action."
    )
    public ResponseEntity<ApiResponse<BookmarkResponse>> toggleBookmark(
            @PathVariable UUID listingId
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        // Get authenticated user email from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        UUID userId = userApplicationService.findUserIdByEmail(userEmail);

        log.info("Toggle bookmark request - traceId: {}, userEmail: {}, userId: {}, listingId: {}",
                traceId, userEmail, userId, listingId);

        BookmarkResponse response = bookmarkApplicationService.toggleBookmark(userId, listingId);

        String actionMessage = response.isBookmarked()
                ? "Listing bookmarked successfully"
                : "Listing bookmark removed successfully";

        return ResponseEntity.ok(ApiResponse.success(actionMessage, response));
    }
}