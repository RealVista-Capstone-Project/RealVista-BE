package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationRepository;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import com.sep.realvista.domain.billing.boost.BoostType;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.ListingBoostStatus;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.user.role.RoleCode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingSearchService {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final BookmarkRepository bookmarkRepository;
    private final PropertyAttributeValueJpaRepository propertyAttributeValueRepository;
    private final LocationRepository locationRepository;
    private final ListingBoostRepository listingBoostRepository;

    private static final class ListingFields {
        static final String STATUS = "status";
        static final String LISTING_TYPE = "listingType";
        static final String PROPERTY = "property";
        static final String PRICE = "price";
        static final String AVAILABLE_FROM = "availableFrom";
        static final String PUBLISHED_AT = "publishedAt";
    }

    private static final class PropertyFields {
        static final String TYPE = "propertyType";
        static final String LOCATION = "location";
        static final String LOCATION_ID = "locationId";
        static final String USABLE_SIZE_M2 = "usableSizeM2";
        static final String PROPERTY_ID = "propertyId";
        static final String EXTRA_ATTRIBUTES = "extraAttributes";
    }

    private static final class PropertyTypeFields {
        static final String ID = "propertyTypeId";
        static final String CODE = "code";
        static final String CATEGORY = "propertyCategory";
    }

    private static final class CategoryFields {
        static final String CODE = "code";
    }

    private static final class LocationFields {
        static final String NAME = "name";
    }

    private static final String JSONB_EXTRACT_FUNCTION = "jsonb_extract_path_text";

    /**
     * Attribute codes that represent numeric quantities and should use >= (min) semantics
     * when passed via dynamicAttributes. Clients pass e.g. {"BEDROOMS": "2"} to mean
     * "at least 2 bedrooms".
     */
    private static final Set<String> NUMERIC_MIN_ATTRIBUTE_CODES = Set.of("BEDROOMS", "BATHROOMS");

    @Transactional(readOnly = true)
    public Page<ListingSearchResponse> search(ListingSearchCriteria criteria, Pageable pageable, UUID userId) {

        log.debug("Searching with criteria: {}", criteria);

        // Handle custom sorting if requested
        Pageable effectivePageable = pageable;
        boolean usePrioritySort = "PRIORITY".equalsIgnoreCase(criteria.getSortBy());
        if (criteria.getSortBy() != null && !criteria.getSortBy().isBlank() && !usePrioritySort) {
            effectivePageable = applySorting(criteria.getSortBy(), pageable);
        }

        Specification<Listing> spec = buildSpecification(criteria);

        Page<Listing> listings = listingRepository.findAll(spec, effectivePageable);

        // Bulk fetch bookmarked listing IDs for this page (single query, avoids N+1)
        Set<UUID> bookmarkedIds = Collections.emptySet();
        if (userId != null && !listings.isEmpty()) {
            List<UUID> pageListingIds = listings.stream()
                    .map(Listing::getListingId)
                    .collect(Collectors.toList());
            bookmarkedIds = bookmarkRepository.findBookmarkedListingIds(userId, pageListingIds);
        }

        // Bulk fetch all attributes for all properties on this page (avoids N+1)
        List<UUID> propertyIds = listings.stream()
                .map(l -> l.getProperty() != null ? l.getProperty().getPropertyId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        final Map<UUID, List<PropertyAttributeValue>> attributesByPropertyId;
        if (!propertyIds.isEmpty()) {
            attributesByPropertyId = propertyAttributeValueRepository
                    .findAllAttributesByPropertyIds(propertyIds)
                    .stream()
                    .collect(Collectors.groupingBy(PropertyAttributeValue::getPropertyId));
        } else {
            attributesByPropertyId = Collections.emptyMap();
        }

        // Bulk fetch active boosts for this page
        List<UUID> listingIds = listings.stream()
                .map(Listing::getListingId)
                .collect(Collectors.toList());
        Map<UUID, List<ListingBoost>> activeBoostsByListingId = listingBoostRepository
                .findAllActiveByListingIds(listingIds, LocalDate.now())
                .stream()
                .collect(Collectors.groupingBy(ListingBoost::getListingId));

        // Final reference for use inside lambda
        final Set<UUID> finalBookmarkedIds = bookmarkedIds;

        // Map to response and populate thumbnails + isFavorite + attributes
        return listings.map(listing -> {
            ListingSearchResponse response = listingMapper.toSearchResponse(listing);

            // Populate address fields explicitly here (within @Transactional) to ensure
            // lazy-loaded property.location chain is resolved within the open session.
            if (listing.getProperty() != null) {
                response.setStreetAddress(listing.getProperty().getStreetAddress());
                Location loc = listing.getProperty().getLocation();
                while (loc != null) {
                    switch (loc.getType()) {
                        case CITY -> response.setCityName(loc.getName());
                        case DISTRICT -> response.setDistrictName(loc.getName());
                        case WARD -> response.setWardName(loc.getName());
                        default -> { }
                    }
                    loc = loc.getParent();
                }
            }

            // Fetch thumbnail from listing_medias if not already populated
            if (response.getThumbnail() == null || response.getThumbnail().isBlank()) {
                String thumbnail = fetchThumbnailForListing(listing.getListingId());
                response.setThumbnail(thumbnail);
            }

            response.setIsFavorite(finalBookmarkedIds.contains(listing.getListingId()));

            // Populate boost info
            List<ListingBoost> boosts = activeBoostsByListingId.getOrDefault(listing.getListingId(), List.of());
            if (!boosts.isEmpty()) {
                response.setIsBoosted(true);
                response.setBoostPackages(boosts.stream()
                        .map(b -> b.getBoostType().name())
                        .collect(Collectors.toList()));
            } else {
                response.setIsBoosted(false);
                response.setBoostPackages(List.of());
            }

            // Populate user type (Agent/Owner)
            if (listing.getUser() != null && listing.getUser().getUserRoles() != null) {
                boolean isAgent = listing.getUser().getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole() != null && ur.getRole().getRoleCode() == RoleCode.AGENT);
                response.setUserType(isAgent ? RoleCode.AGENT.name() : RoleCode.OWNER.name());
            }

            UUID propertyId = listing.getProperty() != null ? listing.getProperty().getPropertyId() : null;
            List<PropertyAttributeValue> attrs = propertyId != null
                    ? attributesByPropertyId.getOrDefault(propertyId, List.of())
                    : List.of();
            response.setAttributes(listingMapper.toAttributeList(attrs));

            return response;
        });
    }

    private String fetchThumbnailForListing(UUID listingId) {
        // Query to get the primary media thumbnail
        var result = listingRepository.findThumbnailByListingId(listingId);
        return result.orElse(null);
    }

    @SuppressWarnings("checkstyle:OperatorWrap")
    private Pageable applySorting(String sortBy, Pageable pageable) {
        Sort sort = Sort.unsorted();
        switch (sortBy) {
            case "DATE_DESC":
                sort = Sort.by(Sort.Direction.DESC, ListingFields.PUBLISHED_AT);
                break;
            case "PRICE_ASC":
                sort = Sort.by(Sort.Direction.ASC, ListingFields.PRICE);
                break;
            case "PRICE_DESC":
                sort = Sort.by(Sort.Direction.DESC, ListingFields.PRICE);
                break;
            case "PRIORITY":
            default:
                // Priority sorting is handled via CriteriaBuilder in buildSpecification
                // as it requires complex joins not supported by standard Pageable Sort
                sort = Sort.unsorted();
                break;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Specification<Listing> buildSpecification(ListingSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Handle Priority-based sorting (Featured > Hot > Agent > Date)
            applyPrioritySorting(criteria, root, query, cb);

            // Add standard filters
            addStandardFilters(criteria, root, cb, predicates);

            // Add dynamic attributes
            addDynamicAttributeFilters(criteria, root, query, cb, predicates);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void applyPrioritySorting(ListingSearchCriteria criteria, Root<Listing> root,
                                      jakarta.persistence.criteria.CriteriaQuery<?> query,
                                      jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (!"PRIORITY".equalsIgnoreCase(criteria.getSortBy())) {
            return;
        }

        // Subquery for FEATURED boost
        Subquery<Long> featuredSub = query.subquery(Long.class);
        Root<ListingBoost> lbFeatured = featuredSub.from(ListingBoost.class);
        featuredSub.select(cb.count(lbFeatured))
            .where(cb.and(
                cb.equal(lbFeatured.get("listingId"), root.get("listingId")),
                cb.equal(lbFeatured.get("boostType"), BoostType.FEATURED),
                cb.equal(lbFeatured.get("status"), ListingBoostStatus.ACTIVE),
                cb.lessThanOrEqualTo(lbFeatured.get("startDate"), cb.currentDate()),
                cb.greaterThanOrEqualTo(lbFeatured.get("endDate"), cb.currentDate()),
                cb.equal(lbFeatured.get("deleted"), false)
            ));

        // Subquery for HOT_BADGE boost
        Subquery<Long> hotSub = query.subquery(Long.class);
        Root<ListingBoost> lbHot = hotSub.from(ListingBoost.class);
        hotSub.select(cb.count(lbHot))
            .where(cb.and(
                cb.equal(lbHot.get("listingId"), root.get("listingId")),
                cb.equal(lbHot.get("boostType"), BoostType.HOT_BADGE),
                cb.equal(lbHot.get("status"), ListingBoostStatus.ACTIVE),
                cb.lessThanOrEqualTo(lbHot.get("startDate"), cb.currentDate()),
                cb.greaterThanOrEqualTo(lbHot.get("endDate"), cb.currentDate()),
                cb.equal(lbHot.get("deleted"), false)
            ));

        // Subquery for AGENT role
        Subquery<Long> agentSub = query.subquery(Long.class);
        Root<com.sep.realvista.domain.user.User> u = agentSub.from(com.sep.realvista.domain.user.User.class);
        var ur = u.join("userRoles");
        var r = ur.join("role");
        agentSub.select(cb.count(u))
            .where(cb.and(
                cb.equal(u.get("userId"), root.get("user").get("userId")),
                cb.equal(r.get("roleCode"), RoleCode.AGENT)
            ));

        var priorityScore = cb.selectCase()
            .when(cb.greaterThan(featuredSub, 0L), 40)
            .when(cb.greaterThan(hotSub, 0L), 30)
            .when(cb.greaterThan(agentSub, 0L), 20)
            .otherwise(10);

        query.orderBy(
            cb.desc(priorityScore),
            cb.desc(root.get(ListingFields.PUBLISHED_AT))
        );
    }

    private void addStandardFilters(ListingSearchCriteria criteria, Root<Listing> root,
                                    jakarta.persistence.criteria.CriteriaBuilder cb,
                                    List<Predicate> predicates) {
        // Only search published listings
        predicates.add(cb.equal(root.get(ListingFields.STATUS), ListingStatus.PUBLISHED));

        Join<Object, Object> propertyJoin = root.join(ListingFields.PROPERTY);

        // Listing Type
        if (criteria.getListingType() != null && !criteria.getListingType().isBlank()) {
            try {
                ListingType type = ListingType.valueOf(criteria.getListingType().toUpperCase());
                predicates.add(cb.equal(root.get(ListingFields.LISTING_TYPE), type));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid listing type: {}", criteria.getListingType());
            }
        }

        // Property Type
        if (criteria.getPropertyType() != null && !criteria.getPropertyType().isBlank()) {
            predicates.add(cb.equal(
                propertyJoin.get(PropertyFields.TYPE).get(PropertyTypeFields.CODE),
                criteria.getPropertyType()
            ));
        }

        // Property Category
        if (criteria.getPropertyCategory() != null && !criteria.getPropertyCategory().isBlank()) {
            predicates.add(cb.equal(
                propertyJoin.get(PropertyFields.TYPE)
                    .get(PropertyTypeFields.CATEGORY)
                    .get(CategoryFields.CODE),
                criteria.getPropertyCategory()
            ));
        }

        // Location LIKE search
        if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
            predicates.add(cb.like(cb.lower(
                propertyJoin.join(PropertyFields.LOCATION).get(LocationFields.NAME)),
                "%" + criteria.getLocation().toLowerCase() + "%"
            ));
        }

        // Location ID hierarchical
        if (criteria.getLocationId() != null) {
            List<UUID> wardIds = locationRepository.findDescendantWardIds(criteria.getLocationId());
            if (!wardIds.isEmpty()) {
                predicates.add(propertyJoin.get(PropertyFields.LOCATION_ID).in(wardIds));
            } else {
                predicates.add(cb.disjunction());
            }
        }

        // Price and Area
        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(ListingFields.PRICE), criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(ListingFields.PRICE), criteria.getMaxPrice()));
        }
        if (criteria.getMinArea() != null) {
            predicates.add(cb.greaterThanOrEqualTo(propertyJoin.get(PropertyFields.USABLE_SIZE_M2),
                    criteria.getMinArea()));
        }
        if (criteria.getMaxArea() != null) {
            predicates.add(cb.lessThanOrEqualTo(propertyJoin.get(PropertyFields.USABLE_SIZE_M2),
                    criteria.getMaxArea()));
        }
    }

    private void addDynamicAttributeFilters(ListingSearchCriteria criteria, Root<Listing> root,
                                            jakarta.persistence.criteria.CriteriaQuery<?> query,
                                            jakarta.persistence.criteria.CriteriaBuilder cb,
                                            List<Predicate> predicates) {
        if (criteria.getDynamicAttributes() == null || criteria.getDynamicAttributes().isEmpty()) {
            return;
        }

        Join<Object, Object> propertyJoin = root.join(ListingFields.PROPERTY);

        criteria.getDynamicAttributes().forEach((code, value) -> {
            if (value == null || value.isBlank()) {
                return;
            }

            if (NUMERIC_MIN_ATTRIBUTE_CODES.contains(code.toUpperCase())) {
                try {
                    java.math.BigDecimal minValue = new java.math.BigDecimal(value);
                    Subquery<UUID> subquery = query.subquery(UUID.class);
                    Root<PropertyAttributeValue> pavRoot = subquery.from(PropertyAttributeValue.class);
                    Join<Object, Object> paJoin = pavRoot.join("propertyAttribute");
                    subquery.select(pavRoot.get("propertyId"))
                        .where(
                            cb.equal(paJoin.get("code"), code.toUpperCase()),
                            cb.greaterThanOrEqualTo(pavRoot.get("valueNumber"), minValue)
                        );
                    predicates.add(propertyJoin.get(PropertyFields.PROPERTY_ID).in(subquery));
                } catch (NumberFormatException e) {
                    log.warn("Invalid numeric value for attribute {}: {}", code, value);
                }
            } else {
                predicates.add(cb.equal(
                    cb.function(JSONB_EXTRACT_FUNCTION, String.class,
                        propertyJoin.get(PropertyFields.EXTRA_ATTRIBUTES), cb.literal(code)),
                    value
                ));
            }
        });
    }
}
