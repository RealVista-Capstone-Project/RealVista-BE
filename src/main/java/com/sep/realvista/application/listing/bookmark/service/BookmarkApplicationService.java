package com.sep.realvista.application.listing.bookmark.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.bookmark.dto.BookmarkListingCardDTO;
import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.application.listing.bookmark.dto.GetBookmarksRequest;
import com.sep.realvista.application.listing.bookmark.mapper.BookmarkMapper;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.exception.ListingNotFoundException;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ListingMediaRepository listingMediaRepository;
    private final PropertyAttributeValueRepository propertyAttributeValueRepository;
    private final ListingBoostRepository listingBoostRepository;
    private final BookmarkMapper bookmarkMapper;

    /**
     * Toggles bookmark status for a listing.
     *
     * If user has already bookmarked the listing, removes the bookmark.
     * If user hasn't bookmarked the listing, creates a new bookmark.
     * Evicts the listing cache since is_favorite status changed.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return BookmarkResponse with action details
     * @throws UserNotFoundException if user doesn't exist
     * @throws ListingNotFoundException if listing doesn't exist
     */
    @CacheEvict(value = "listings", key = "#listingId")
    public BookmarkResponse toggleBookmark(UUID userId, UUID listingId) {
        log.info("Toggling bookmark - userId: {}, listingId: {}", userId, listingId);

        // Validate user exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Validate listing exists
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        // Check if bookmark already exists (considering only non-deleted bookmarks)
        boolean exists = bookmarkRepository.existsByUserIdAndListingId(userId, listingId);
        boolean isBookmarked;

        if (exists) {
            // Bookmark exists → remove it
            bookmarkRepository.deleteByUserIdAndListingId(userId, listingId);
            isBookmarked = false;
            log.info("Removed bookmark - userId: {}, listingId: {}", userId, listingId);
        } else {
            // Bookmark doesn't exist → create new one
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

    /**
     * Gets all bookmarks for a user with filters and pagination.
     *
     * @param userId the authenticated user ID
     * @param request the filter and pagination request
     * @return paginated response of bookmarked listing cards
     */
    @Transactional(readOnly = true)
    public PageResponse<BookmarkListingCardDTO> getBookmarks(UUID userId, GetBookmarksRequest request) {
        log.info("Getting bookmarks for user: {} with filters - listingType: {}, "
                        + "propertyTypes: {}, sort: {}, page: {}, size: {}",
                userId, request.getListingType(), request.getPropertyTypes(),
                request.getSortDirection(), request.getPage(), request.getSize());

        // Create pageable with sort direction
        Sort sort = request.getSortDirection() == GetBookmarksRequest.SortDirection.OLDEST
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Fetch bookmarks with filters
        Page<Bookmark> bookmarksPage = bookmarkRepository.findBookmarksByUserWithFilters(
                userId,
                request.getPropertyTypes(),
                request.getListingType(),
                pageable
        );

        // Extract listing IDs and property IDs for batch fetching
        List<UUID> listingIds = bookmarksPage.getContent().stream()
                .map(Bookmark::getListingId)
                .collect(Collectors.toList());

        List<UUID> propertyIds = bookmarksPage.getContent().stream()
                .map(bookmark -> bookmark.getListing().getPropertyId())
                .distinct()
                .collect(Collectors.toList());

        // Batch fetch primary media for all listings in a single query
        List<ListingMedia> allPrimaryMedia = listingMediaRepository.findPrimaryByListingIds(listingIds);
        Map<UUID, ListingMedia> primaryMediaMap = allPrimaryMedia.stream()
                .collect(Collectors.toMap(ListingMedia::getListingId, media -> media));

        // Batch fetch attributes for all properties in a single query
        List<PropertyAttributeValue> allAttributes = propertyAttributeValueRepository
                .findAllAttributesByPropertyIds(propertyIds);
        Map<UUID, List<PropertyAttributeValue>> attributesMap = allAttributes.stream()
                .collect(Collectors.groupingBy(PropertyAttributeValue::getPropertyId));

        // Batch fetch active boosts for all listings
        List<ListingBoost> allActiveBoosts = listingBoostRepository.findActiveByListingIds(listingIds);

        // Map bookmarks to DTOs
        List<BookmarkListingCardDTO> content = bookmarksPage.getContent().stream()
                .map(bookmark -> {
                    UUID listingId = bookmark.getListingId();
                    UUID propertyId = bookmark.getListing().getPropertyId();
                    ListingMedia primaryMedia = primaryMediaMap.get(listingId);
                    List<PropertyAttributeValue> attributes = attributesMap.getOrDefault(propertyId, List.of());

                    BookmarkListingCardDTO dto = bookmarkMapper.toListingCard(
                            bookmark, primaryMedia, attributes, allActiveBoosts);

                    // Apply display priority numbers after mapping (business rule, not mapping concern)
                    if (dto != null && dto.getAttributes() != null) {
                        List<PropertyAttributeDTO> attrs = dto.getAttributes();
                        for (int i = 0; i < attrs.size(); i++) {
                            attrs.get(i).setPriority(i + 1);
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Retrieved {} bookmarks for user: {}", content.size(), userId);

        return PageResponse.<BookmarkListingCardDTO>builder()
                .content(content)
                .page(bookmarksPage.getNumber())
                .size(bookmarksPage.getSize())
                .totalElements(bookmarksPage.getTotalElements())
                .totalPages(bookmarksPage.getTotalPages())
                .first(bookmarksPage.isFirst())
                .last(bookmarksPage.isLast())
                .build();
    }
}