package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.BoostType;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.ListingBoostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingBoostJpaRepository extends JpaRepository<ListingBoost, UUID> {

    List<ListingBoost> findByListingIdAndStatusAndDeletedFalse(UUID listingId, ListingBoostStatus status);

    Optional<ListingBoost> findByListingIdAndBoostTypeAndStatusAndDeletedFalse(
            UUID listingId, BoostType boostType, ListingBoostStatus status);

    List<ListingBoost> findByUserIdAndStatusAndDeletedFalse(UUID userId, ListingBoostStatus status);

    List<ListingBoost> findByListingIdInAndStatusAndDeletedFalse(List<UUID> listingIds, ListingBoostStatus status);
}
