package com.sep.realvista.application.listing.bookmark.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for bookmark operations.
 * 
 * Returns information about which user bookmarked/unbookmarked which listing.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookmarkResponse {

    private UUID userId;
    private UUID listingId;
    private String userEmail;
    private String userFullName;
    private String listingType;
    private String propertyAddress; 
    private boolean bookmarked;
    private LocalDateTime actionTimestamp;
}