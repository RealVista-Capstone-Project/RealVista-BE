package com.sep.realvista.application.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.presentation.rest.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.presentation.rest.listing.dto.ListingSearchResponse;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingSearchService {

    private final ListingRepository listingRepository;

    @Transactional(readOnly = true)
    public List<ListingSearchResponse> search(AdvancedSearchRequest request) {
        Specification<Listing> spec = buildSpecification(request);
        List<Listing> listings = listingRepository.findAll(spec);
        return listings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Specification<Listing> buildSpecification(AdvancedSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always only search for PUBLISHED listings
            predicates.add(cb.equal(root.get("status"), ListingStatus.PUBLISHED));

            Join<Object, Object> propertyJoin = root.join("property");

            // Fixed Criteria: Property Type
            if (request.getPropertyType() != null && !request.getPropertyType().isBlank()) {
                predicates.add(cb.equal(propertyJoin.join("propertyType").get("code"), request.getPropertyType()));
            }

            // Fixed Criteria: Location (District name)
            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                predicates.add(cb.equal(propertyJoin.join("location").get("name"), request.getLocation()));
            }

            // Fixed Criteria: Price Range
            if (request.getPrice() != null && request.getPrice().size() == 2) {
                if (request.getPrice().get(0) != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getPrice().get(0)));
                }
                if (request.getPrice().get(1) != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getPrice().get(1)));
                }
            }

            // Fixed Criteria: Area Range
            if (request.getArea() != null && request.getArea().size() == 2) {
                if (request.getArea().get(0) != null) {
                    predicates.add(cb.greaterThanOrEqualTo(propertyJoin.get("usableSizeM2"), request.getArea().get(0)));
                }
                if (request.getArea().get(1) != null) {
                    predicates.add(cb.lessThanOrEqualTo(propertyJoin.get("usableSizeM2"), request.getArea().get(1)));
                }
            }

            // Dynamic Criteria: JSONB extra_attributes
            if (request.getDynamic() != null && !request.getDynamic().isEmpty()) {
                for (Map.Entry<String, Object> entry : request.getDynamic().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof List && ((List<?>) value).size() == 2) {
                        // Range for dynamic attributes (e.g., floors: [10, 25])
                        List<?> range = (List<?>) value;
                        if (range.get(0) != null) {
                            predicates.add(cb.greaterThanOrEqualTo(
                                cb.function("jsonb_extract_path_text", String.class, 
                                    propertyJoin.get("extraAttributes"), cb.literal(key)).as(Double.class),
                                Double.valueOf(range.get(0).toString())
                            ));
                        }
                        if (range.get(1) != null) {
                            predicates.add(cb.lessThanOrEqualTo(
                                cb.function("jsonb_extract_path_text", String.class, 
                                    propertyJoin.get("extraAttributes"), cb.literal(key)).as(Double.class),
                                Double.valueOf(range.get(1).toString())
                            ));
                        }
                    } else if (value != null) {
                        // Exact match for dynamic attributes
                        predicates.add(cb.equal(
                            cb.function("jsonb_extract_path_text", String.class, 
                                propertyJoin.get("extraAttributes"), cb.literal(key)),
                            value.toString()
                        ));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ListingSearchResponse mapToResponse(Listing listing) {
        return ListingSearchResponse.builder()
                .listingId(listing.getListingId())
                .title("Property at " + listing.getProperty().getStreetAddress())
                .propertyType(listing.getProperty().getPropertyType().getName())
                .location(listing.getProperty().getLocation().getName())
                .price(listing.getPrice())
                .area(listing.getProperty().getUsableSizeM2())
                .thumbnailUrl(null) // Map from ListingMedia later
                .build();
    }
}
