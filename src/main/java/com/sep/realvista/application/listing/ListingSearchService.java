package com.sep.realvista.application.listing;

import com.sep.realvista.application.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingSearchService {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    // Constants for magic strings
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LISTING_TYPE = "listingType";
    private static final String FIELD_PROPERTY = "property";
    private static final String FIELD_PROPERTY_TYPE = "propertyType";
    private static final String FIELD_PROPERTY_TYPE_ID = "propertyTypeId";
    private static final String FIELD_PROPERTY_CATEGORY = "propertyCategory";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_USABLE_SIZE_M2 = "usableSizeM2";
    private static final String FIELD_AVAILABLE_FROM = "availableFrom";
    private static final String FIELD_BEDROOMS = "bedrooms";
    private static final String FIELD_BATHROOMS = "bathrooms";
    private static final String FIELD_EXTRA_ATTRIBUTES = "extraAttributes";
    private static final String FIELD_PROPERTY_ID = "propertyId";
    private static final String FIELD_MEDIA_TYPE = "mediaType";
    
    private static final String JSONB_EXTRACT_FUNCTION = "jsonb_extract_path_text";

    @Transactional(readOnly = true)
    public Page<ListingSearchResponse> search(AdvancedSearchRequest request, Pageable pageable) {
        log.debug("Searching listings with criteria: {}", request);
        
        validateSearchRequest(request);
        
        Specification<Listing> spec = buildSpecification(request);
        Page<Listing> listings = listingRepository.findAll(spec, pageable);
        
        log.debug("Found {} listings", listings.getTotalElements());
        return listings.map(listingMapper::toSearchResponse);
    }

    /**
     * Validate search request before building specification
     */
    private void validateSearchRequest(AdvancedSearchRequest request) {
        // Validate price range
        if (request.getPrice() != null) {
            if (request.getPrice().size() != 2) {
                throw new DomainException(
                    "Price range must contain exactly 2 elements [min, max]",
                    "INVALID_PRICE_RANGE"
                );
            }
            if (request.getPrice().get(0) != null && request.getPrice().get(1) != null) {
                if (request.getPrice().get(0).compareTo(request.getPrice().get(1)) > 0) {
                    throw new DomainException(
                        "Price minimum cannot be greater than maximum",
                        "INVALID_PRICE_RANGE"
                    );
                }
            }
        }

        // Validate area range
        if (request.getArea() != null) {
            if (request.getArea().size() != 2) {
                throw new DomainException(
                    "Area range must contain exactly 2 elements [min, max]",
                    "INVALID_AREA_RANGE"
                );
            }
            if (request.getArea().get(0) != null && request.getArea().get(1) != null) {
                if (request.getArea().get(0).compareTo(request.getArea().get(1)) > 0) {
                    throw new DomainException(
                        "Area minimum cannot be greater than maximum",
                        "INVALID_AREA_RANGE"
                    );
                }
            }
        }

        // Validate bedrooms/bathrooms
        if (request.getBedrooms() != null && request.getBedrooms() < 0) {
            throw new DomainException(
                "Bedrooms cannot be negative",
                "INVALID_BEDROOMS"
            );
        }
        if (request.getBathrooms() != null && request.getBathrooms() < 0) {
            throw new DomainException(
                "Bathrooms cannot be negative",
                "INVALID_BATHROOMS"
            );
        }
    }

    private Specification<Listing> buildSpecification(AdvancedSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only search published listings
            predicates.add(cb.equal(root.get(FIELD_STATUS), ListingStatus.PUBLISHED));

            Join<Object, Object> propertyJoin = root.join(FIELD_PROPERTY);

            addBasicCriteria(request, predicates, cb, root, propertyJoin);
            addAttributeCriteria(request, predicates, cb, propertyJoin);
            addMediaCriteria(request, predicates, cb, root, query, propertyJoin);
            addDynamicCriteria(request, predicates, cb, propertyJoin);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addBasicCriteria(AdvancedSearchRequest request, List<Predicate> predicates,
                                   CriteriaBuilder cb, Root<Listing> root,
                                   Join<Object, Object> propertyJoin) {
        // Listing Type - WITH PROPER ERROR HANDLING
        if (request.getListingType() != null && !request.getListingType().isBlank()) {
            try {
                ListingType type = ListingType.valueOf(request.getListingType().toUpperCase());
                predicates.add(cb.equal(root.get(FIELD_LISTING_TYPE), type));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid listing type provided: {}", request.getListingType());
                throw new DomainException("Invalid listing type: " + request.getListingType() 
                    + ". Valid values are: SALE, RENT", "INVALID_LISTING_TYPE");
            }
        }

        // Property Type
        if (request.getPropertyType() != null) {
            predicates.add(cb.equal(
                propertyJoin.get(FIELD_PROPERTY_TYPE).get(FIELD_PROPERTY_TYPE_ID), 
                request.getPropertyType()
            ));
        }

        // Property Category
        if (request.getPropertyCategory() != null && !request.getPropertyCategory().isBlank()) {
            predicates.add(cb.equal(
                propertyJoin.get(FIELD_PROPERTY_TYPE)
                    .get(FIELD_PROPERTY_CATEGORY)
                    .get(FIELD_CODE), 
                request.getPropertyCategory()
            ));
        }

        // Location
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            predicates.add(cb.equal(
                propertyJoin.join(FIELD_LOCATION).get(FIELD_NAME), 
                request.getLocation()
            ));
        }

        // Price Range
        if (request.getPrice() != null && request.getPrice().size() == 2) {
            if (request.getPrice().get(0) != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get(FIELD_PRICE), 
                    request.getPrice().get(0)
                ));
            }
            if (request.getPrice().get(1) != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get(FIELD_PRICE), 
                    request.getPrice().get(1)
                ));
            }
        }

        // Area Range
        if (request.getArea() != null && request.getArea().size() == 2) {
            if (request.getArea().get(0) != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    propertyJoin.get(FIELD_USABLE_SIZE_M2), 
                    request.getArea().get(0)
                ));
            }
            if (request.getArea().get(1) != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    propertyJoin.get(FIELD_USABLE_SIZE_M2), 
                    request.getArea().get(1)
                ));
            }
        }

        // Available From
        if (request.getAvailableFrom() != null) {
            predicates.add(cb.or(
                cb.isNull(root.get(FIELD_AVAILABLE_FROM)),
                cb.lessThanOrEqualTo(root.get(FIELD_AVAILABLE_FROM), request.getAvailableFrom())
            ));
        }
    }

    private void addAttributeCriteria(AdvancedSearchRequest request, List<Predicate> predicates,
                                       CriteriaBuilder cb, Join<Object, Object> propertyJoin) {
        // Bedrooms
        if (request.getBedrooms() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                propertyJoin.get(FIELD_BEDROOMS),
                request.getBedrooms()
            ));
        }

        // Bathrooms
        if (request.getBathrooms() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                propertyJoin.get(FIELD_BATHROOMS),
                request.getBathrooms()
            ));
        }

        // Text attributes
        addTextAttributeFilter(request.getLegal(), "legal", predicates, cb, propertyJoin);
        addTextAttributeFilter(request.getFurniture(), "furniture", predicates, cb, propertyJoin);
        addTextAttributeFilter(request.getDirection(), "direction", predicates, cb, propertyJoin);
        addTextAttributeFilter(request.getBalconyDirection(), "balconyDirection", 
                predicates, cb, propertyJoin);
    }

    private void addTextAttributeFilter(String value, String attributeName,
                                         List<Predicate> predicates, CriteriaBuilder cb,
                                         Join<Object, Object> propertyJoin) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.equal(
                cb.function(JSONB_EXTRACT_FUNCTION, String.class,
                    propertyJoin.get(FIELD_EXTRA_ATTRIBUTES), 
                    cb.literal(attributeName)),
                value
            ));
        }
    }

    private void addMediaCriteria(AdvancedSearchRequest request, List<Predicate> predicates,
                                   CriteriaBuilder cb, Root<Listing> root,
                                   CriteriaQuery<?> query, Join<Object, Object> propertyJoin) {
        
        List<com.sep.realvista.domain.property.MediaType> requestedMediaTypes = new ArrayList<>();
        
        if (Boolean.TRUE.equals(request.getHasVideo())) {
            requestedMediaTypes.add(com.sep.realvista.domain.property.MediaType.VIDEO);
        }
        if (Boolean.TRUE.equals(request.getHas3D())) {
            requestedMediaTypes.add(com.sep.realvista.domain.property.MediaType.THREE_D);
        }

        if (!requestedMediaTypes.isEmpty()) {
            // Combined subquery for better performance
            var subquery = query.subquery(Long.class);
            var mediaRoot = subquery.from(com.sep.realvista.domain.property.PropertyMedia.class);
            
            subquery.select(cb.literal(1L))
                .where(
                    cb.equal(mediaRoot.get(FIELD_PROPERTY_ID), propertyJoin.get(FIELD_PROPERTY_ID)),
                    mediaRoot.get(FIELD_MEDIA_TYPE).in(requestedMediaTypes)
                );
            
            predicates.add(cb.exists(subquery));
        }
    }

    private void addDynamicCriteria(AdvancedSearchRequest request, List<Predicate> predicates,
                                     CriteriaBuilder cb, Join<Object, Object> propertyJoin) {
        if (request.getDynamic() != null && !request.getDynamic().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getDynamic().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Validate key (prevent injection)
                if (key == null || key.isBlank() || !isValidAttributeKey(key)) {
                    log.warn("Invalid dynamic attribute key: {}", key);
                    throw new DomainException(
                        "Invalid dynamic attribute key: " + key,
                        "INVALID_DYNAMIC_KEY"
                    );
                }

                if (value instanceof List && ((List<?>) value).size() == 2) {
                    // Range query
                    List<?> range = (List<?>) value;
                    addDynamicRangeCriteria(key, range, predicates, cb, propertyJoin);
                } else if (value != null) {
                    // Exact match
                    predicates.add(cb.equal(
                        cb.function(JSONB_EXTRACT_FUNCTION, String.class,
                            propertyJoin.get(FIELD_EXTRA_ATTRIBUTES), 
                            cb.literal(key)),
                        value.toString()
                    ));
                }
            }
        }
    }

    private void addDynamicRangeCriteria(String key, List<?> range, List<Predicate> predicates,
                                          CriteriaBuilder cb, Join<Object, Object> propertyJoin) {
        try {
            if (range.get(0) != null) {
                Double minValue = parseDoubleValue(range.get(0));
                predicates.add(cb.greaterThanOrEqualTo(
                    cb.function(JSONB_EXTRACT_FUNCTION, String.class,
                        propertyJoin.get(FIELD_EXTRA_ATTRIBUTES), 
                        cb.literal(key)).as(Double.class),
                    minValue
                ));
            }
            if (range.get(1) != null) {
                Double maxValue = parseDoubleValue(range.get(1));
                predicates.add(cb.lessThanOrEqualTo(
                    cb.function(JSONB_EXTRACT_FUNCTION, String.class,
                        propertyJoin.get(FIELD_EXTRA_ATTRIBUTES), 
                        cb.literal(key)).as(Double.class),
                    maxValue
                ));
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid numeric value in dynamic range for key {}: {}", key, range);
            throw new DomainException(
                "Invalid numeric value in range for attribute: " + key,
                "INVALID_DYNAMIC_VALUE"
            );
        }
    }

    private Double parseDoubleValue(Object value) throws NumberFormatException {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(value.toString());
    }

    /**
     * Validate attribute key to prevent potential injection attacks
     */
    private boolean isValidAttributeKey(String key) {
        // Allow only alphanumeric, underscore, and hyphen
        return key.matches("^[a-zA-Z0-9_-]+$");
    }
}