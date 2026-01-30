package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
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

import java.util.List;
import java.util.UUID;

/**
 * Application Service for Listing operations.
 * Orchestrates business logic and coordinates between domain and infrastructure layers.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ListingApplicationService {

    private final ListingRepository listingRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyAttributeValueJpaRepository propertyAttributeValueJpaRepository;
    private final ListingMapper listingMapper;

    /**
     * Get listing detail by ID.
     * Returns complete listing information including media, property, location, type, category, agent/owner, and attributes.
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
        List<PropertyAttributeValue> attributeValues =
                propertyAttributeValueJpaRepository.findByPropertyIdWithAttribute(property.getPropertyId());

        // Attach property and user for DTO mapping (read-only, not persisted)
        listing.attachProperty(property);

        log.info("Successfully fetched listing detail for ID: {} with {} attributes",
                listingId, attributeValues.size());

        return listingMapper.toDetailResponseWithMediaAndAttributes(listing, listingMedias, attributeValues);
    }
}
