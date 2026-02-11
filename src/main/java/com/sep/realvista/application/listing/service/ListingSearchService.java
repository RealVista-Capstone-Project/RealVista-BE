package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.common.exception.DomainException;
import com.sep.realvista.domain.billing.boost.BoostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        static final String BEDROOMS = "bedrooms";
        static final String BATHROOMS = "bathrooms";
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

    @Transactional(readOnly = true)
    public Page<ListingSearchResponse> search(
            String listingType,
            String propertyType, 
            String propertyCategory,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minArea,
            Double maxArea,
            Integer bedrooms,
            Integer bathrooms,
            Map<String, String> dynamicAttributes,
            String sortBy,
            Pageable pageable) {
        
        log.debug("🔍 Searching with: type={}, propertyType={}, category={}, dynamicAttrs={}", 
            listingType, propertyType, propertyCategory, dynamicAttributes);
        
        // Handle custom sorting if requested
        Pageable effectivePageable = pageable;
        if (sortBy != null && !sortBy.isBlank()) {
            effectivePageable = applySorting(sortBy, pageable);
        }

        Specification<Listing> spec = buildSpecification(
            listingType, propertyType, propertyCategory, location,
            minPrice, maxPrice, minArea, maxArea, bedrooms, bathrooms,
            dynamicAttributes
        );
        
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

    private Specification<Listing> buildSpecification(
            String listingType,
            String propertyType,
            String propertyCategory,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minArea,
            Double maxArea,
            Integer bedrooms,
            Integer bathrooms,
            Map<String, String> dynamicAttributes) {
        
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            
            // Only search published listings
            predicates.add(cb.equal(root.get(ListingFields.STATUS), ListingStatus.PUBLISHED));
            log.debug("✅ Added PUBLISHED filter");

            Join<Object, Object> propertyJoin = root.join(ListingFields.PROPERTY);

            // Listing Type
            if (listingType != null && !listingType.isBlank()) {
                try {
                    ListingType type = ListingType.valueOf(listingType.toUpperCase());
                    predicates.add(cb.equal(root.get(ListingFields.LISTING_TYPE), type));
                    log.debug("✅ Added listingType filter: {}", type);
                } catch (IllegalArgumentException e) {
                    log.warn("❌ Invalid listing type: {}", listingType);
                }
            }

            // Property Type
            if (propertyType != null && !propertyType.isBlank()) {
                log.debug("🏠 Property Type filter: {}", propertyType);
                predicates.add(cb.equal(
                    propertyJoin.get(PropertyFields.TYPE).get(PropertyTypeFields.CODE), 
                    propertyType
                ));
            }

            // Property Category
            if (propertyCategory != null && !propertyCategory.isBlank()) {
                predicates.add(cb.equal(
                    propertyJoin.get(PropertyFields.TYPE)
                        .get(PropertyTypeFields.CATEGORY)
                        .get(CategoryFields.CODE), 
                    propertyCategory
                ));
                log.debug("✅ Added propertyCategory filter: {}", propertyCategory);
            }

            // Location (LIKE search)
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(
                    propertyJoin.join(PropertyFields.LOCATION).get(LocationFields.NAME)), 
                    "%" + location.toLowerCase() + "%"
                ));
                log.debug("✅ Added location LIKE filter: {}", location);
            }

            // Price Range
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(ListingFields.PRICE), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(ListingFields.PRICE), maxPrice));
            }

            // Area Range
            if (minArea != null) {
                predicates.add(cb.greaterThanOrEqualTo(propertyJoin.get(PropertyFields.USABLE_SIZE_M2), minArea));
            }
            if (maxArea != null) {
                predicates.add(cb.lessThanOrEqualTo(propertyJoin.get(PropertyFields.USABLE_SIZE_M2), maxArea));
            }
            
            // Bedrooms
            if (bedrooms != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    propertyJoin.get(PropertyFields.BEDROOMS),
                    bedrooms
                ));
            }
            
            // Bathrooms
            if (bathrooms != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    propertyJoin.get(PropertyFields.BATHROOMS),
                    bathrooms
                ));
            }
            
            // Dynamic Attributes (JSONB) - Generic handling
            if (dynamicAttributes != null && !dynamicAttributes.isEmpty()) {
                dynamicAttributes.forEach((attributeCode, value) -> {
                    if (value != null && !value.isBlank()) {
                        predicates.add(cb.equal(
                            cb.function(JSONB_EXTRACT_FUNCTION, String.class,
                                propertyJoin.get(PropertyFields.EXTRA_ATTRIBUTES),
                                cb.literal(attributeCode)),
                            value
                        ));
                        log.debug("✅ Added dynamic attribute filter: {}={}", attributeCode, value);
                    }
                });
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
