package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.map.MapSearchRequest;
import com.sep.realvista.application.listing.dto.map.MapSearchResponse;
import com.sep.realvista.application.listing.dto.map.PropertyMapMarker;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.search.MapBounds;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Application Service for map-based property searches.
 * Orchestrates fetching listings within geographical bounds and transforming to map markers.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MapSearchApplicationService {

    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final PropertyAttributeValueJpaRepository propertyAttributeValueJpaRepository;

    /**
     * Search for property listings within map bounds.
     * Returns lightweight map markers for efficient rendering.
     *
     * @param request map search request with bounds and filters
     * @return map search response with markers and metadata
     */
    public MapSearchResponse searchPropertiesOnMap(MapSearchRequest request) {
        log.info("Map search request - bounds: ({},{}) to ({},{}), type: {}, page: {}, size: {}",
                request.getSouthLat(), request.getWestLng(),
                request.getNorthLat(), request.getEastLng(),
                request.getListingType(), request.getPage(), request.getSize());

        // Validate map bounds
        MapBounds bounds = MapBounds.of(
                request.getNorthLat(),
                request.getSouthLat(),
                request.getEastLng(),
                request.getWestLng()
        );

        // Get total count first
        Long totalCount = listingRepository.countPublishedWithinBounds(
                request.getNorthLat(),
                request.getSouthLat(),
                request.getEastLng(),
                request.getWestLng(),
                request.getListingType()
        );

        log.info("Found {} total listings within bounds", totalCount);

        // Calculate pagination parameters (convert 1-indexed page to 0-indexed offset)
        int pageSize = request.getSize();
        int pageNumber = request.getPage();
        int offset = (pageNumber - 1) * pageSize;

        // Fetch listings within bounds with proper pagination
        List<Listing> listings = listingRepository.findPublishedWithinBounds(
                request.getNorthLat(),
                request.getSouthLat(),
                request.getEastLng(),
                request.getWestLng(),
                request.getListingType(),
                pageSize,
                offset
        );

        // Apply additional filters if specified
        List<Listing> filteredListings = applyPriceFilters(listings, request.getMinPrice(), request.getMaxPrice());

        // Transform to map markers
        List<PropertyMapMarker> markers = filteredListings.stream()
                .map(this::convertToMapMarker)
                .filter(Objects::nonNull)
                .toList();

        log.info("Returning {} markers for page {} (filtered from {} total)", markers.size(), pageNumber, totalCount);

        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean isFirst = pageNumber == 1;
        boolean isLast = pageNumber >= totalPages;

        return MapSearchResponse.builder()
                .content(markers)
                .page(pageNumber)
                .size(pageSize)
                .totalElements(totalCount)
                .totalPages(totalPages)
                .first(isFirst)
                .last(isLast)
                .bounds(MapSearchResponse.MapBoundsDTO.builder()
                        .northLat(request.getNorthLat())
                        .southLat(request.getSouthLat())
                        .eastLng(request.getEastLng())
                        .westLng(request.getWestLng())
                        .build())
                .hasMore(!isLast)
                .build();
    }

    /**
     * Apply price range filters to listings.
     */
    private List<Listing> applyPriceFilters(List<Listing> listings, BigDecimal minPrice, BigDecimal maxPrice) {
        return listings.stream()
                .filter(listing -> {
                    if (minPrice != null && listing.getPrice().compareTo(minPrice) < 0) {
                        return false;
                    }
                    if (maxPrice != null && listing.getPrice().compareTo(maxPrice) > 0) {
                        return false;
                    }
                    return true;
                })
                .toList();
    }

    /**
     * Helper to extract integer attribute safely.
     */
    private Integer getIntAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Convert listing to map marker.
     * Fetches associated property, thumbnail, and attributes.
     */
    private PropertyMapMarker convertToMapMarker(Listing listing) {
        // Fetch property
        Property property = propertyRepository.findById(listing.getPropertyId()).orElse(null);
        if (property == null) {
            log.warn("Property not found for listing {}", listing.getListingId());
            return null;
        }

        // Fetch thumbnail
        String thumbnailUrl = null;
        List<ListingMedia> medias = listingMediaRepository
                .findByListingIdOrderByDisplayOrderAsc(listing.getListingId());
        if (!medias.isEmpty() && medias.get(0).getPropertyMedia() != null) {
            thumbnailUrl = medias.get(0).getPropertyMedia().getMediaUrl();
        }

        // Fetch attributes
        List<PropertyAttributeValue> attributeValues = propertyAttributeValueJpaRepository
                .findByPropertyIdWithAttribute(property.getPropertyId());

        Map<String, Object> attributes = new HashMap<>();
        for (PropertyAttributeValue value : attributeValues) {
            if (value.getPropertyAttribute() != null) {
                String attributeName = value.getPropertyAttribute().getName().toLowerCase();
                attributes.put(attributeName, value.getValueNumber() != null
                        ? value.getValueNumber()
                        : value.getValueText());
            }
        }

        return PropertyMapMarker.builder()
                .listingId(listing.getListingId())
                .coordinates(PropertyMapMarker.CoordinatesDTO.builder()
                        .latitude(property.getLatitude())
                        .longitude(property.getLongitude())
                        .build())
                .streetAddress(property.getStreetAddress())
                .price(listing.getPrice())
                .listingType(listing.getListingType())
                .name(listing.getName())
                .thumbnailUrl(thumbnailUrl)
                .bedrooms(getIntAttribute(attributes, "bedrooms"))
                .bathrooms(getIntAttribute(attributes, "bathrooms"))
                .sizeM2(property.getUsableSizeM2())
                .propertyType(property.getPropertyType() != null
                        ? property.getPropertyType().getName()
                        : null)
                .locationName(property.getLocation() != null
                        ? property.getLocation().getName()
                        : null)
                .isFavorite(false) // Implement favorite check based on current user
                .build();
    }
}
