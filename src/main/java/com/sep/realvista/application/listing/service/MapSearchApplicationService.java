package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.map.MapSearchRequest;
import com.sep.realvista.application.listing.dto.map.MapSearchResponse;
import com.sep.realvista.application.listing.dto.map.PropertyMapMarker;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.search.MapBounds;
import com.sep.realvista.domain.listing.search.MapSearchCriteria;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application Service for map-based property searches.
 * Orchestrates fetching listings within geographical bounds and transforming to
 * map markers.
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
    private final BookmarkRepository bookmarkRepository;
    private final ListingBoostRepository listingBoostRepository;
    private final ListingMapper listingMapper;

    /**
     * Search for property listings within map bounds.
     * Returns lightweight map markers for efficient rendering.
     *
     * @param request map search request with bounds and filters
     * @param userId  current user ID (nullable, for isFavorite population)
     * @return map search response with markers and metadata
     */
    public MapSearchResponse searchPropertiesOnMap(MapSearchRequest request, UUID userId) {
        log.info("Map search request - bounds: ({},{}) to ({},{}), type: {}, page: {}, size: {}",
                request.getSouthLat(), request.getWestLng(),
                request.getNorthLat(), request.getEastLng(),
                request.getListingType(), request.getPage(), request.getSize());

        MapBounds bounds = determineBounds(request);
        MapSearchCriteria criteria = buildCriteria(request, bounds);

        Long totalCount = listingRepository.countPublishedWithinBounds(criteria);
        if (totalCount == 0) {
            return buildEmptyResponse(request, bounds);
        }

        List<Listing> listings = listingRepository.findPublishedWithinBounds(criteria);
        Map<UUID, List<PropertyAttributeValue>> attributesByPropertyId = fetchBulkAttributes(listings);
        Set<UUID> bookmarkedIds = fetchBookmarkedIds(listings, userId);
        Map<UUID, List<ListingBoost>> activeBoostsByListingId = fetchActiveBoosts(listings);

        List<PropertyMapMarker> markers = listings.stream()
                .map(l -> convertToMapMarker(l,
                        attributesByPropertyId.getOrDefault(l.getPropertyId(), List.of()),
                        bookmarkedIds.contains(l.getListingId()),
                        activeBoostsByListingId.getOrDefault(l.getListingId(), List.of())))
                .filter(Objects::nonNull)
                .toList();

        return buildSearchResponse(request, markers, totalCount, criteria.getPage(), criteria.getSize());
    }

    private MapBounds determineBounds(MapSearchRequest request) {
        boolean hasSearchText = request.getSearchText() != null && !request.getSearchText().isEmpty();
        boolean hasRequestBounds = request.getNorthLat() != null && request.getSouthLat() != null
                && request.getEastLng() != null && request.getWestLng() != null;

        if (hasSearchText) {
            return MapBounds.of(new BigDecimal("90"), new BigDecimal("-90"),
                    new BigDecimal("180"), new BigDecimal("-180"));
        } else if (hasRequestBounds) {
            return MapBounds.of(request.getNorthLat(), request.getSouthLat(),
                    request.getEastLng(), request.getWestLng());
        } else {
            return MapBounds.of(new BigDecimal("90"), new BigDecimal("-90"),
                    new BigDecimal("180"), new BigDecimal("-180"));
        }
    }

    private MapSearchCriteria buildCriteria(MapSearchRequest request, MapBounds bounds) {
        return MapSearchCriteria.builder()
                .bounds(bounds)
                .listingType(request.getListingType())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .searchText(request.getSearchText())
                .propertyCategory(request.getPropertyCategory())
                .propertyType(request.getPropertyType())
                .categories(request.getPropertyCategory() != null
                        ? Arrays.asList(request.getPropertyCategory().split(","))
                        : null)
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .area(request.getArea())
                .sortBy(request.getSortBy())
                .sortDirection(request.getSortDirection())
                .page(request.getPage() < 1 ? 1 : request.getPage())
                .size(request.getSize())
                .build();
    }

    private Map<UUID, List<PropertyAttributeValue>> fetchBulkAttributes(List<Listing> listings) {
        List<UUID> propertyIds = listings.stream()
                .map(Listing::getPropertyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (propertyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return propertyAttributeValueJpaRepository.findAllAttributesByPropertyIds(propertyIds)
                .stream()
                .collect(Collectors.groupingBy(PropertyAttributeValue::getPropertyId));
    }

    private Set<UUID> fetchBookmarkedIds(List<Listing> listings, UUID userId) {
        if (userId == null || listings.isEmpty()) {
            return Collections.emptySet();
        }
        List<UUID> listingIds = listings.stream()
                .map(Listing::getListingId)
                .collect(Collectors.toList());
        return bookmarkRepository.findBookmarkedListingIds(userId, listingIds);
    }

    private Map<UUID, List<ListingBoost>> fetchActiveBoosts(List<Listing> listings) {
        if (listings.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UUID> listingIds = listings.stream()
                .map(Listing::getListingId)
                .collect(Collectors.toList());
        return listingBoostRepository.findAllActiveByListingIds(listingIds, java.time.LocalDate.now())
                .stream()
                .collect(Collectors.groupingBy(ListingBoost::getListingId));
    }

    private MapSearchResponse buildEmptyResponse(MapSearchRequest request, MapBounds bounds) {
        return MapSearchResponse.builder()
                .content(List.of())
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .bounds(MapSearchResponse.MapBoundsDTO.builder()
                        .northLat(bounds.northLat())
                        .southLat(bounds.southLat())
                        .eastLng(bounds.eastLng())
                        .westLng(bounds.westLng())
                        .build())
                .filterMetadata(buildFilterMetadata(request))
                .build();
    }

    private MapSearchResponse buildSearchResponse(MapSearchRequest request, List<PropertyMapMarker> markers,
            Long totalCount, int pageNumber, int pageSize) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        return MapSearchResponse.builder()
                .content(markers)
                .page(pageNumber)
                .size(pageSize)
                .totalElements(totalCount)
                .totalPages(totalPages)
                .first(pageNumber == 1)
                .last(pageNumber >= totalPages)
                .bounds(MapSearchResponse.MapBoundsDTO.builder()
                        .northLat(request.getNorthLat())
                        .southLat(request.getSouthLat())
                        .eastLng(request.getEastLng())
                        .westLng(request.getWestLng())
                        .build())
                .filterMetadata(buildFilterMetadata(request))
                .build();
    }

    /**
     * Build filter metadata from the request.
     * Note: available_price_range and price_histogram require additional
     * implementation.
     */
    private MapSearchResponse.FilterMetadataDTO buildFilterMetadata(MapSearchRequest request) {
        // Build applied filters
        var appliedFiltersBuilder = MapSearchResponse.AppliedFiltersDTO.builder();

        if (request.getSearchText() != null) {
            appliedFiltersBuilder.searchText(request.getSearchText());
        }
        if (request.getPropertyCategory() != null) {
            appliedFiltersBuilder.propertyCategory(request.getPropertyCategory());
        }
        if (request.getPropertyType() != null) {
            appliedFiltersBuilder.propertyType(request.getPropertyType());
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
     * Convert listing to map marker using pre-fetched attribute values.
     */
    private PropertyMapMarker convertToMapMarker(Listing listing,
            List<PropertyAttributeValue> attrValues, boolean isFavorite, List<ListingBoost> boosts) {
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
        if (!medias.isEmpty() && medias.getFirst().getPropertyMedia() != null) {
            thumbnailUrl = medias.getFirst().getPropertyMedia().getMediaUrl();
        }

        return PropertyMapMarker.builder()
                .listingId(listing.getListingId())
                .slug(listing.getSlug())
                .coordinates(PropertyMapMarker.CoordinatesDTO.builder()
                        .latitude(property.getLatitude())
                        .longitude(property.getLongitude())
                        .build())
                .streetAddress(property.getStreetAddress())
                .wardName(extractLocationName(property.getLocation(), "WARD"))
                .districtName(extractLocationName(property.getLocation(), "DISTRICT"))
                .cityName(extractLocationName(property.getLocation(), "CITY"))
                .price(listing.getPrice())
                .listingType(listing.getListingType())
                .name(listing.getName())
                .thumbnailUrl(thumbnailUrl)
                .sizeM2(property.getUsableSizeM2() != null ? property.getUsableSizeM2() : property.getLandSizeM2())
                .propertyType(property.getPropertyType() != null ? property.getPropertyType().getName() : null)
                .isFavorite(isFavorite)
                .isBoosted(!boosts.isEmpty())
                .boostPackages(boosts.stream()
                        .map(b -> b.getBoostType().name())
                        .collect(Collectors.toList()))
                .attributes(listingMapper.toAttributeList(attrValues))
                .build();
    }

    private String extractLocationName(Location location, String type) {
        Location current = location;
        while (current != null) {
            if (current.getType().name().equals(type)) {
                return current.getName();
            }
            current = current.getParent();
        }
        return null;
    }
}
