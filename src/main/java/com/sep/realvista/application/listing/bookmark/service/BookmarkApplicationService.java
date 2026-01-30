package com.sep.realvista.application.listing.bookmark.service;

import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.application.listing.bookmark.mapper.BookmarkMapper;
import com.sep.realvista.domain.listing.ListingRepository;
import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import com.sep.realvista.domain.listing.exception.ListingNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application service for bookmark operations.
 * 
 * Orchestrates business logic for bookmarking/unbookmarking listings.
 * Follows Clean Architecture principles by delegating to domain services
 * and coordinating between domain repositories.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookmarkApplicationService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BookmarkMapper bookmarkMapper;

    /**
     * Toggles bookmark status for a listing.
     * 
     * If user has already bookmarked the listing, removes the bookmark.
     * If user hasn't bookmarked the listing, creates a new bookmark.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return BookmarkResponse with action details
     * @throws UserNotFoundException if user doesn't exist
     * @throws ListingNotFoundException if listing doesn't exist
     */
    public BookmarkResponse toggleBookmark(UUID userId, UUID listingId) {
        log.info("Toggling bookmark - userId: {}, listingId: {}", userId, listingId);

        // Validate user exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Validate listing exists
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        // Check if bookmark already exists and toggle
        boolean bookmarkExists = bookmarkRepository.existsByUserIdAndListingId(userId, listingId);
        boolean isBookmarked;

        if (bookmarkExists) {
            // Remove existing bookmark
            bookmarkRepository.deleteByUserIdAndListingId(userId, listingId);
            isBookmarked = false;
            log.info("Removed bookmark - userId: {}, listingId: {}", userId, listingId);
        } else {
            // Create new bookmark
            var bookmark = Bookmark.builder()
                    .userId(userId)
                    .listingId(listingId)
                    .build();
            bookmarkRepository.save(bookmark);
            isBookmarked = true;
            log.info("Created bookmark - userId: {}, listingId: {}", userId, listingId);
        }

        // Use mapper to build response
        return bookmarkMapper.toResponse(user, listing, isBookmarked, LocalDateTime.now());
    }
}