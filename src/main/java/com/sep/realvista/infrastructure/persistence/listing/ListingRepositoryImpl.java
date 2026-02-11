package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.search.MapSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListingRepositoryImpl implements ListingRepository {

    private final ListingJpaRepository jpaRepository;

    @Override
    public Listing save(Listing listing) {
        return jpaRepository.save(listing);
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return jpaRepository.findActiveById(id);
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
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Listing> findPublishedWithinBounds(MapSearchCriteria criteria) {
        String typeStr = criteria.getListingTypeStr();
        int offset = criteria.getOffset();

        if ("price".equalsIgnoreCase(criteria.getSortBy())) {
            if ("asc".equalsIgnoreCase(criteria.getSortDirection())) {
                return jpaRepository.findPublishedWithinBoundsSortByPriceAsc(
                        criteria.getBounds(), typeStr,
                        criteria.getMinPrice(), criteria.getMaxPrice(),
                        criteria.getSearchText(), criteria.getCategory(),
                        criteria.getBedrooms(), criteria.getBathrooms(),
                        criteria.getArea(), criteria.getSize(), offset);
            }
            return jpaRepository.findPublishedWithinBoundsSortByPriceDesc(
                    criteria.getBounds(), typeStr,
                    criteria.getMinPrice(), criteria.getMaxPrice(),
                    criteria.getSearchText(), criteria.getCategory(),
                    criteria.getBedrooms(), criteria.getBathrooms(),
                    criteria.getArea(), criteria.getSize(), offset);
        } else if ("createdAt".equalsIgnoreCase(criteria.getSortBy())) {
            return jpaRepository.findPublishedWithinBoundsSortByCreatedAt(
                    criteria.getBounds(), typeStr,
                    criteria.getMinPrice(), criteria.getMaxPrice(),
                    criteria.getSearchText(), criteria.getCategory(),
                    criteria.getBedrooms(), criteria.getBathrooms(),
                    criteria.getArea(), criteria.getSize(), offset);
        }

        // Default: sort by publishedAt DESC
        return jpaRepository.findPublishedWithinBoundsSortByPublishedAt(
                criteria.getBounds(), typeStr,
                criteria.getMinPrice(), criteria.getMaxPrice(),
                criteria.getSearchText(), criteria.getCategory(),
                criteria.getBedrooms(), criteria.getBathrooms(),
                criteria.getArea(), criteria.getSize(), offset);
    }

    @Override
    public Long countPublishedWithinBounds(MapSearchCriteria criteria) {
        return jpaRepository.countPublishedWithinBounds(
                criteria.getBounds(), criteria.getListingTypeStr(),
                criteria.getMinPrice(), criteria.getMaxPrice(),
                criteria.getSearchText(), criteria.getCategory(),
                criteria.getBedrooms(), criteria.getBathrooms(),
                criteria.getArea());
    }
}
