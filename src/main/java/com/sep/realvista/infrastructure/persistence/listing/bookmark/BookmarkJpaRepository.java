package com.sep.realvista.infrastructure.persistence.listing.bookmark;

import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository interface for Bookmark entity.
 * 
 * Provides data access methods using Spring Data JPA.
 */
@Repository
public interface BookmarkJpaRepository extends JpaRepository<Bookmark, BookmarkId> {

    /**
     * Finds bookmark by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return optional bookmark
     */
    Optional<Bookmark> findByUserIdAndListingId(UUID userId, UUID listingId);

    /**
     * Deletes bookmark by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.userId = :userId AND b.listingId = :listingId")
    void deleteByUserIdAndListingId(@Param("userId") UUID userId, @Param("listingId") UUID listingId);

    /**
     * Checks if bookmark exists by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return true if exists
     */
    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);
}