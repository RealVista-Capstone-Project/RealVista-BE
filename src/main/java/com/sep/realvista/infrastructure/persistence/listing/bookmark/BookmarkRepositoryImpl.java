package com.sep.realvista.infrastructure.persistence.listing.bookmark;

import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of BookmarkRepository interface.
 *
 * Bridges the domain repository contract with Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryImpl implements BookmarkRepository {

    private final BookmarkJpaRepository jpaRepository;

    @Override
    public Bookmark save(Bookmark bookmark) {
        return jpaRepository.save(bookmark);
    }

    @Override
    public Optional<Bookmark> findByUserIdAndListingId(UUID userId, UUID listingId) {
        return jpaRepository.findByUserIdAndListingId(userId, listingId);
    }

    @Override
    public void deleteByUserIdAndListingId(UUID userId, UUID listingId) {
        jpaRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    @Override
    public boolean existsByUserIdAndListingId(UUID userId, UUID listingId) {
        return jpaRepository.existsByUserIdAndListingId(userId, listingId);
    }

    @Override
    public Page<Bookmark> findBookmarksByUserWithFilters(
            UUID userId,
            List<UUID> propertyTypeIds,
            ListingType listingType,
            Pageable pageable
    ) {
        // Empty list should be treated as null for the query
        List<UUID> effectivePropertyTypeIds = (propertyTypeIds != null && !propertyTypeIds.isEmpty())
                ? propertyTypeIds
                : null;

        // Determine sort direction from pageable
        // If no sort specified or if sorting by createdAt ascending, use ascending query
        boolean isAscending = pageable.getSort().stream()
                .anyMatch(order -> "createdAt".equals(order.getProperty())
                        && order.isAscending());

        if (isAscending) {
            return jpaRepository.findByUserIdWithFiltersOrderByCreatedAtAsc(
                    userId,
                    effectivePropertyTypeIds,
                    listingType,
                    pageable
            );
        } else {
            // Default to newest first (descending)
            return jpaRepository.findByUserIdWithFiltersOrderByCreatedAtDesc(
                    userId,
                    effectivePropertyTypeIds,
                    listingType,
                    pageable
            );
        }
    }
}