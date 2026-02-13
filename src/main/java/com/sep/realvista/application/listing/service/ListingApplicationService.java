package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.CostBreakdownDTO;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
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
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        private final ListingMapper listingMapper;
        private final CostBreakdownService costBreakdownService;

        /**
         * Get listing detail by ID.
         * Returns complete listing information including media, property, location,
         * type, category, agent/owner, and attributes.
         *
         * @param listingId the listing ID
         * @return complete listing detail response
         * @throws ResourceNotFoundException if listing not found
         */
        @Cacheable(value = "listings", key = "#listingId")
        @Transactional(readOnly = true)
        public ListingDetailResponse getListingDetail(UUID listingId) {
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

                // Fetch property attribute values (bedrooms, bathrooms, amenities, etc.)
                List<PropertyAttributeValue> attributeValues = propertyAttributeValueJpaRepository
                                .findByPropertyIdWithAttribute(property.getPropertyId());

                // Attach property and user for DTO mapping (read-only, not persisted)
                listing.attachProperty(property);

                log.info("Successfully fetched listing detail for ID: {} with {} attributes",
                                listingId, attributeValues.size());

                ListingDetailResponse response = listingMapper.toDetailResponseWithMediaAndAttributes(
                                listing, listingMedias, attributeValues);

                // Calculate and add cost breakdown (only for RENT listings)
                CostBreakdownDTO costBreakdown = costBreakdownService.calculateCostBreakdown(listing);
                response.setCostBreakdown(costBreakdown);

                return response;
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

                        // Look ahead to the next (older) entry for comparison
                        if (i + 1 < historyEntries.size()) {
                                BigDecimal olderPrice = historyEntries.get(i + 1).getPrice();
                                int comparison = entry.getPrice().compareTo(olderPrice);
                                if (comparison > 0) {
                                        changeType = PriceChangeType.INCREASED;
                                } else if (comparison < 0) {
                                        changeType = PriceChangeType.DECREASED;
                                } else {
                                        changeType = PriceChangeType.INITIAL;
                                }

                                priceChange = entry.getPrice().subtract(olderPrice);

                                if (olderPrice.compareTo(BigDecimal.ZERO) > 0) {
                                        priceChangePercent = priceChange
                                                        .divide(olderPrice, 4, RoundingMode.HALF_UP)
                                                        .multiply(BigDecimal.valueOf(100))
                                                        .doubleValue();
                                }
                        } else {
                                // Last entry (oldest in history)
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
