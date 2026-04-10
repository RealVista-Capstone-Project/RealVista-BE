package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.ListingBoostStatus;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListingBoostRepositoryImpl implements ListingBoostRepository {

    private final ListingBoostJpaRepository jpaRepository;

    @Override
    public List<ListingBoost> findAllActiveByListingIds(List<UUID> listingIds, LocalDate now) {
        if (listingIds == null || listingIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllActiveByListingIds(listingIds, ListingBoostStatus.ACTIVE, now);
    }
}
