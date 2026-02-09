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

        // Get total count first (with price filter)
        Long totalCount = listingRepository.countPublishedWithinBounds(
                bounds,
                request.getListingType(),
                request.getMinPrice(),
                request.getMaxPrice()
        );

        log.info("Found {} total listings within bounds", totalCount);

        // Calculate pagination parameters (convert 1-indexed page to 0-indexed offset)
        int pageSize = request.getSize();
        int pageNumber = request.getPage();
        int offset = (pageNumber - 1) * pageSize;

        // Fetch listings within bounds with proper pagination and price filtering
        List<Listing> listings = listingRepository.findPublishedWithinBounds(
                bounds,
                request.getListingType(),
                request.getMinPrice(),
                request.getMaxPrice(),
                pageSize,
                offset
        );

        // Transform to map markers
        List<PropertyMapMarker> markers = listings.stream()
                .map(this::convertToMapMarker)
                .filter(Objects::nonNull)
                .toList();

        log.info("Returning {} markers for page {} (filtered from {} total)", markers.size(), pageNumber, totalCount);

        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean isFirst = pageNumber == 1;
        boolean isLast = pageNumber >= totalPages;

        // Build filter metadata
        MapSearchResponse.FilterMetadataDTO filterMetadata = buildFilterMetadata(request);

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
                .filterMetadata(filterMetadata)
                .hasMore(!isLast)
                .build();
    }



    /**
     * Build filter metadata from the request.
     * Note: available_price_range and price_histogram require additional implementation.
     */
    private MapSearchResponse.FilterMetadataDTO buildFilterMetadata(MapSearchRequest request) {
        // Build applied filters
        MapSearchResponse.AppliedFiltersDTO.AppliedFiltersDTOBuilder appliedFiltersBuilder =
                MapSearchResponse.AppliedFiltersDTO.builder();

        if (request.getSearchText() != null) {
            appliedFiltersBuilder.searchText(request.getSearchText());
        }
        if (request.getCategory() != null) {
            appliedFiltersBuilder.category(request.getCategory());
        }
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            appliedFiltersBuilder.priceRange(MapSearchResponse.PriceRangeDTO.builder()
                    .min(request.getMinPrice())
                    .max(request.getMaxPrice())
                    .build());
        }
        if (request.getBedrooms() != null) {
            appliedFiltersBuilder.bedrooms(request.getBedrooms());
        }
        if (request.getBathrooms() != null) {
            appliedFiltersBuilder.bathrooms(request.getBathrooms());
        }
        if (request.getArea() != null) {
            appliedFiltersBuilder.area(request.getArea());
        }
        if (request.getRentalPeriod() != null) {
            appliedFiltersBuilder.rentalPeriod(request.getRentalPeriod());
        }
        if (request.getListingType() != null) {
            appliedFiltersBuilder.listingType(request.getListingType().name());
        }

        return MapSearchResponse.FilterMetadataDTO.builder()
                .appliedFilters(appliedFiltersBuilder.build())
                .availablePriceRange(null)
                .priceHistogram(null)
                .build();
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
