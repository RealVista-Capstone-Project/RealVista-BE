package com.sep.realvista.domain.billing.boost.repository;

import com.sep.realvista.domain.billing.boost.ListingBoost;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListingBoostRepository {
    List<ListingBoost> findAllActiveByListingIds(List<UUID> listingIds, LocalDate now);
}
