package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.search.MapSearchCriteria;
import com.sep.realvista.domain.listing.similarity.SimilarListing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ListingRepositoryImpl implements ListingRepository {

    private final ListingJpaRepository jpaRepository;
    private final ListingCustomRepository customRepository;

    @Override
    public Page<Listing> findAll(Specification<Listing> spec, Pageable pageable) {
        return jpaRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<String> findThumbnailByListingId(UUID listingId) {
        return jpaRepository.findThumbnailByListingId(listingId);
    }

    @Override
    public Listing save(Listing listing) {
        return jpaRepository.save(listing);
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return jpaRepository.findActiveById(id);
    }

    @Override
    public Optional<Listing> findBySlug(String slug) {
        return jpaRepository.findBySlugAndDeletedFalse(slug);
    }

    @Override
    public List<Listing> findAll() {
        return jpaRepository.findByDeletedFalse();
    }

    @Override
    public List<Listing> findByPropertyId(UUID propertyId) {
        return jpaRepository.findByPropertyId(propertyId);
    }

    @Override
    public List<Listing> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<Listing> findByUserIdOrPropertyOwnerId(UUID userId) {
        return jpaRepository.findByUserIdOrPropertyOwnerId(userId);
    }

    @Override
    public List<Listing> findByStatus(ListingStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Listing> findByListingTypeAndStatus(ListingType listingType,
                                                    ListingStatus status) {
        return jpaRepository.findByListingTypeAndStatus(listingType, status);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByPropertyIdAndListingTypeAndStatusAndUserId(
            UUID propertyId, ListingType listingType, ListingStatus status, UUID userId) {
        return jpaRepository.existsByPropertyIdAndListingTypeAndStatusAndUserId(
                propertyId, listingType, status, userId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public List<Listing> findPublishedWithinBounds(MapSearchCriteria criteria) {
        String typeStr = criteria.getListingTypeStr();
        int offset = criteria.getOffset();

        List<String> categories = criteria.getCategories() != null && !criteria.getCategories().isEmpty()
                ? criteria.getCategories().stream().map(String::toLowerCase).toList()
                : Collections.emptyList();
        boolean filterByCategory = !categories.isEmpty();

        if ("price".equalsIgnoreCase(criteria.getSortBy())) {
            if ("asc".equalsIgnoreCase(criteria.getSortDirection())) {
                return jpaRepository.findPublishedWithinBoundsSortByPriceAsc(
                        criteria.getBounds(), typeStr,
                        criteria.getMinPrice(), criteria.getMaxPrice(),
                        criteria.getSearchText(), categories, filterByCategory,
                        criteria.getPropertyCategory(), criteria.getPropertyType(),
                        criteria.getBedrooms(), criteria.getBathrooms(),
                        criteria.getArea(), criteria.getSize(), offset);
            }
            return jpaRepository.findPublishedWithinBoundsSortByPriceDesc(
                    criteria.getBounds(), typeStr,
                    criteria.getMinPrice(), criteria.getMaxPrice(),
                    criteria.getSearchText(), categories, filterByCategory,
                    criteria.getPropertyCategory(), criteria.getPropertyType(),
                    criteria.getBedrooms(), criteria.getBathrooms(),
                    criteria.getArea(), criteria.getSize(), offset);
        } else if ("createdAt".equalsIgnoreCase(criteria.getSortBy())) {
            return jpaRepository.findPublishedWithinBoundsSortByCreatedAt(
                    criteria.getBounds(), typeStr,
                    criteria.getMinPrice(), criteria.getMaxPrice(),
                    criteria.getSearchText(), categories, filterByCategory,
                    criteria.getPropertyCategory(), criteria.getPropertyType(),
                    criteria.getBedrooms(), criteria.getBathrooms(),
                    criteria.getArea(), criteria.getSize(), offset);
        }

        // Default: sort by publishedAt DESC
        return jpaRepository.findPublishedWithinBoundsSortByPublishedAt(
                criteria.getBounds(), typeStr,
                criteria.getMinPrice(), criteria.getMaxPrice(),
                criteria.getSearchText(), categories, filterByCategory,
                criteria.getPropertyCategory(), criteria.getPropertyType(),
                criteria.getBedrooms(), criteria.getBathrooms(),
                criteria.getArea(), criteria.getSize(), offset);
    }

    @Override
    public Long countPublishedWithinBounds(MapSearchCriteria criteria) {
        List<String> categories = criteria.getCategories() != null && !criteria.getCategories().isEmpty()
                ? criteria.getCategories().stream().map(String::toLowerCase).toList()
                : Collections.emptyList();
        boolean filterByCategory = !categories.isEmpty();

        return jpaRepository.countPublishedWithinBounds(
                criteria.getBounds(), criteria.getListingTypeStr(),
                criteria.getMinPrice(), criteria.getMaxPrice(),
                criteria.getSearchText(), categories, filterByCategory,
                criteria.getPropertyCategory(), criteria.getPropertyType(),
                criteria.getBedrooms(), criteria.getBathrooms(),
                criteria.getArea());
    }

    @Override
    public List<SimilarListing> findSimilarListings(UUID listingId, int limit) {
        log.debug("Finding similar listings for listingId: {}, limit: {}", listingId, limit);
        return customRepository.findSimilarListings(listingId, limit);
    }

    @Override
    public List<Listing> findPublishedListingsPublishedBefore(LocalDateTime cutoff) {
        log.debug("Finding published listings published before: {}", cutoff);
        return jpaRepository.findPublishedListingsPublishedBefore(cutoff);
    }
}
