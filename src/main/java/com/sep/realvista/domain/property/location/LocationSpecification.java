package com.sep.realvista.domain.property.location;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * JPA Specification builder for dynamic Location filtering (admin list endpoint).
 */
public final class LocationSpecification {

    private LocationSpecification() { }

    /**
     * Build a composite specification from optional filter parameters.
     * Always excludes soft-deleted records.
     *
     * @param search   partial match on name or code (case-insensitive); null = no filter
     * @param level    exact match on type (CITY/DISTRICT/WARD); null = no filter
     * @param parentId exact match on parentId; null = no filter
     */
    public static Specification<Location> filterBy(String search, LocationType level,
                                                   UUID parentId, LocationStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted
            predicates.add(cb.equal(root.get("deleted"), false));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                String normalizedPattern = "%" + normalizeSearch(search) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("normalizedName")), normalizedPattern)
                ));
            }

            if (level != null) {
                predicates.add(cb.equal(root.get("type"), level));
            }

            if (parentId != null) {
                predicates.add(cb.equal(root.get("parentId"), parentId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalizeSearch(String value) {
        return java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('Đ', 'D')
                .replace('đ', 'd')
                .toLowerCase(Locale.ROOT);
    }
}
