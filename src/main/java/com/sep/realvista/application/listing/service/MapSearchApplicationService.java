package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.map.MapSearchRequest;
import com.sep.realvista.application.listing.dto.map.MapSearchResponse;
import com.sep.realvista.application.listing.dto.map.PropertyMapMarker;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.search.MapBounds;
import com.sep.realvista.domain.listing.search.MapSearchCriteria;
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
        if (bounds == null) {
            log.warn("Map search request with missing bounds - returning empty");
            return buildEmptyResponse(request,
                    MapBounds.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        }

        MapSearchCriteria criteria = buildCriteria(request, bounds);

        Long totalCount = listingRepository.countPublishedWithinBounds(criteria);
        if (totalCount == 0) {
            return buildEmptyResponse(request, bounds);
        }

        List<Listing> listings = listingRepository.findPublishedWithinBounds(criteria);
        Map<UUID, List<PropertyAttributeValue>> attributesByPropertyId = fetchBulkAttributes(listings);
        Set<UUID> bookmarkedIds = fetchBookmarkedIds(listings, userId);
        Map<UUID, List<ListingBoost>> activeBoostsByListingId = fetchActiveBoosts(listings);

        List<ListingSearchResponse> content = listings.stream()
                .map(l -> {
                    ListingSearchResponse response = listingMapper.toSearchResponse(l);

                    // Populate address fields and coordinates
                    if (l.getProperty() != null) {
                        response.setStreetAddress(l.getProperty().getStreetAddress());
                        Location loc = l.getProperty().getLocation();
                        while (loc != null) {
                            switch (loc.getType()) {
                                case CITY -> response.setCityName(loc.getName());
                                case DISTRICT -> response.setDistrictName(loc.getName());
                                case WARD -> response.setWardName(loc.getName());
                                default -> { }
                            }
                            loc = loc.getParent();
                        }

                        if (l.getProperty().getLatitude() != null && l.getProperty().getLongitude() != null) {
                            response.setCoordinates(PropertyMapMarker.CoordinatesDTO.builder()
                                    .latitude(l.getProperty().getLatitude())
                                    .longitude(l.getProperty().getLongitude())
                                    .build());
                        }
                    }

                    // Populate thumbnail
                    listingRepository.findThumbnailByListingId(l.getListingId())
                            .ifPresent(response::setThumbnail);

                    // Populate favorite status
                    response.setIsFavorite(bookmarkedIds.contains(l.getListingId()));

                    // Populate boost information
                    List<ListingBoost> boosts = activeBoostsByListingId.getOrDefault(l.getListingId(), List.of());
                    if (!boosts.isEmpty()) {
                        response.setIsBoosted(true);
                        response.setBoostPackages(boosts.stream()
                                .map(b -> b.getBoostType().name())
                                .collect(Collectors.toList()));
                    } else {
                        response.setIsBoosted(false);
                        response.setBoostPackages(List.of());
                    }

                    // Populate attributes and extract bedrooms/bathrooms
                    UUID propertyId = l.getPropertyId();
                    List<PropertyAttributeValue> attrs = attributesByPropertyId.getOrDefault(propertyId, List.of());
                    response.setAttributes(listingMapper.toAttributeList(attrs));

                    // extract stats
                    attrs.forEach(attr -> {
                        if (attr.getPropertyAttribute() != null && attr.getPropertyAttribute().getCode() != null) {
                            String code = attr.getPropertyAttribute().getCode().toLowerCase();
                            if ("phong-ngu".equals(code) || "bedrooms".equals(code)) {
                                response.setBedrooms(attr.getValueNumber() != null
                                        ? attr.getValueNumber().intValue() : null);
                            } else if ("phong-tam".equals(code) || "bathrooms".equals(code)) {
                                response.setBathrooms(attr.getValueNumber() != null
                                        ? attr.getValueNumber().intValue() : null);
                            }
                        }
                    });

                    return response;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return buildSearchResponse(request, content, totalCount, criteria.getPage(), criteria.getSize());
    }

    private MapBounds determineBounds(MapSearchRequest request) {
        if (request.getNorthLat() != null && request.getSouthLat() != null
                && request.getEastLng() != null && request.getWestLng() != null) {
            return MapBounds.of(request.getNorthLat(), request.getSouthLat(),
                    request.getEastLng(), request.getWestLng());
        }
        return null; // Signals invalid bounds
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
        MapSearchResponse response = new MapSearchResponse();
        response.setContent(List.of());
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setFirst(true);
        response.setLast(true);
        response.setBounds(MapSearchResponse.MapBoundsDTO.builder()
                .northLat(bounds.northLat())
                .southLat(bounds.southLat())
                .eastLng(bounds.eastLng())
                .westLng(bounds.westLng())
                .build());
        response.setFilterMetadata(buildFilterMetadata(request));
        return response;
    }

    private MapSearchResponse buildSearchResponse(MapSearchRequest request, List<ListingSearchResponse> markers,
            Long totalCount, int pageNumber, int pageSize) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        MapSearchResponse response = new MapSearchResponse();
        response.setContent(markers);
        response.setPage(pageNumber);
        response.setSize(pageSize);
        response.setTotalElements(totalCount);
        response.setTotalPages(totalPages);
        response.setFirst(pageNumber == 1);
        response.setLast(pageNumber >= totalPages);
        response.setBounds(MapSearchResponse.MapBoundsDTO.builder()
                .northLat(request.getNorthLat())
                .southLat(request.getSouthLat())
                .eastLng(request.getEastLng())
                .westLng(request.getWestLng())
                .build());
        response.setFilterMetadata(buildFilterMetadata(request));
        return response;
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

}
