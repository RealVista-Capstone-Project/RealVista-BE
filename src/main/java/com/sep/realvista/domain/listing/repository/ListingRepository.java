package com.sep.realvista.domain.listing.repository;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.search.MapSearchCriteria;
import com.sep.realvista.domain.listing.similarity.SimilarListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository {

    Listing save(Listing listing);

    List<Listing> saveAll(List<Listing> listings);

    Optional<Listing> findById(UUID id);

    Optional<Listing> findBySlug(String slug);

    List<Listing> findAll();

    List<Listing> findAllById(Iterable<UUID> ids);

    Page<Listing> findAll(Specification<Listing> spec, Pageable pageable);

    Optional<String> findThumbnailByListingId(UUID listingId);

    List<Listing> findByPropertyId(UUID propertyId);

    List<Listing> findByUserId(UUID userId);

    List<Listing> findByUserIdOrPropertyOwnerId(UUID userId);

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByListingTypeAndStatus(ListingType listingType, ListingStatus status);

    boolean existsById(UUID id);

    boolean existsByPropertyIdAndListingTypeAndStatusAndUserId(
            UUID propertyId, ListingType listingType, ListingStatus status, UUID userId);

    void deleteById(UUID id);

    void deleteAll();

    /**
     * Find published listings within geographical bounds with filters and sorting.
     *
     * @param criteria search criteria including bounds, filters, sort, and
     *                 pagination
     * @return list of published listings matching the criteria
     */
    List<Listing> findPublishedWithinBounds(MapSearchCriteria criteria);

    /**
     * Count published listings within geographical bounds with filters.
     *
     * @param criteria search criteria including bounds and filters
     * @return total count of matching listings
     */
    Long countPublishedWithinBounds(MapSearchCriteria criteria);

    /**
     * Find similar listings based on property type, price, area, and common
     * attributes.
     * Returns listings sorted by similarity score (descending) and published date
     * (descending).
     *
     * @param listingId the reference listing ID
     * @param limit     maximum number of results to return
     * @return list of similar listings with similarity scores
     */
    List<SimilarListing> findSimilarListings(UUID listingId, int limit);

    /**
     * Find all published listings whose publishedAt timestamp is before the given cutoff.
     * Used by the listing expiry scheduler to expire stale published listings.
     *
     * @param cutoff the threshold date-time; listings published before this are candidates for expiry
     * @return list of published listings older than the cutoff
     */
    List<Listing> findPublishedListingsPublishedBefore(LocalDateTime cutoff);

    /**
     * Find all published listings whose publishedAt timestamp is strictly before windowEnd
     * and greater than or equal to windowStart.
     * Used by the listing expiry scheduler to notify about stale published listings.
     *
     * @param windowStart the beginning of the time window
     * @param windowEnd the end of the time window
     * @return list of published listings within the window
     */
    List<Listing> findPublishedListingsPublishedBetween(LocalDateTime windowStart, LocalDateTime windowEnd);

    /**
     * Find listings by status whose updatedAt timestamp is before the given cutoff.
     * Used by the appointment cleanup scheduler for grace period cancellation.
     *
     * @param status the listing status (e.g., DRAFT)
     * @param cutoff the threshold date-time
     * @return list of listings matching the criteria
     */
    List<Listing> findByStatusAndUpdatedAtBefore(ListingStatus status, LocalDateTime cutoff);

    long count();

    long countByStatus(ListingStatus status);

    List<Listing> findTop10ByOrderByUpdatedAtDesc();

    List<Object[]> findTopAgents(int limit);
}

