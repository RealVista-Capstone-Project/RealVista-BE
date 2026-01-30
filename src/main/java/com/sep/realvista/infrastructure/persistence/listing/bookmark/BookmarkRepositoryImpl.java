package com.sep.realvista.infrastructure.persistence.listing.bookmark;

import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}