package com.sep.realvista.infrastructure.scheduler;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingExpiryScheduler {

    private final ListingRepository listingRepository;

    @Value("${realvista.listing.max-lifetime-days:14}")
    private long maxLifetimeDays;

    @Scheduled(cron = "${realvista.listing.expiry-cron:0 0 * * * *}")
    @Transactional
    public void expireStaleListings() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(maxLifetimeDays);
        log.info("Running listing expiry scheduler – cutoff: {}, maxLifetimeDays: {}", cutoff, maxLifetimeDays);

        List<Listing> expiredListings = listingRepository.findPublishedListingsPublishedBefore(cutoff);

        if (expiredListings.isEmpty()) {
            log.info("No expired listings found.");
            return;
        }

        log.info("Found {} listing(s) to expire.", expiredListings.size());

        int successCount = 0;
        int errorCount = 0;

        for (Listing listing : expiredListings) {
            try {
                log.info("Expiring listing [id={}, publishedAt={}]", listing.getListingId(), listing.getPublishedAt());
                listing.unpublish();
                listingRepository.save(listing);
                successCount++;
            } catch (Exception ex) {
                // Catch per-listing errors so a single bad record doesn't abort the whole batch.
                log.error("Failed to expire listing [id={}]: {}", listing.getListingId(), ex.getMessage(), ex);
                errorCount++;
            }
        }

        log.info("Listing expiry complete – expired: {}, errors: {}", successCount, errorCount);
    }
}
