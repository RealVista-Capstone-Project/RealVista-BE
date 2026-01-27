package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListingMediaRepositoryImpl implements ListingMediaRepository {

    private final ListingMediaJpaRepository jpaRepository;

    @Override
    public ListingMedia save(ListingMedia listingMedia) {
        return jpaRepository.save(listingMedia);
    }

    @Override
    public Optional<ListingMedia> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ListingMedia> findByListingId(UUID listingId) {
        return jpaRepository.findByListingId(listingId);
    }

    @Override
    public List<ListingMedia> findByListingIdOrderByDisplayOrderAsc(UUID listingId) {
        return jpaRepository.findByListingIdOrderByDisplayOrder(listingId);
    }

    @Override
    public Optional<ListingMedia> findPrimaryByListingId(UUID listingId) {
        return jpaRepository.findPrimaryByListingId(listingId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByListingId(UUID listingId) {
        List<ListingMedia> mediaList = findByListingId(listingId);
        mediaList.forEach(media -> {
            media.markAsDeleted();
            jpaRepository.save(media);
        });
    }
}
