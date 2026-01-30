package com.sep.realvista.presentation.rest.listing.bookmark;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.application.listing.bookmark.service.BookmarkApplicationService;
import com.sep.realvista.application.user.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Toggle bookmark status for a listing.
     * 
     * If the authenticated user has already bookmarked the listing, removes the bookmark.
     * If the user hasn't bookmarked the listing, creates a new bookmark.
     * 
     * @param listingId the listing ID to bookmark/unbookmark
     * @param authentication the authenticated user details
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