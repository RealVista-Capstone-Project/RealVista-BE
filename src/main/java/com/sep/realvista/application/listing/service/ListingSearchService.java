package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
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


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingSearchService {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

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

    // Attribute codes in property_attributes table
    private static final String ATTR_CODE_BEDROOMS = "BEDROOMS";
    private static final String ATTR_CODE_BATHROOMS = "BATHROOMS";

    private static final String JSONB_EXTRACT_FUNCTION = "jsonb_extract_path_text";

    @Transactional(readOnly = true)
    public Page<ListingSearchResponse> search(ListingSearchCriteria criteria, Pageable pageable) {

        log.debug("Searching with criteria: {}", criteria);

        // Handle custom sorting if requested
        Pageable effectivePageable = pageable;
        if (criteria.getSortBy() != null && !criteria.getSortBy().isBlank()) {
            effectivePageable = applySorting(criteria.getSortBy(), pageable);
        }

        Specification<Listing> spec = buildSpecification(criteria);

        Page<Listing> listings = listingRepository.findAll(spec, effectivePageable);

        // Map to response and populate thumbnails
        return listings.map(listing -> {
            ListingSearchResponse response = listingMapper.toSearchResponse(listing);

            // Fetch thumbnail from listing_medias if not already populated
            if (response.getThumbnail() == null || response.getThumbnail().isBlank()) {
                String thumbnail = fetchThumbnailForListing(listing.getListingId());
                response.setThumbnail(thumbnail);
            }

            return response;
        });
    }

    private String fetchThumbnailForListing(UUID listingId) {
        // Query to get the primary media thumbnail
        var result = listingRepository.findThumbnailByListingId(listingId);
        return result.orElse(null);
    }

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
                // Priority sorting is complex (Boost > Agent > Owner > Date)
                // For now, implementing basic Date DESC as fallback or simple multi-column sort
                sort = Sort.by(Sort.Direction.DESC, ListingFields.PUBLISHED_AT);
                break;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Specification<Listing> buildSpecification(ListingSearchCriteria criteria) {

        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Only search published listings
            predicates.add(cb.equal(root.get(ListingFields.STATUS), ListingStatus.PUBLISHED));
            log.debug("Added PUBLISHED filter");

            Join<Object, Object> propertyJoin = root.join(ListingFields.PROPERTY);

            // Listing Type
            if (criteria.getListingType() != null && !criteria.getListingType().isBlank()) {
                try {
                    ListingType type = ListingType.valueOf(criteria.getListingType().toUpperCase());
                    predicates.add(cb.equal(root.get(ListingFields.LISTING_TYPE), type));
                    log.debug("Added listingType filter: {}", type);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid listing type: {}", criteria.getListingType());
                }
            }

            // Property Type
            if (criteria.getPropertyType() != null && !criteria.getPropertyType().isBlank()) {
                log.debug("Property type filter: {}", criteria.getPropertyType());
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
                log.debug("Added propertyCategory filter: {}", criteria.getPropertyCategory());
            }

            // Location (LIKE search)
            if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
                predicates.add(cb.like(cb.lower(
                    propertyJoin.join(PropertyFields.LOCATION).get(LocationFields.NAME)),
                    "%" + criteria.getLocation().toLowerCase() + "%"
                ));
                log.debug("Added location LIKE filter: {}", criteria.getLocation());
            }

            // Price Range
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(ListingFields.PRICE), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(ListingFields.PRICE), criteria.getMaxPrice()));
            }

            // Area Range
            if (criteria.getMinArea() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                propertyJoin.get(PropertyFields.USABLE_SIZE_M2),
                                criteria.getMinArea()
                        )
                );
            }
            if (criteria.getMaxArea() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                propertyJoin.get(PropertyFields.USABLE_SIZE_M2),
                                criteria.getMaxArea()
                        )
                );
            }

            // Bedrooms - query via property_attribute_values table
            if (criteria.getBedrooms() != null) {
                Subquery<UUID> bedroomSubquery = query.subquery(UUID.class);
                Root<PropertyAttributeValue> pavRoot = bedroomSubquery.from(PropertyAttributeValue.class);
                Join<Object, Object> paJoin = pavRoot.join("propertyAttribute");
                bedroomSubquery.select(pavRoot.get("propertyId"))
                    .where(
                        cb.equal(paJoin.get("code"), ATTR_CODE_BEDROOMS),
                        cb.greaterThanOrEqualTo(
                            pavRoot.get("valueNumber"),
                            BigDecimal.valueOf(criteria.getBedrooms())
                        )
                    );
                predicates.add(propertyJoin.get(PropertyFields.PROPERTY_ID).in(bedroomSubquery));
                log.debug("Added bedrooms filter via attribute values: >= {}", criteria.getBedrooms());
            }

            // Bathrooms - query via property_attribute_values table
            if (criteria.getBathrooms() != null) {
                Subquery<UUID> bathroomSubquery = query.subquery(UUID.class);
                Root<PropertyAttributeValue> pavRoot = bathroomSubquery.from(PropertyAttributeValue.class);
                Join<Object, Object> paJoin = pavRoot.join("propertyAttribute");
                bathroomSubquery.select(pavRoot.get("propertyId"))
                    .where(
                        cb.equal(paJoin.get("code"), ATTR_CODE_BATHROOMS),
                        cb.greaterThanOrEqualTo(
                            pavRoot.get("valueNumber"),
                            BigDecimal.valueOf(criteria.getBathrooms())
                        )
                    );
                predicates.add(propertyJoin.get(PropertyFields.PROPERTY_ID).in(bathroomSubquery));
                log.debug("Added bathrooms filter via attribute values: >= {}", criteria.getBathrooms());
            }

            // Dynamic Attributes (JSONB) - Generic handling
            if (criteria.getDynamicAttributes() != null && !criteria.getDynamicAttributes().isEmpty()) {
                criteria.getDynamicAttributes().forEach((attributeCode, value) -> {
                    if (value != null && !value.isBlank()) {
                        predicates.add(cb.equal(
                            cb.function(JSONB_EXTRACT_FUNCTION, String.class,
                                propertyJoin.get(PropertyFields.EXTRA_ATTRIBUTES),
                                cb.literal(attributeCode)),
                            value
                        ));
                        log.debug("Added dynamic attribute filter: {}={}", attributeCode, value);
                    }
                });
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
