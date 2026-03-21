package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.CostBreakdownDTO;
import com.sep.realvista.application.listing.dto.CreateListingRequest;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.ListingResponse;
import com.sep.realvista.application.listing.dto.PriceChangeType;
import com.sep.realvista.application.listing.dto.PriceHistoryDTO;
import com.sep.realvista.application.listing.dto.PriceHistoryResponse;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.dto.UpdateListingRequest;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.analytics.ListingPriceHistory;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingPriceHistoryRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.similarity.SimilarListing;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application Service for Listing operations.
 * Orchestrates business logic and coordinates between domain and infrastructure
 * layers.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ListingApplicationService {

    private final ListingRepository listingRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final ListingPriceHistoryRepository listingPriceHistoryRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyAttributeValueRepository propertyAttributeValueRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final ListingMapper listingMapper;
    private final CostBreakdownService costBreakdownService;
    private final BookmarkRepository bookmarkRepository;
    private final ListingAnalyticsService listingAnalyticsService;

    // Self-injection via @Lazy to route internal calls through the Spring AOP proxy,
    // ensuring @Cacheable on getCachedListingDetail is actually triggered.
    @Lazy
    @Autowired
    private ListingApplicationService self;

    /**
     * Verifies if a user can modify a listing.
     * A user can modify a listing if they are either:
     * 1. The listing creator (userId matches listing.userId), OR
     * 2. The property owner (userId matches listing.property.ownerId)
     *
     * @param listing the listing to check
     * @param userId the user ID attempting to modify
     * @return true if user is authorized, false otherwise
     */
    private boolean canModifyListing(Listing listing, UUID userId) {
        // Check if user is the listing creator
        if (listing.getUserId().equals(userId)) {
            return true;
        }

        // Check if user is the property owner
        // Need to fetch the property to check ownerId
        Property property = propertyRepository.findById(listing.getPropertyId())
                .orElse(null);

        if (property != null && property.getOwnerId().equals(userId)) {
            return true;
        }

        return false;
    }

    /**
     * Verifies authorization and throws exception if user cannot modify the listing.
     *
     * @param listing the listing to check
     * @param userId the user ID attempting to modify
     * @param operation the operation being performed (for error message)
     * @throws IllegalStateException if user is not authorized
     */
    private void verifyListingModificationAuthorization(Listing listing, UUID userId, String operation) {
        if (!canModifyListing(listing, userId)) {
            log.error("User {} is not authorized to {} listing {} (not creator or property owner)",
                    userId, operation, listing.getListingId());
            throw new IllegalStateException("You are not authorized to modify this listing");
        }
    }

    /**
     * Get listing detail by ID.
     * Returns complete listing information including media, property, location,
     * type, category, agent/owner, and attributes.
     * Caches the core listing data (without is_favorite), then adds user-specific bookmark status.
     *
     * @param listingId the listing ID
     * @param userId    optional user ID for bookmark status
     * @return complete listing detail response
     * @throws ResourceNotFoundException if listing not found
     */
    @Transactional(readOnly = true)
    public ListingDetailResponse getListingDetail(UUID listingId, UUID userId) {
        // Route through self (proxy) so @Cacheable on getCachedListingDetail fires correctly
        ListingDetailResponse response = self.getCachedListingDetail(listingId);

        // Set is_favorite based on current user (not cached)
        if (userId != null) {
            boolean isFavorite = bookmarkRepository.existsByUserIdAndListingId(userId, listingId);
            response.setIsFavorite(isFavorite);

            // Record view for analytics (async - does not slow down response)
            listingAnalyticsService.recordView(listingId, userId);
        } else {
            response.setIsFavorite(false);
        }

        return response;
    }

    /**
     * Internal method to get cached listing detail without user-specific data.
     * This method is cached by listingId only (not per-user).
     *
     * @param listingId the listing ID
     * @return listing detail response (is_favorite will be null)
     * @throws ResourceNotFoundException if listing not found
     */
    @Cacheable(value = "listings", key = "#listingId")
    public ListingDetailResponse getCachedListingDetail(UUID listingId) {
        log.info("Fetching listing detail for ID: {}", listingId);

        // Fetch listing with all associations
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found in getCachedListingDetail with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify property exists and is accessible
        Property property = propertyRepository.findById(listing.getPropertyId())
                .orElseThrow(() -> {
                    log.error("Property not found for listing ID: {}, property ID: {}",
                            listingId, listing.getPropertyId());
                    return new ResourceNotFoundException("Property", listing.getPropertyId());
                });

        // Fetch listing media
        var listingMedias = listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId);

        // Fetch property attribute values (bedrooms, bathrooms, etc.)
        List<PropertyAttributeValue> attributeValues = propertyAttributeValueRepository
                .findByPropertyIdWithAttribute(property.getPropertyId());

        // Fetch property amenities (gym, pool, security, etc.)
        List<PropertyAmenity> propertyAmenities = propertyAmenityRepository
                .findByPropertyIdWithAmenity(property.getPropertyId());

        // Attach property and user for DTO mapping (read-only, not persisted)
        listing.attachProperty(property);

        log.info("Successfully fetched listing detail for ID: {} with {} attributes and {} amenities",
                listingId, attributeValues.size(), propertyAmenities.size());

        ListingDetailResponse response = listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
                listing, listingMedias, attributeValues, propertyAmenities);

        // Calculate and add cost breakdown (only for RENT listings)
        CostBreakdownDTO costBreakdown = costBreakdownService.calculateCostBreakdown(listing);
        response.setCostBreakdown(costBreakdown);

        // Note: is_favorite is NOT set here - it will be set by the public method
        return response;
    }

    /**
     * Get listing detail by slug.
     * Returns complete listing information using SEO-friendly slug.
     * Caches the core listing data (without is_favorite), then adds user-specific bookmark status.
     *
     * @param slug   the listing slug (format: {name}-{short-uuid})
     * @param userId optional user ID for bookmark status
     * @return complete listing detail response
     * @throws ResourceNotFoundException if listing not found
     */
    @Transactional(readOnly = true)
    public ListingDetailResponse getListingBySlug(String slug, UUID userId) {
        log.info("Fetching listing detail for slug: {}", slug);

        // Find listing by slug (not cached, lightweight operation)
        Listing listing = listingRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.error("Listing not found with slug: {}", slug);
                    return new ResourceNotFoundException("Listing with slug: " + slug);
                });

        // Delegate to getListingDetail which handles caching by listingId
        return getListingDetail(listing.getListingId(), userId);
    }

    /**
     * Get similar listings based on property type, price, area, and common
     * attributes.
     * Returns listings sorted by similarity score (descending) and published date
     * (descending).
     *
     * @param listingId the listing ID to find similar listings for
     * @param limit     maximum number of results to return (default 5, max 10)
     * @return similar listings response with scores
     * @throws ResourceNotFoundException if listing not found
     */
    @Cacheable(value = "similarListings", key = "#listingId + '_'"
            + " + T(java.lang.Math).min("
            + "T(java.lang.Math).max(1, #limit), 10)")
    @Transactional(readOnly = true)
    public SimilarListingsResponse getSimilarListings(UUID listingId, int limit) {
        log.info("Fetching similar listings for listingId: {}, limit: {}", listingId, limit);

        // Validate limit (clamped to [1, 10])
        int validatedLimit = Math.min(Math.max(1, limit), 10);
        if (validatedLimit != limit) {
            log.warn("Requested limit {} adjusted to {}", limit, validatedLimit);
        }

        // Verify listing exists
        if (!listingRepository.existsById(listingId)) {
            log.error("Listing not found in getSimilarListings with ID: {}", listingId);
            throw new ResourceNotFoundException("Listing", listingId);
        }

        // Fetch similar listings from repository
        List<SimilarListing> similarListings = listingRepository.findSimilarListings(listingId, validatedLimit);

        log.info("Found {} similar listings for listingId: {}", similarListings.size(), listingId);

        // Batch-fetch required attributes for all similar listings' properties
        Map<UUID, List<PropertyAttributeDTO>> attributesByPropertyId = fetchRequiredAttributes(similarListings);

        // Map to DTOs
        List<SimilarListingDTO> dtoList = similarListings.stream()
                .map(sl -> mapToSimilarListingDTO(sl, attributesByPropertyId))
                .collect(Collectors.toList());

        return SimilarListingsResponse.builder()
                .listings(dtoList)
                .total(dtoList.size())
                .limit(validatedLimit)
                .build();
    }

    /**
     * Batch-fetch required attributes for all similar listings' properties.
     * Returns up to 3 required attributes per property, grouped by property ID.
     */
    private Map<UUID, List<PropertyAttributeDTO>> fetchRequiredAttributes(List<SimilarListing> similarListings) {
        if (similarListings.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> propertyIds = similarListings.stream()
                .map(SimilarListing::getPropertyId)
                .collect(Collectors.toList());

        List<PropertyAttributeValue> allAttributes = propertyAttributeValueRepository
                .findRequiredAttributesByPropertyIds(propertyIds);

        // Group by propertyId, limit to 3 per property
        return allAttributes.stream()
                .collect(Collectors.groupingBy(PropertyAttributeValue::getPropertyId))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .limit(3)
                                .map(this::mapToPropertyAttributeDTO)
                                .collect(Collectors.toList())));
    }

    /**
     * Map PropertyAttributeValue to a lightweight PropertyAttributeDTO.
     */
    private PropertyAttributeDTO mapToPropertyAttributeDTO(PropertyAttributeValue pav) {
        var attr = pav.getPropertyAttribute();
        return PropertyAttributeDTO.builder()
                .attributeId(attr.getPropertyAttributeId())
                .attributeCode(attr.getCode())
                .attributeName(attr.getName())
                .dataType(attr.getDataType().name())
                .icon(attr.getIcon())
                .unit(attr.getUnit())
                .valueNumber(pav.getValueNumber())
                .valueText(pav.getValueText())
                .valueBoolean(pav.getValueBoolean())
                .build();
    }

    /**
     * Map SimilarListing domain object to DTO with attributes.
     */
    private SimilarListingDTO mapToSimilarListingDTO(SimilarListing similarListing,
                                                     Map<UUID, List<PropertyAttributeDTO>> attributesByPropertyId) {
        return SimilarListingDTO.builder()
                .listingId(similarListing.getListingId())
                .slug(similarListing.getSlug())
                .name(similarListing.getName())
                .listingType(similarListing.getListingType())
                .propertyTypeName(similarListing.getPropertyTypeName())
                .price(similarListing.getPrice())
                .area(similarListing.getArea())
                .locationName(similarListing.getLocationName())
                .thumbnailUrl(similarListing.getThumbnailUrl())
                .similarityScore(similarListing.getSimilarityPercentage())
                .publishedAt(similarListing.getPublishedAt())
                .attributes(attributesByPropertyId.getOrDefault(
                        similarListing.getPropertyId(), Collections.emptyList()))
                .build();
    }

    /**
     * Get price history for a listing.
     * Returns all price changes with calculated differences and percentages.
     *
     * @param listingId the listing ID
     * @return price history response with current price and historical entries
     * @throws ResourceNotFoundException if listing not found
     */
    @Transactional(readOnly = true)
    public PriceHistoryResponse getPriceHistory(UUID listingId) {
        log.info("Fetching price history for listing ID: {}", listingId);

        // Verify listing exists
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found getPriceHistory with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Fetch price history entries
        List<ListingPriceHistory> historyEntries = listingPriceHistoryRepository
                .findByListingIdOrderByCreatedAtDesc(listingId);

        // Build price history DTOs with calculated changes
        // Entries are ordered DESC (newest first), so we compare each entry
        // to the NEXT (older) entry to determine if price increased/decreased
        List<PriceHistoryDTO> priceHistoryDTOs = new ArrayList<>();

        for (int i = 0; i < historyEntries.size(); i++) {
            ListingPriceHistory entry = historyEntries.get(i);
            PriceChangeType changeType;
            BigDecimal priceChange = null;
            Double priceChangePercent = null;

            // Null safety check for entry price
            if (entry.getPrice() == null) {
                log.warn("Skipping price history entry with null price for listing ID: {}",
                        listingId);
                continue;
            }

            // Look ahead to the next (older) entry for comparison
            if (i + 1 < historyEntries.size()) {
                BigDecimal olderPrice = historyEntries.get(i + 1).getPrice();

                // Null safety check for older price
                if (olderPrice == null) {
                    changeType = PriceChangeType.UNCHANGED;
                    priceChange = BigDecimal.ZERO;
                    priceChangePercent = 0d;
                } else {
                    int comparison = entry.getPrice().compareTo(olderPrice);
                    if (comparison > 0) {
                        changeType = PriceChangeType.INCREASED;
                    } else if (comparison < 0) {
                        changeType = PriceChangeType.DECREASED;
                    } else {
                        changeType = PriceChangeType.UNCHANGED;
                    }

                    priceChange = entry.getPrice().subtract(olderPrice);

                    if (olderPrice.compareTo(BigDecimal.ZERO) > 0) {
                        priceChangePercent = priceChange
                                .divide(olderPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }
                }
            } else {
                // Last entry (oldest in history) - this is the initial price
                changeType = PriceChangeType.INITIAL;
            }

            PriceHistoryDTO dto = PriceHistoryDTO.builder()
                    .priceHistoryId(entry.getListingPriceHistoryId())
                    .price(entry.getPrice())
                    .minPrice(entry.getMinPrice())
                    .maxPrice(entry.getMaxPrice())
                    .changedAt(entry.getCreatedAt())
                    .priceChange(priceChange)
                    .priceChangePercent(priceChangePercent)
                    .changeType(changeType)
                    .build();

            priceHistoryDTOs.add(dto);
        }

        log.info("Successfully fetched {} price history entries for listing ID: {}",
                priceHistoryDTOs.size(), listingId);

        return PriceHistoryResponse.builder()
                .listingId(listingId)
                .currentPrice(listing.getPrice())
                .priceHistory(priceHistoryDTOs)
                .build();
    }

    /**
     * Create a new listing.
     *
     * @param request the create listing request
     * @param userId  the user ID creating the listing
     * @return listing response
     * @throws ResourceNotFoundException if property not found
     */
    public ListingResponse createListing(
            CreateListingRequest request,
            UUID userId) {
        log.info("Creating new listing for user ID: {}, property ID: {}", userId, request.getPropertyId());

        // Verify property exists
        propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> {
                    log.error("Property not found with ID: {}", request.getPropertyId());
                    return new ResourceNotFoundException("Property", request.getPropertyId());
                });

        // Generate slug from name
        UUID listingId = UUID.randomUUID();
        String slug = com.sep.realvista.shared.util.ShortIdUtils.generateSlug(request.getName(), listingId);

        // Build listing entity
        Listing listing = Listing.builder()
                .listingId(listingId)
                .propertyId(request.getPropertyId())
                .userId(userId)
                .listingType(request.getListingType())
                .name(request.getName())
                .slug(slug)
                .price(request.getPrice())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .isNegotiable(request.getIsNegotiable() != null ? request.getIsNegotiable() : false)
                .availableFrom(request.getAvailableFrom())
                .status(com.sep.realvista.domain.listing.ListingStatus.DRAFT)
                .build();

        // Save listing
        Listing savedListing = listingRepository.save(listing);

        // Create initial price history entry
        ListingPriceHistory priceHistory = ListingPriceHistory.builder()
                .listingId(savedListing.getListingId())
                .price(savedListing.getPrice())
                .minPrice(savedListing.getMinPrice())
                .maxPrice(savedListing.getMaxPrice())
                .build();
        listingPriceHistoryRepository.save(priceHistory);

        log.info("Successfully created listing with ID: {}", savedListing.getListingId());

        return listingMapper.toListingResponse(savedListing);
    }

    /**
     * Update an existing listing.
     *
     * @param listingId the listing ID
     * @param request   the update listing request
     * @param userId    the user ID performing the update
     * @return updated listing response
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException     if user is not the listing creator or property owner
     */
    @CacheEvict(value = "listings", key = "#listingId")
    public ListingResponse updateListing(
            UUID listingId,
            UpdateListingRequest request,
            UUID userId) {
        log.info("Updating listing ID: {} by user ID: {}", listingId, userId);

        // Fetch listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found in updateListing with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify ownership (listing creator OR property owner)
        verifyListingModificationAuthorization(listing, userId, "update");

        // Track if price changed for price history
        boolean priceChanged = false;
        BigDecimal oldPrice = listing.getPrice();

        // Update fields
        if (request.getName() != null) {
            listing.updateSlug(com.sep.realvista.shared.util.ShortIdUtils.generateSlug(
                    request.getName(), listingId));
        }

        if (request.getPrice() != null || request.getMinPrice() != null
                || request.getMaxPrice() != null
                || request.getIsNegotiable() != null) {
            BigDecimal newPrice = request.getPrice() != null ? request.getPrice() : listing.getPrice();
            if (!newPrice.equals(oldPrice)) {
                priceChanged = true;
            }
            listing.updatePricing(
                    request.getPrice(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getIsNegotiable());
        }

        // Note: Update availableFrom is not currently implemented
        // This would require adding a method to the Listing domain entity

        // Save listing
        Listing updatedListing = listingRepository.save(listing);

        // Create price history entry if price changed
        if (priceChanged) {
            ListingPriceHistory priceHistory = ListingPriceHistory.builder()
                    .listingId(updatedListing.getListingId())
                    .price(updatedListing.getPrice())
                    .minPrice(updatedListing.getMinPrice())
                    .maxPrice(updatedListing.getMaxPrice())
                    .build();
            listingPriceHistoryRepository.save(priceHistory);
            log.info("Created price history entry for listing ID: {} (old: {}, new: {})",
                    listingId, oldPrice, updatedListing.getPrice());
        }

        log.info("Successfully updated listing ID: {}", listingId);

        return listingMapper.toListingResponse(updatedListing);
    }

    /**
     * Delete a listing (soft delete).
     *
     * @param listingId the listing ID
     * @param userId    the user ID performing the deletion
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException     if user is not the listing creator or property owner
     */
    public void deleteListing(UUID listingId, UUID userId) {
        log.info("Deleting listing ID: {} by user ID: {}", listingId, userId);

        // Fetch listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found deleteListing with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify ownership (listing creator OR property owner)
        verifyListingModificationAuthorization(listing, userId, "delete");

        // Soft delete
        listingRepository.deleteById(listingId);

        log.info("Successfully deleted listing ID: {}", listingId);
    }

    /**
     * Get all listings for a user.
     * Returns listings where the user is either the listing creator OR the property owner.
     *
     * @param userId the user ID
     * @return list of user's listings
     */
    @Transactional(readOnly = true)
    public List<com.sep.realvista.application.listing.dto.ListingResponse> getMyListings(UUID userId) {
        log.info("Fetching all listings for user ID: {}", userId);

        List<Listing> listings = listingRepository.findByUserIdOrPropertyOwnerId(userId);

        log.info("Found {} listings for user ID: {}", listings.size(), userId);

        return listings.stream()
                .map(listing -> {
                    ListingResponse response =
                            listingMapper.toListingResponse(listing);

                    // If thumbnail is null, fetch it from repository
                    if (response.getThumbnail() == null) {
                        listingRepository.findThumbnailByListingId(listing.getListingId())
                                .ifPresent(response::setThumbnail);
                    }

                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Submit listing for review (DRAFT -> PENDING).
     *
     * @param listingId the listing ID
     * @param userId    the user ID performing the action
     * @return updated listing response
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException     if user is not the listing creator or property owner, or listing is not in DRAFT status
     */
    @CacheEvict(value = "listings", key = "#listingId")
    public ListingResponse submitForReview(
            UUID listingId, UUID userId) {
        log.info("Submitting listing ID: {} for review by user ID: {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found in submitForReview with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify ownership (listing creator OR property owner)
        verifyListingModificationAuthorization(listing, userId, "submit for review");

        listing.submitForReview();
        Listing updatedListing = listingRepository.save(listing);

        log.info("Successfully submitted listing ID: {} for review", listingId);

        return listingMapper.toListingResponse(updatedListing);
    }

    /**
     * Publish listing (PENDING/DRAFT -> PUBLISHED).
     *
     * @param listingId the listing ID
     * @param userId    the user ID performing the action
     * @return updated listing response
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException     if user is not the listing creator or property owner, or listing cannot be published
     */
    @CacheEvict(value = "listings", key = "#listingId")
    public ListingResponse publishListing(
            UUID listingId, UUID userId) {
        log.info("Publishing listing ID: {} by user ID: {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found in publishListing with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify ownership (listing creator OR property owner)
        verifyListingModificationAuthorization(listing, userId, "publish");

        listing.publish();
        Listing updatedListing = listingRepository.save(listing);

        log.info("Successfully published listing ID: {}", listingId);

        return listingMapper.toListingResponse(updatedListing);
    }

    /**
     * Unpublish listing (PUBLISHED -> DRAFT).
     *
     * @param listingId the listing ID
     * @param userId    the user ID performing the action
     * @return updated listing response
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException     if user is not the listing creator or property owner, or listing is not published
     */
    @CacheEvict(value = "listings", key = "#listingId")
    public com.sep.realvista.application.listing.dto.ListingResponse unpublishListing(
            UUID listingId, UUID userId) {
        log.info("Unpublishing listing ID: {} by user ID: {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify ownership (listing creator OR property owner)
        verifyListingModificationAuthorization(listing, userId, "unpublish");

        listing.unpublish();
        Listing updatedListing = listingRepository.save(listing);

        log.info("Successfully unpublished listing ID: {}", listingId);

        return listingMapper.toListingResponse(updatedListing);
    }

    /**
     * Mark listing as sold (SALE listings only).
     *
     * @param listingId the listing ID
     * @param userId    the user ID performing the action
     * @return updated listing response
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException     if user is not the listing creator or property owner, listing is not SALE type,
     *                                   or not published
     */
    @CacheEvict(value = "listings", key = "#listingId")
    public ListingResponse markAsSold(
            UUID listingId, UUID userId) {
        log.info("Marking listing ID: {} as sold by user ID: {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found markAsSold with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify ownership (listing creator OR property owner)
        verifyListingModificationAuthorization(listing, userId, "mark as sold");

        listing.markAsSold();
        Listing updatedListing = listingRepository.save(listing);

        log.info("Successfully marked listing ID: {} as sold", listingId);

        return listingMapper.toListingResponse(updatedListing);
    }

    /**
     * Mark listing as rented (RENT listings only).
     *
     * @param listingId the listing ID
     * @param userId    the user ID performing the action
     * @return updated listing response
     * @throws ResourceNotFoundException if listing not found
     * @throws IllegalStateException     if user is not the listing creator or property owner, listing is not RENT type,
     *                                   or not published
     */
    @CacheEvict(value = "listings", key = "#listingId")
    public ListingResponse markAsRented(
            UUID listingId, UUID userId) {
        log.info("Marking listing ID: {} as rented by user ID: {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.error("Listing not found markAsRented with ID: {}", listingId);
                    return new ResourceNotFoundException("Listing", listingId);
                });

        // Verify ownership (listing creator OR property owner)
        verifyListingModificationAuthorization(listing, userId, "mark as rented");

        listing.markAsRented();
        Listing updatedListing = listingRepository.save(listing);

        log.info("Successfully marked listing ID: {} as rented", listingId);

        return listingMapper.toListingResponse(updatedListing);
    }
}
