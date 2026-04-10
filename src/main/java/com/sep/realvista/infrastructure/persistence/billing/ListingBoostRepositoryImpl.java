package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.BoostType;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.ListingBoostStatus;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListingBoostRepositoryImpl implements ListingBoostRepository {

    private final ListingBoostJpaRepository jpa;

    @Override
    public ListingBoost save(ListingBoost listingBoost) {
        return jpa.save(listingBoost);
    }

    @Override
    public Optional<ListingBoost> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<ListingBoost> findActiveByListingId(UUID listingId) {
        return jpa.findByListingIdAndStatusAndDeletedFalse(listingId, ListingBoostStatus.ACTIVE);
    }

    @Override
    public Optional<ListingBoost> findActiveByListingIdAndBoostType(UUID listingId, BoostType boostType) {
        return jpa.findByListingIdAndBoostTypeAndStatusAndDeletedFalse(
                listingId, boostType, ListingBoostStatus.ACTIVE);
    }

    @Override
    public List<ListingBoost> findActiveByUserId(UUID userId) {
        return jpa.findByUserIdAndStatusAndDeletedFalse(userId, ListingBoostStatus.ACTIVE);
    }

    @Override
    public List<ListingBoost> findActiveByListingIds(List<UUID> listingIds) {
        return jpa.findByListingIdInAndStatusAndDeletedFalse(listingIds, ListingBoostStatus.ACTIVE);
    }
}
