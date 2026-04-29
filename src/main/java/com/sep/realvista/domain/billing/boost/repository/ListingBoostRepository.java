package com.sep.realvista.domain.billing.boost.repository;

import com.sep.realvista.domain.billing.boost.BoostType;
import com.sep.realvista.domain.billing.boost.ListingBoost;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingBoostRepository {

    ListingBoost save(ListingBoost listingBoost);

    Optional<ListingBoost> findById(UUID id);

    List<ListingBoost> findActiveByListingId(UUID listingId);

    Optional<ListingBoost> findActiveByListingIdAndBoostType(UUID listingId, BoostType boostType);

    List<ListingBoost> findActiveByUserId(UUID userId);

    List<ListingBoost> findActiveByListingIds(List<UUID> listingIds);

    List<ListingBoost> findAllActiveByListingIds(List<UUID> listingIds, LocalDate now);

    long countActiveByBoostPackageId(UUID boostPackageId);
}
