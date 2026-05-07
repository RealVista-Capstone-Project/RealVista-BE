package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.CostBreakdownDTO;
import com.sep.realvista.application.listing.dto.CreateListingRequest;
import com.sep.realvista.application.listing.dto.ListingCompareDataResponse;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.ListingResponse;
import com.sep.realvista.application.listing.dto.ManagedListingSearchCriteria;
import com.sep.realvista.application.listing.dto.ManagedListingSummaryDTO;
import com.sep.realvista.application.listing.dto.PriceChangeType;
import com.sep.realvista.application.listing.dto.PriceHistoryDTO;
import com.sep.realvista.application.listing.dto.PriceHistoryResponse;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.RelatedListingsResponse;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.dto.UpdateListingRequest;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.DomainException;
import com.sep.realvista.domain.common.exception.InsufficientQuotaException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.analytics.ListingPriceHistory;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingPriceHistoryRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.similarity.SimilarListing;
import com.sep.realvista.shared.util.AddressFormatter;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final PriceChangeNotificationService priceChangeNotificationService;
    private final ListingAnalyticsService listingAnalyticsService;
    private final SettingPreferenceRepository settingPreferenceRepository;
    private final com.sep.realvista.infrastructure.service.NotificationMessageService notificationMessageService;
    private final NotificationApplicationService notificationApplicationService;
    private final AppointmentApplicationService appointmentApplicationService;
    private final UserRepository userRepository;
    private final UserFeatureSubscriptionRepository userFeatureSubscriptionRepository;
    private final EngagementRepository engagementRepository;
    private final com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository listingBoostRepository;
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
   * @param userId  the user ID attempting to modify
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
    return property != null && property.getOwnerId().equals(userId);
  }

  /**
   * Verifies authorization and throws exception if user cannot modify the
   * listing.
   *
   * @param listing   the listing to check
   * @param userId    the user ID attempting to modify
   * @param operation the operation being performed (for error message)
   * @throws IllegalStateException if user is not authorized
   */
  private void verifyListingModificationAuthorization(Listing listing, UUID userId, String operation) {
    if (!canModifyListing(listing, userId)) {
      log.error("User {} is not authorized to {} listing {} (not creator or property owner)",
          userId, operation, listing.getListingId());
      throw new DomainException(
          "You are not authorized to modify this listing",
          "ERROR_LISTING_MODIFICATION_FORBIDDEN");
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
    public ListingDetailResponse getListingDetail(UUID listingId, UUID userId, boolean recordView) {
        return getListingDetail(listingId, userId, recordView, false);
    }

    @Transactional(readOnly = true)
    public ListingDetailResponse getListingDetail(UUID listingId, UUID userId, boolean recordView, boolean editing) {
        // Route through self (proxy) so @Cacheable on getCachedListingDetail fires correctly
        ListingDetailResponse response = self.getCachedListingDetail(listingId);
        ensureListingDetailAccessible(response, userId);
        if (userId != null) {
            boolean isFavorite = bookmarkRepository.existsByUserIdAndListingId(userId, listingId);
            response.setIsFavorite(isFavorite);
        } else {
            response.setIsFavorite(false);
        }

        // Record view for analytics (async - does not slow down response)
        if (recordView) {
            listingAnalyticsService.recordView(listingId, userId);
        }

        // Filter media for agents when editing: only show media uploaded by property owner or this agent
        if (editing && userId != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAgent = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
            if (isAgent && response.getPropertyOwner() != null) {
                UUID propertyOwnerId = response.getPropertyOwner().getUserId();
                response.setMedia(response.getMedia().stream()
                        .filter(m -> m.getUploadBy().equals(propertyOwnerId)
                                || m.getUploadBy().equals(userId))
                        .collect(Collectors.toList()));
            }
        }

    return response;
  }

  private void ensureListingDetailAccessible(ListingDetailResponse listing, UUID userId) {
    if (listing.getStatus() == ListingStatus.PUBLISHED) {
      return;
    }

    boolean isListingCreator = userId != null && userId.equals(listing.getUserId());
    boolean isPropertyOwner = userId != null
        && listing.getPropertyOwner() != null
        && userId.equals(listing.getPropertyOwner().getUserId());

    if (!isListingCreator && !isPropertyOwner) {
      throw new ResourceNotFoundException("Listing", listing.getListingId());
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
        // Fetch agent's privacy preferences
        SettingPreference preference = settingPreferenceRepository.findByUserId(listing.getUserId())
                .orElse(null);

        log.info("Successfully fetched listing detail for ID: {} with {} attributes and {} amenities",
                listingId, attributeValues.size(), propertyAmenities.size());

        ListingDetailResponse response = listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
                listing, listingMedias, attributeValues, propertyAmenities, preference);

        // Set isCreatedByOwner flag
        boolean isCreatedByOwner = listing.getUserId().equals(property.getOwnerId());
        response.setIsCreatedByOwner(isCreatedByOwner);

        // Set property owner info
        com.sep.realvista.domain.user.User propertyOwner = userRepository.findById(property.getOwnerId())
                .orElse(null);
        if (propertyOwner != null) {
            SettingPreference ownerPreference = settingPreferenceRepository
                    .findByUserId(propertyOwner.getUserId()).orElse(null);
            response.setPropertyOwner(listingMapper.mapAgentInfo(propertyOwner, ownerPreference));
        }

        // Calculate and add cost breakdown (only for RENT listings)
        CostBreakdownDTO costBreakdown = costBreakdownService.calculateCostBreakdown(listing);
        response.setCostBreakdown(costBreakdown);

        response.setThreeDRoomNames(buildThreeDRoomNames(listingMedias));

        // Note: is_favorite is NOT set here - it will be set by the public method
        return response;
    }
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
    // Fetch agent's privacy preferences
    SettingPreference preference = settingPreferenceRepository.findByUserId(listing.getUserId())
        .orElse(null);

    log.info("Successfully fetched listing detail for ID: {} with {} attributes and {} amenities",
        listingId, attributeValues.size(), propertyAmenities.size());

    ListingDetailResponse response = listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
        listing, listingMedias, attributeValues, propertyAmenities, preference);

    // Set isCreatedByOwner flag
    boolean isCreatedByOwner = listing.getUserId().equals(property.getOwnerId());
    response.setIsCreatedByOwner(isCreatedByOwner);

    // Set property owner info
    com.sep.realvista.domain.user.User propertyOwner = userRepository.findById(property.getOwnerId())
        .orElse(null);
    if (propertyOwner != null) {
      SettingPreference ownerPreference = settingPreferenceRepository
          .findByUserId(propertyOwner.getUserId()).orElse(null);
      response.setPropertyOwner(listingMapper.mapAgentInfo(propertyOwner, ownerPreference));
    }

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
    public ListingDetailResponse getListingBySlug(String slug, UUID userId, boolean recordView) {
        return getListingBySlug(slug, userId, recordView, false);
    }

    @Transactional(readOnly = true)
    public ListingDetailResponse getListingBySlug(String slug, UUID userId, boolean recordView, boolean editing) {
        log.info("Fetching listing detail for slug: {}", slug);
        // Find listing by slug (not cached, lightweight operation)
        Listing listing = listingRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.error("Listing not found with slug: {}", slug);
                    return new ResourceNotFoundException("Listing with slug: " + slug);
                });
        // Delegate to getListingDetail which handles caching by listingId
        return getListingDetail(listing.getListingId(), userId, recordView, editing);
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
  /**
   * Get similar listings based on property type, price, area, and common
   * attributes.
   * Returns listings enriched with user-specific bookmark status.
   *
   * @param listingId the listing ID to find similar listings for
   * @param limit     maximum number of results to return
   * @param userId    optional user ID for bookmark status
   * @return similar listings response with user-specific data
   */
  @Transactional(readOnly = true)
  public SimilarListingsResponse getSimilarListings(UUID listingId, int limit, UUID userId) {
    // 1. Get cached similarity results
    SimilarListingsResponse cached = self.getCachedSimilarListings(listingId, limit);

    // 2. If no user or no results, return as is
    if (userId == null || cached.getListings() == null || cached.getListings().isEmpty()) {
      return cached;
    }

    // 3. Populate is_favorite for current user
    List<UUID> listingIds = cached.getListings().stream()
        .map(SimilarListingDTO::getListingId)
        .collect(Collectors.toList());

    Set<UUID> bookmarkedIds = bookmarkRepository.findBookmarkedListingIds(userId, listingIds);

    List<SimilarListingDTO> enriched = cached.getListings().stream()
        .map(dto -> {
          // Create a copy to avoid polluting the cache
          SimilarListingDTO copy = SimilarListingDTO.builder()
              .listingId(dto.getListingId())
              .slug(dto.getSlug())
              .name(dto.getName())
              .listingType(dto.getListingType())
              .propertyTypeName(dto.getPropertyTypeName())
              .price(dto.getPrice())
              .area(dto.getArea())
              .locationName(dto.getLocationName())
              .thumbnailUrl(dto.getThumbnailUrl())
              .similarityScore(dto.getSimilarityScore())
              .publishedAt(dto.getPublishedAt())
              .attributes(dto.getAttributes())
              .isFavorite(bookmarkedIds.contains(dto.getListingId()))
              .build();
          return copy;
        })
        .collect(Collectors.toList());

    return SimilarListingsResponse.builder()
        .listings(enriched)
        .total(cached.getTotal())
        .limit(cached.getLimit())
        .build();
  }

  /**
   * Internal method to get cached similarity results without user-specific data.
   */
  @Cacheable(value = "similarListings", key = "#listingId + '_'"
      + " + T(java.lang.Math).min("
      + "T(java.lang.Math).max(1, #limit), 10)")
  @Transactional(readOnly = true)
  public SimilarListingsResponse getCachedSimilarListings(UUID listingId, int limit) {
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
    // Batch-fetch attributes for all similar listings' properties
    Map<UUID, List<PropertyAttributeDTO>> attributesByPropertyId = fetchTopAttributes(similarListings);
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
   * Batch-fetch attributes for all similar listings' properties.
   * Returns up to 3 attributes per property, grouped by property ID.
   */
  private Map<UUID, List<PropertyAttributeDTO>> fetchTopAttributes(List<SimilarListing> similarListings) {
    if (similarListings.isEmpty()) {
      return Collections.emptyMap();
    }
    List<UUID> propertyIds = similarListings.stream()
        .map(SimilarListing::getPropertyId)
        .collect(Collectors.toList());
    List<PropertyAttributeValue> allAttributes = propertyAttributeValueRepository
        .findAllAttributesByPropertyIds(propertyIds);
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
        .fullAddress(AddressFormatter.formatFullAddress(
            similarListing.getStreetAddress(),
            similarListing.getWardName(),
            similarListing.getDistrictName(),
            similarListing.getCityName()))
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
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> {
                    log.error("Property not found with ID: {}", request.getPropertyId());
                    return new ResourceNotFoundException("Property", request.getPropertyId());
                });

        // Build listing entity in DRAFT status initially
        Listing listing = Listing.builder()
                .propertyId(request.getPropertyId())
                .userId(userId)
                .listingType(request.getListingType())
                .name(request.getName())
                .slug("placeholder-" + UUID.randomUUID()) // temporary unique slug; updated after persist
                .price(request.getPrice())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .isNegotiable(request.getIsNegotiable() != null ? request.getIsNegotiable() : false)
                .availableFrom(request.getAvailableFrom())
                .content(request.getContent())
                .status(ListingStatus.DRAFT)
                .build();

        // Handle immediate publication if requested
        if (Boolean.TRUE.equals(request.getShouldPublish())) {
            validatePropertyCanPublishListing(property, request.getListingType(), request.getAvailableFrom());

            // 2. Verify no other published listing of same type exists for this user/property
            boolean duplicateExists = listingRepository.existsByPropertyIdAndListingTypeAndStatusAndUserId(
                    property.getPropertyId(), request.getListingType(), ListingStatus.PUBLISHED, userId);
            if (duplicateExists) {
                log.error("Listing creator {} already has a published {} listing for property {}",
                        userId, request.getListingType(), property.getPropertyId());
                throw new BusinessConflictException(String.format(
                        "A published listing of type %s already exists for this property and user.",
                        request.getListingType().name()), "ERROR_DUPLICATE_LISTING_PUBLISH");
            }

            consumeListingQuotaIfFirstPublish(listing, userId);
            listing.publish();
        }

        // Persist first so that the real listingId is generated by the DB/Hibernate
        Listing savedListing = listingRepository.save(listing);

        // Now generate the proper SEO slug using the real persisted ID
        String slug = com.sep.realvista.shared.util.ShortIdUtils.generateSlug(
                request.getName(), savedListing.getListingId());
        savedListing.updateSlug(slug);
        savedListing = listingRepository.save(savedListing);

        // Create initial price history entry
        ListingPriceHistory priceHistory = ListingPriceHistory.builder()
                .listingId(savedListing.getListingId())
                .price(savedListing.getPrice())
                .minPrice(savedListing.getMinPrice())
                .maxPrice(savedListing.getMaxPrice())
                .changedBy(savedListing.getUserId())
                .build();
        listingPriceHistoryRepository.save(priceHistory);

        // 3. Persist media relationships if provided
        List<UUID> mediaIds = request.getMediaIds();
        if (mediaIds != null && !mediaIds.isEmpty()) {
            for (int displayOrder = 0; displayOrder < mediaIds.size(); displayOrder++) {
                UUID mediaId = mediaIds.get(displayOrder);
                if (mediaId == null) {
                    continue; // skip null IDs sent from client
                }
                boolean isPrimary = mediaId.equals(request.getPrimaryMediaId());
                ListingMedia listingMedia = ListingMedia.create(
                        savedListing.getListingId(), mediaId, displayOrder, isPrimary);
                listingMediaRepository.save(listingMedia);
            }
            log.info("Saved {} media records for listing ID: {}", mediaIds.size(), savedListing.getListingId());
        }

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
        if (request.getName() != null && !listing.getName().equals(request.getName())) {
            listing.updateSlug(com.sep.realvista.shared.util.ShortIdUtils.generateSlug(
                    request.getName(), listingId));
            listing.setName(request.getName());
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

        if (request.getAvailableFrom() != null) {
            listing.setAvailableFrom(request.getAvailableFrom());
        }

        // Update listing type if requested and allowed
        if (request.getListingType() != null && !listing.getListingType().equals(request.getListingType())) {
            if (listing.getStatus() == ListingStatus.PUBLISHED
                    || listing.getStatus() == ListingStatus.SOLD
                    || listing.getStatus() == ListingStatus.RENTED) {
                log.error("Cannot change listing type for listing ID: {} in status: {}",
                        listingId, listing.getStatus());
                throw new BusinessConflictException(
                        "Cannot change listing type for a listing that is Published, Sold, or Rented",
                        "ERROR_FORBIDDEN_TYPE_CHANGE");
            }
            listing.setListingType(request.getListingType());
        }

        // Save listing
        Listing updatedListing = listingRepository.save(listing);

        // Create price history entry if price changed
        if (priceChanged) {
            ListingPriceHistory priceHistory = ListingPriceHistory.builder()
                    .listingId(updatedListing.getListingId())
                    .price(updatedListing.getPrice())
                    .minPrice(updatedListing.getMinPrice())
                    .maxPrice(updatedListing.getMaxPrice())
                    .changedBy(userId)
                    .build();
            listingPriceHistoryRepository.save(priceHistory);
            log.info("Created price history entry for listing ID: {} (old: {}, new: {})",
                    listingId, oldPrice, updatedListing.getPrice());

            try {
                priceChangeNotificationService.notifyBookmarkUsersAboutPriceChange(
                        updatedListing.getListingId(),
                        updatedListing.getName(),
                        oldPrice,
                        updatedListing.getPrice(),
                        userId);
            } catch (Exception e) {
                log.error("Failed to fan-out price-change notifications for listing {}: {}",
                        updatedListing.getListingId(), e.getMessage(), e);
            }
        }

        if (request.getContent() != null) {
            listing.setContent(request.getContent());
        }

        // Save listing again if content changed (or just once at the end)
        updatedListing = listingRepository.save(listing);

        // Update Listing Media if provided
        List<UUID> mediaIds = request.getMediaIds();

        if (mediaIds != null) {
            var existingMediaList = listingMediaRepository.findByListingId(updatedListing.getListingId());

            // Remove media no longer selected
            for (ListingMedia existingMedia : existingMediaList) {
                if (!mediaIds.contains(existingMedia.getPropertyMediaId())) {
                    listingMediaRepository.deleteById(existingMedia.getListingMediaId());
                }
            }

            // Update remaining or add new
            for (int i = 0; i < mediaIds.size(); i++) {
                UUID mediaId = mediaIds.get(i);
                boolean isPrimary = mediaId.equals(request.getPrimaryMediaId());
                // Primary media always gets display_order 0; others follow list index
                int displayOrder = isPrimary ? 0 : i;

                Listing finalUpdatedListing = updatedListing;
                existingMediaList.stream()
                        .filter(m -> m.getPropertyMediaId().equals(mediaId))
                        .findFirst()
                        .ifPresentOrElse(
                                existing -> {
                                    existing.updateDisplayOrder(displayOrder);
                                    if (isPrimary) {
                                        existing.markAsPrimary();
                                    } else {
                                        existing.removePrimary();
                                    }
                                    listingMediaRepository.save(existing);
                                },
                                () -> {
                                    ListingMedia newMedia = ListingMedia.create(
                                            finalUpdatedListing.getListingId(), mediaId, displayOrder, isPrimary);
                                    listingMediaRepository.save(newMedia);
                                }
                        );
            }
            log.info("Updated media for listing ID: {} ({} total items)", listingId, mediaIds.size());
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
     * Get managed listings with pagination, search, and sort.
     * Returns listings where the user is either the listing creator OR the property owner.
     *
     * @param userId   the user ID
     * @param criteria search criteria
     * @param pageable pagination info
     * @return page of user's listings
     */
    @Transactional(readOnly = true)
    public Page<ListingResponse> getManagedListings(
            UUID userId, ManagedListingSearchCriteria criteria, Pageable pageable) {
        log.info("Fetching managed listings for user ID: {}", userId);

        Specification<Listing> spec = buildManagedListingSpec(userId, criteria);

        // Handle sorting if specified
        Pageable effectivePageable = pageable;
        if (criteria.getSortBy() != null && !criteria.getSortBy().isBlank()) {
            Sort sort = switch (criteria.getSortBy()) {
                case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
                case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
                case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
                default -> Sort.by(Sort.Direction.DESC, "createdAt");
            };
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        } else if (pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Listing> listings = listingRepository.findAll(spec, effectivePageable);

        return listings.map(listing -> {
            ListingResponse response = listingMapper.toListingResponse(listing);
            // If thumbnail is null, fetch it from repository
            if (response.getThumbnail() == null) {
                listingRepository.findThumbnailByListingId(listing.getListingId())
                        .ifPresent(response::setThumbnail);
            }
            return response;
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
    Property property = propertyRepository.findById(request.getPropertyId())
        .orElseThrow(() -> {
          log.error("Property not found with ID: {}", request.getPropertyId());
          return new ResourceNotFoundException("Property", request.getPropertyId());
        });

    // Build listing entity in DRAFT status initially
    Listing listing = Listing.builder()
        .propertyId(request.getPropertyId())
        .userId(userId)
        .listingType(request.getListingType())
        .name(request.getName())
        .slug("placeholder-" + UUID.randomUUID()) // temporary unique slug; updated after persist
        .price(request.getPrice())
        .minPrice(request.getMinPrice())
        .maxPrice(request.getMaxPrice())
        .isNegotiable(request.getIsNegotiable() != null ? request.getIsNegotiable() : false)
        .availableFrom(request.getAvailableFrom())
        .content(request.getContent())
        .status(ListingStatus.DRAFT)
        .build();

    // Handle immediate publication if requested
    if (Boolean.TRUE.equals(request.getShouldPublish())) {
      validatePropertyCanPublishListing(property, request.getListingType(), request.getAvailableFrom());

      // 2. Verify no other published listing of same type exists for this
      // user/property
      boolean duplicateExists = listingRepository.existsByPropertyIdAndListingTypeAndStatusAndUserId(
          property.getPropertyId(), request.getListingType(), ListingStatus.PUBLISHED, userId);
      if (duplicateExists) {
        log.error("Listing creator {} already has a published {} listing for property {}",
            userId, request.getListingType(), property.getPropertyId());
        throw new BusinessConflictException(String.format(
            "A published listing of type %s already exists for this property and user.",
            request.getListingType().name()), "ERROR_DUPLICATE_LISTING_PUBLISH");
      }

      consumeListingQuotaIfFirstPublish(listing, userId);
      listing.publish();
    }

    // Persist first so that the real listingId is generated by the DB/Hibernate
    Listing savedListing = listingRepository.save(listing);

    // Now generate the proper SEO slug using the real persisted ID
    String slug = com.sep.realvista.shared.util.ShortIdUtils.generateSlug(
        request.getName(), savedListing.getListingId());
    savedListing.updateSlug(slug);
    savedListing = listingRepository.save(savedListing);

            // User is either creator or property owner.
            // When the owner views via isOwner, exclude listings whose creator (agent) has been deleted.
            var propertyJoin = root.join("property", JoinType.LEFT);
            var userJoin = root.join("user", JoinType.LEFT);
            Predicate isCreator = cb.equal(root.get("userId"), userId);
            Predicate isOwner = cb.equal(propertyJoin.get("ownerId"), userId);
            Predicate creatorNotDeleted = cb.isFalse(userJoin.get("deleted"));
            Predicate isAgentCreated = cb.and(isOwner, cb.notEqual(root.get("userId"), userId), creatorNotDeleted);

            String createdBy = criteria != null ? criteria.getCreatedBy() : null;
            if ("SELF".equalsIgnoreCase(createdBy)) {
                predicates.add(isCreator);
            } else if ("AGENT".equalsIgnoreCase(createdBy)) {
                predicates.add(isAgentCreated);
            } else {
                predicates.add(cb.or(isCreator, cb.and(isOwner, creatorNotDeleted)));
            }

    // 3. Persist media relationships if provided
    List<UUID> mediaIds = request.getMediaIds();
    if (mediaIds != null && !mediaIds.isEmpty()) {
      for (int displayOrder = 0; displayOrder < mediaIds.size(); displayOrder++) {
        UUID mediaId = mediaIds.get(displayOrder);
        if (mediaId == null) {
          continue; // skip null IDs sent from client
        }
        boolean isPrimary = mediaId.equals(request.getPrimaryMediaId());
        ListingMedia listingMedia = ListingMedia.create(
            savedListing.getListingId(), mediaId, displayOrder, isPrimary);
        listingMediaRepository.save(listingMedia);
      }
      log.info("Saved {} media records for listing ID: {}", mediaIds.size(), savedListing.getListingId());
    }

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
   * @throws IllegalStateException     if user is not the listing creator or
   *                                   property owner
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
    if (request.getName() != null && !listing.getName().equals(request.getName())) {
      listing.updateSlug(com.sep.realvista.shared.util.ShortIdUtils.generateSlug(
          request.getName(), listingId));
      listing.setName(request.getName());
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

    if (request.getAvailableFrom() != null) {
      listing.setAvailableFrom(request.getAvailableFrom());
    }

    // Update listing type if requested and allowed
    if (request.getListingType() != null && !listing.getListingType().equals(request.getListingType())) {
      if (listing.getStatus() == ListingStatus.PUBLISHED
          || listing.getStatus() == ListingStatus.SOLD
          || listing.getStatus() == ListingStatus.RENTED) {
        log.error("Cannot change listing type for listing ID: {} in status: {}",
            listingId, listing.getStatus());
        throw new BusinessConflictException(
            "Cannot change listing type for a listing that is Published, Sold, or Rented",
            "ERROR_FORBIDDEN_TYPE_CHANGE");
      }
      listing.setListingType(request.getListingType());
    }

    if (listing.getStatus() == ListingStatus.PUBLISHED) {
      Property property = propertyRepository.findById(listing.getPropertyId())
          .orElseThrow(() -> new ResourceNotFoundException("Property", listing.getPropertyId()));
      validatePropertyCanPublishListing(property, listing.getListingType(), listing.getAvailableFrom());
    }

    // Save listing
    Listing updatedListing = listingRepository.save(listing);

    // Create price history entry if price changed
    if (priceChanged) {
      ListingPriceHistory priceHistory = ListingPriceHistory.builder()
          .listingId(updatedListing.getListingId())
          .price(updatedListing.getPrice())
          .minPrice(updatedListing.getMinPrice())
          .maxPrice(updatedListing.getMaxPrice())
          .changedBy(userId)
          .build();
      listingPriceHistoryRepository.save(priceHistory);
      log.info("Created price history entry for listing ID: {} (old: {}, new: {})",
          listingId, oldPrice, updatedListing.getPrice());
    }

    if (request.getContent() != null) {
      listing.setContent(request.getContent());
    }

    // Save listing again if content changed (or just once at the end)
    updatedListing = listingRepository.save(listing);

    // Update Listing Media if provided
    List<UUID> mediaIds = request.getMediaIds();

    if (mediaIds != null) {
      var existingMediaList = listingMediaRepository.findByListingId(updatedListing.getListingId());

      // Remove media no longer selected
      for (ListingMedia existingMedia : existingMediaList) {
        if (!mediaIds.contains(existingMedia.getPropertyMediaId())) {
          listingMediaRepository.deleteById(existingMedia.getListingMediaId());
        }
      }

      // Update remaining or add new
      for (int i = 0; i < mediaIds.size(); i++) {
        UUID mediaId = mediaIds.get(i);
        boolean isPrimary = mediaId.equals(request.getPrimaryMediaId());
        // Primary media always gets display_order 0; others follow list index
        int displayOrder = isPrimary ? 0 : i;

        Listing finalUpdatedListing = updatedListing;
        existingMediaList.stream()
            .filter(m -> m.getPropertyMediaId().equals(mediaId))
            .findFirst()
            .ifPresentOrElse(
                existing -> {
                  existing.updateDisplayOrder(displayOrder);
                  if (isPrimary) {
                    existing.markAsPrimary();
                  } else {
                    existing.removePrimary();
                  }
                  listingMediaRepository.save(existing);
                },
                () -> {
                  ListingMedia newMedia = ListingMedia.create(
                      finalUpdatedListing.getListingId(), mediaId, displayOrder, isPrimary);
                  listingMediaRepository.save(newMedia);
                });
      }
      log.info("Updated media for listing ID: {} ({} total items)", listingId, mediaIds.size());
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
   * @throws IllegalStateException     if user is not the listing creator or
   *                                   property owner
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
   * Get managed listings with pagination, search, and sort.
   * Returns listings where the user is either the listing creator OR the property
   * owner.
   *
   * @param userId   the user ID
   * @param criteria search criteria
   * @param pageable pagination info
   * @return page of user's listings
   */
  @Transactional(readOnly = true)
  public Page<ListingResponse> getManagedListings(
      UUID userId, ManagedListingSearchCriteria criteria, Pageable pageable) {
    log.info("Fetching managed listings for user ID: {}", userId);

    Specification<Listing> spec = buildManagedListingSpec(userId, criteria);

    // Handle sorting if specified
    Pageable effectivePageable = pageable;
    if (criteria.getSortBy() != null && !criteria.getSortBy().isBlank()) {
      Sort sort = switch (criteria.getSortBy()) {
        case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
        case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
        case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
        default -> Sort.by(Sort.Direction.DESC, "createdAt");
      };
      effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    } else if (pageable.getSort().isUnsorted()) {
      effectivePageable = PageRequest.of(pageable.getPageNumber(),
          pageable.getPageSize(),
          Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    Page<Listing> listings = listingRepository.findAll(spec, effectivePageable);

    return listings.map(listing -> {
      ListingResponse response = listingMapper.toListingResponse(listing);
      // If thumbnail is null, fetch it from repository
      if (response.getThumbnail() == null) {
        listingRepository.findThumbnailByListingId(listing.getListingId())
            .ifPresent(response::setThumbnail);
      }
      return response;
    });
  }

  /**
   * Get summary counts for managed listings.
   *
   * @param userId the user ID
   * @return counts of ALL, RENT, and SALE listings
   */
  @Transactional(readOnly = true)
  public ManagedListingSummaryDTO getManagedListingSummary(UUID userId) {
    log.info("Fetching managed listings summary for user ID: {}", userId);

    List<Listing> allListings = listingRepository.findByUserIdOrPropertyOwnerId(userId);
    LocalDate currentMonthStartDate = LocalDate.now().withDayOfMonth(1);
    LocalDate previousMonthStartDate = currentMonthStartDate.minusMonths(1);
    LocalDate nextMonthStartDate = currentMonthStartDate.plusMonths(1);
    LocalDateTime currentMonthStart = currentMonthStartDate.atStartOfDay();
    LocalDateTime previousMonthStart = previousMonthStartDate.atStartOfDay();
    LocalDateTime nextMonthStart = nextMonthStartDate.atStartOfDay();

    long total = allListings.size();
    long rent = allListings.stream().filter(l -> ListingType.RENT.equals(l.getListingType())).count();
    long sale = allListings.stream().filter(l -> ListingType.SALE.equals(l.getListingType())).count();
    long currentMonthAll = listingRepository.countByUserIdOrPropertyOwnerIdAndCreatedAtBetween(
        userId, currentMonthStart, nextMonthStart);
    long previousAll = listingRepository.countByUserIdOrPropertyOwnerIdAndCreatedAtBetween(
        userId, previousMonthStart, currentMonthStart);
    long currentMonthRent = listingRepository.countByUserIdOrPropertyOwnerIdAndListingTypeAndCreatedAtBetween(
        userId, ListingType.RENT, currentMonthStart, nextMonthStart);
    long previousRent = listingRepository.countByUserIdOrPropertyOwnerIdAndListingTypeAndCreatedAtBetween(
        userId, ListingType.RENT, previousMonthStart, currentMonthStart);
    long currentMonthSale = listingRepository.countByUserIdOrPropertyOwnerIdAndListingTypeAndCreatedAtBetween(
        userId, ListingType.SALE, currentMonthStart, nextMonthStart);
    long previousSale = listingRepository.countByUserIdOrPropertyOwnerIdAndListingTypeAndCreatedAtBetween(
        userId, ListingType.SALE, previousMonthStart, currentMonthStart);

    return ManagedListingSummaryDTO.builder()
        .all(total)
        .rent(rent)
        .sale(sale)
        .currentMonthAll(currentMonthAll)
        .currentMonthRent(currentMonthRent)
        .currentMonthSale(currentMonthSale)
        .previousAll(previousAll)
        .previousRent(previousRent)
        .previousSale(previousSale)
        .build();
  }

  private Specification<Listing> buildManagedListingSpec(UUID userId, ManagedListingSearchCriteria criteria) {
    return (root, query, cb) -> {
      assert query != null;
      query.distinct(true);
      List<Predicate> predicates = new ArrayList<>();

      // Exclude soft-deleted listings
      predicates.add(cb.equal(root.get("deleted"), false));

      // User is either creator or property owner.
      // When the owner views via isOwner, exclude listings whose creator (agent) has
      // been deleted.
      var propertyJoin = root.join("property", JoinType.LEFT);
      var userJoin = root.join("user", JoinType.LEFT);
      Predicate isCreator = cb.equal(root.get("userId"), userId);
      Predicate isOwner = cb.equal(propertyJoin.get("ownerId"), userId);
      Predicate creatorNotDeleted = cb.isFalse(userJoin.get("deleted"));
      predicates.add(cb.or(isCreator, cb.and(isOwner, creatorNotDeleted)));

      if (criteria != null) {
        // Listing Type
        if (criteria.getListingType() != null
            && !criteria.getListingType().isBlank()
            && !criteria.getListingType().equalsIgnoreCase("ALL")) {
          try {
            ListingType type = ListingType.valueOf(criteria.getListingType().toUpperCase());
            predicates.add(cb.equal(root.get("listingType"), type));
          } catch (IllegalArgumentException e) {
            log.warn("Invalid listing type in search: {}", criteria.getListingType());
          }
        }

        // Status
        if (criteria.getStatus() != null
            && !criteria.getStatus().isBlank()
            && !criteria.getStatus().equalsIgnoreCase("ALL")) {
          try {
            ListingStatus status = ListingStatus.valueOf(criteria.getStatus().toUpperCase());
            predicates.add(cb.equal(root.get("status"), status));
          } catch (IllegalArgumentException e) {
            log.warn("Invalid listing status in search: {}", criteria.getStatus());
          }
        }

        // Search query (name or address)
        if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
          String searchStr = "%" + criteria.getSearch().toLowerCase() + "%";
          Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchStr);

          var locationJoin = propertyJoin.join("location", JoinType.LEFT);
          Predicate addressMatch = cb.like(cb.lower(propertyJoin.get("streetAddress")), searchStr);
          Predicate locMatch = cb.like(cb.lower(locationJoin.get("name")), searchStr);

          predicates.add(cb.or(nameMatch, addressMatch, locMatch));
        }

        // Filter by property ID
        if (criteria.getPropertyId() != null) {
          predicates.add(cb.equal(propertyJoin.get("propertyId"), criteria.getPropertyId()));
        }
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * Submit listing for review (DRAFT -> PENDING).
   *
   * @param listingId the listing ID
   * @param userId    the user ID performing the action
   * @return updated listing response
   * @throws ResourceNotFoundException if listing not found
   * @throws IllegalStateException     if user is not the listing creator or
   *                                   property owner
   *                                   , or listing is not in DRAFT status
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

    // Verify associated property is available (active)
    Property property = propertyRepository.findById(listing.getPropertyId())
        .orElseThrow(() -> new ResourceNotFoundException("Property", listing.getPropertyId()));

    validatePropertyCanPublishListing(property, listing.getListingType(), listing.getAvailableFrom());

    // Verify no other published listing of the same type exists for the listing
    // creator and property
    boolean duplicateExists = listingRepository.existsByPropertyIdAndListingTypeAndStatusAndUserId(
        listing.getPropertyId(), listing.getListingType(), ListingStatus.PUBLISHED, listing.getUserId());

    if (duplicateExists) {
      log.error("Listing creator {} already has a published {} listing for property {}",
          listing.getUserId(), listing.getListingType(), listing.getPropertyId());
      throw new BusinessConflictException(String.format(
          "A published listing of type %s already exists for this property and user.",
          listing.getListingType().name()), "ERROR_DUPLICATE_LISTING_PUBLISH");
    }

    boolean isFirstPublish = !Boolean.TRUE.equals(listing.getHasBeenPublished());
    if (isFirstPublish) {
      consumeListingQuotaIfFirstPublish(listing, userId);
    }
    listing.publish();
    Listing updatedListing = listingRepository.save(listing);

    log.info("Successfully published listing ID: {}", listingId);

    return listingMapper.toListingResponse(updatedListing);
  }

  private void consumeListingQuotaIfFirstPublish(Listing listing, UUID userId) {
    UserFeatureSubscription sub = userFeatureSubscriptionRepository
        .findActiveByUserIdAndFeatureTypeForUpdate(userId, FeatureType.LISTING)
        .stream()
        .filter(UserFeatureSubscription::isUsable)
        .findFirst()
        .orElseThrow(() -> new InsufficientQuotaException(
            "No active listing subscription with available quota"));
    sub.useQuota(1);
    userFeatureSubscriptionRepository.save(sub);
  }

  private void validatePropertyCanPublishListing(Property property, ListingType listingType,
      LocalDate availableFrom) {
    if (property.getStatus() == PropertyStatus.AVAILABLE) {
      return;
    }

    if (property.getStatus() == PropertyStatus.RENTED && listingType == ListingType.SALE) {
      return;
    }

    if (property.getStatus() == PropertyStatus.RENTED && listingType == ListingType.RENT) {
      if (!Boolean.TRUE.equals(property.getAllowRentListingWhenRented())) {
        throw new BusinessConflictException(
            "Rent listings are disabled while this property is rented",
            "ERROR_RENT_LISTING_DISABLED_WHILE_RENTED");
      }
      if (availableFrom == null) {
        throw new BusinessConflictException(
            "Available from date is required for rent listings on rented properties",
            "ERROR_AVAILABLE_FROM_REQUIRED_FOR_RENTED_PROPERTY");
      }
      LocalDate today = LocalDate.now();
      if (!availableFrom.isAfter(today)) {
        throw new BusinessConflictException(
            "Available from date must be in the future for rent listings on rented properties",
            "ERROR_AVAILABLE_FROM_MUST_BE_FUTURE_FOR_RENTED_PROPERTY");
      }
      LocalDate currentLeaseEndDate = leaseAgreementRepository
          .findActiveLeasesByPropertyId(property.getPropertyId())
          .stream()
          .map(LeaseAgreement::getLeaseEndDate)
          .filter(java.util.Objects::nonNull)
          .max(LocalDate::compareTo)
          .orElse(null);
      if (currentLeaseEndDate != null && availableFrom.isBefore(currentLeaseEndDate.plusDays(1))) {
        throw new BusinessConflictException(
            "Available from date must be after the current lease end date",
            "ERROR_AVAILABLE_FROM_BEFORE_CURRENT_LEASE_END");
      }
      return;
    }

    log.error("Cannot publish listing: Associated property {} is in status {}",
        property.getPropertyId(), property.getStatus());
    throw new BusinessConflictException(
        "Associated property is not in active state (status: " + property.getStatus() + ")",
        "ERROR_PROPERTY_NOT_AVAILABLE",
        new Object[] {property.getStatus()});
  }

  /**
   * Unpublish listing (PUBLISHED -> DRAFT).
   *
   * @param listingId the listing ID
   * @param userId    the user ID performing the action
   * @return updated listing response
   * @throws ResourceNotFoundException if listing not found
   * @throws IllegalStateException     if user is not the listing creator or
   *                                   property owner,
   *                                   or listing is not published
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

    // Notify bookers about temporary unpublishing
    appointmentApplicationService.notifyUnpublishedListing(updatedListing);

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
   * @throws IllegalStateException     if user is not the listing creator or
   *                                   property owner, listing is not SALE type,
   *                                   or not published
   */
  @CacheEvict(value = "listings", allEntries = true)
  public ListingResponse markAsSold(UUID listingId, UUID userId) {
    log.info("Marking listing ID: {} as sold by user ID: {}", listingId, userId);

    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

    verifyListingModificationAuthorization(listing, userId, "mark as sold");

    listing.markAsSold(userId);

    // 1. Cancel all active appointments
    String reason = "Bất động sản không còn trống (đã bán/cho thuê).";
    appointmentApplicationService.cancelActiveAppointmentsByListingId(listingId, listing.getUserId(), reason);

    // 2. Synchronize all other listings and the property
    closeAllListingsAndProperty(listing, PropertyStatus.SOLD, userId);

    Listing updatedListing = listingRepository.save(listing);
    syncEngagementListingIdForAgentClose(updatedListing, userId);
    log.info("Successfully marked listing ID: {} and all related listings as sold", listingId);

    return listingMapper.toListingResponse(updatedListing);
  }

  @CacheEvict(value = "listings", allEntries = true)
  public ListingResponse markAsRented(UUID listingId, UUID userId) {
    log.info("Marking listing ID: {} as rented by user ID: {}", listingId, userId);

    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

    verifyListingModificationAuthorization(listing, userId, "mark as rented");

    listing.markAsRented(userId);

    // 1. Cancel all active appointments
    String reason = "Bất động sản không còn trống (đã bán/cho thuê).";
    appointmentApplicationService.cancelActiveAppointmentsByListingId(listingId, listing.getUserId(), reason);

    // 2. Synchronize all other listings and the property
    closeAllListingsAndProperty(listing, PropertyStatus.RENTED, userId);

    Listing updatedListing = listingRepository.save(listing);
    syncEngagementListingIdForAgentClose(updatedListing, userId);
    log.info("Successfully marked listing ID: {} and all related listings as rented", listingId);

    return listingMapper.toListingResponse(updatedListing);
  }

  /**
   * When the listing creator marks sold/rented, attach this listing to hired
   * engagements
   * for the same property and agent. Skipped when the actor is only the property
   * owner.
   */
  private void syncEngagementListingIdForAgentClose(Listing listing, UUID actorUserId) {
    if (!actorUserId.equals(listing.getUserId())) {
      return;
    }
    UUID propertyId = listing.getPropertyId();
    if (propertyId == null) {
      return;
    }
    List<Engagement> engagements = engagementRepository.findByListingIdInOrPropertyIdIn(
        Collections.emptyList(), List.of(propertyId));
    UUID listingId = listing.getListingId();
    UUID agentUserId = listing.getUserId();

    for (Engagement engagement : engagements) {
      if (engagement.getPropertyId() == null || !propertyId.equals(engagement.getPropertyId())) {
        continue;
      }
      EngagementStatus status = engagement.getStatus();
      if (status != EngagementStatus.ACCEPTED && status != EngagementStatus.FINISHED) {
        continue;
      }
      EngagementType type = engagement.getEngagementType();
      boolean agentMatches = (type == EngagementType.AGENT_PROPOSAL
          && agentUserId.equals(engagement.getInitiatorId()))
          || (type == EngagementType.OWNER_INVITATION
              && agentUserId.equals(engagement.getReceiverId()));
      if (!agentMatches) {
        continue;
      }
      engagement.linkToListing(listingId);
      engagementRepository.save(engagement);
    }
  }

  private void closeAllListingsAndProperty(Listing triggeringListing, PropertyStatus targetPropertyStatus,
      UUID closedByUserId) {
    UUID propertyId = triggeringListing.getPropertyId();
    // 1. Update the property status
    Property property = propertyRepository.findById(propertyId)
        .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

    if (targetPropertyStatus == PropertyStatus.SOLD) {
      property.markAsSold();
    } else if (targetPropertyStatus == PropertyStatus.RENTED) {
      property.markAsRented();
    }
    propertyRepository.save(property);

    // 2. Synchronize associated listings. Sold properties close everything;
    // rented properties keep sale listings and only draft rent listings when the
    // toggle is off.
    List<Listing> listings = listingRepository.findByPropertyId(propertyId);
    Set<UUID> usersToNotify = new HashSet<>();

    // Always notify the user who triggered the action and the property owner
    usersToNotify.add(triggeringListing.getUserId());
    usersToNotify.add(property.getOwnerId());

    for (Listing l : listings) {
      // Skip the one that triggered this action or those already in a terminal state
      if (l.getListingId().equals(triggeringListing.getListingId())
          || l.getStatus() == ListingStatus.SOLD
          || l.getStatus() == ListingStatus.RENTED) {
        continue;
      }

      if (l.getStatus() == ListingStatus.PUBLISHED || l.getStatus() == ListingStatus.PENDING) {
        boolean shouldUpdate = targetPropertyStatus == PropertyStatus.SOLD
            || (targetPropertyStatus == PropertyStatus.RENTED
                && l.getListingType() == ListingType.RENT
                && !Boolean.TRUE.equals(property.getAllowRentListingWhenRented()));
        if (!shouldUpdate) {
          continue;
        }

        if (targetPropertyStatus == PropertyStatus.SOLD && l.getStatus() != ListingStatus.PUBLISHED) {
          continue;
        }

        // Keep track of users whose listings were actually updated
        usersToNotify.add(l.getUserId());

        if (targetPropertyStatus == PropertyStatus.SOLD) {
          l.markAsSoldDueToPropertyClosure(closedByUserId);
        } else if (targetPropertyStatus == PropertyStatus.RENTED) {
          l.moveToDraft();
        }
        listingRepository.save(l);

        // Trigger immediate appointment cancellation for closed listings
        String cancellationReason = "Property no longer available (sold/rented).";
        appointmentApplicationService.cancelActiveAppointmentsByListingId(
            l.getListingId(), property.getOwnerId(), cancellationReason);
      }
    }

    // 3. Dispatch notifications
    sendClosingNotifications(property, triggeringListing, usersToNotify, targetPropertyStatus);
    sendOwnerEngagementReviewReminder(property, targetPropertyStatus);
  }

  private void sendClosingNotifications(Property property, Listing triggeringListing, Set<UUID> userIds,
      PropertyStatus status) {
    log.info("Starting closing notifications for property {} - Status: {} | Recipients: {}",
        property.getPropertyId(), status, userIds.size());

    EventType eventType = status == PropertyStatus.SOLD
        ? EventType.LISTING_SOLD
        : EventType.LISTING_RENTED;

    for (UUID userId : userIds) {
      try {
        String lang = getLanguageForUser(userId);
        String title = notificationMessageService.getMessage(
            status == PropertyStatus.SOLD ? "PROPERTY_SOLD_TITLE" : "PROPERTY_RENTED_TITLE", lang);
        String message = notificationMessageService.getMessage(
            status == PropertyStatus.SOLD ? "PROPERTY_SOLD_MESSAGE" : "PROPERTY_RENTED_MESSAGE",
            lang, property.getStreetAddress());

        userRepository.findById(userId).ifPresent(user -> {
          notificationApplicationService.sendNotification(SendNotificationRequest.builder()
              .userId(user.getUserId())
              .userEmail(user.getEmail().getValue())
              .title(title)
              .message(message)
              .eventType(eventType)
              .entityType(EntityType.PROPERTY)
              .entityId(property.getPropertyId())
              .build());
        });
      } catch (Exception e) {
        log.error("Failed to send closing notification to user {}: {}",
            userId, e.getMessage());
      }
    }

    /**
     * One label per THREE_D media on the listing, ordered like the gallery. Blank string = unnamed room.
     */
    private List<String> buildThreeDRoomNames(List<ListingMedia> listingMedias) {
        if (listingMedias == null || listingMedias.isEmpty()) {
            return List.of();
        }
        return listingMedias.stream()
                .filter(lm -> lm.getPropertyMedia() != null && !Boolean.TRUE.equals(lm.getDeleted()))
                .filter(lm -> lm.getPropertyMedia().getMediaType() == MediaType.THREE_D)
                .sorted(Comparator.comparing(
                        ListingMedia::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(lm -> extractThreeDRoomLabel(lm.getPropertyMedia().getMetadata()))
                .collect(Collectors.toList());
    }

    private static String extractThreeDRoomLabel(Map<String, Object> metadata) {
        if (metadata == null) {
            return "";
        }
        Object roomName = metadata.get("room_name");
        if (roomName instanceof String s) {
            return s.trim();
        }
        return "";
    }
}
