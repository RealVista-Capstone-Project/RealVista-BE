package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.CostBreakdownDTO;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.dto.PriceChangeType;
import com.sep.realvista.application.listing.dto.PriceHistoryDTO;
import com.sep.realvista.application.listing.dto.PriceHistoryResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.analytics.ListingPriceHistory;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingPriceHistoryRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.similarity.SimilarListing;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
        private final PropertyAttributeValueJpaRepository propertyAttributeValueJpaRepository;
        private final PropertyAmenityRepository propertyAmenityRepository;
        private final ListingMapper listingMapper;
        private final CostBreakdownService costBreakdownService;
        private final BookmarkRepository bookmarkRepository;

        /**
         * Get listing detail by ID.
         * Returns complete listing information including media, property, location,
         * type, category, agent/owner, and attributes.
         *
         * @param listingId the listing ID
         * @return complete listing detail response
         * @throws ResourceNotFoundException if listing not found
         */
        @Cacheable(value = "listings", key = "#listingId + '_' + (#userId != null ? #userId.toString() : 'anon')")
        @Transactional(readOnly = true)
        public ListingDetailResponse getListingDetail(UUID listingId, UUID userId) {
                log.info("Fetching listing detail for ID: {}", listingId);

                // Fetch listing with all associations
                Listing listing = listingRepository.findById(listingId)
                                .orElseThrow(() -> {
                                        log.error("Listing not found with ID: {}", listingId);
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
                List<PropertyAttributeValue> attributeValues = propertyAttributeValueJpaRepository
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

                // Populate isFavorite for authenticated users
                if (userId != null) {
                        boolean isFavorite = bookmarkRepository.existsByUserIdAndListingId(userId, listingId);
                        response.setIsFavorite(isFavorite);
                } else {
                        response.setIsFavorite(false);
                }

                return response;
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
                        log.error("Listing not found with ID: {}", listingId);
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

                List<PropertyAttributeValue> allAttributes = propertyAttributeValueJpaRepository
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
                                        log.error("Listing not found with ID: {}", listingId);
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
}
