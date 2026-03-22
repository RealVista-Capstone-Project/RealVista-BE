package com.sep.realvista.infrastructure.slug;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.shared.util.ShortIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Component to regenerate listing slugs on application startup
 * Ensures all slugs use proper Base62-encoded short UUIDs
 * Runs once after V42 migration to fix temporary slugs
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlugRegenerator {

    private final ListingRepository listingRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void regenerateSlugs() {
        log.info("Starting slug regeneration with Base62 short UUIDs...");

        try {
            List<Listing> listings = listingRepository.findAll();
            int updated = 0;

            for (Listing listing : listings) {
                String currentSlug = listing.getSlug();
                String newSlug = ShortIdUtils.generateSlug(listing.getName(), listing.getListingId());

                // Only update if slug changed (not already Base62 format)
                if (!newSlug.equals(currentSlug)) {
                    listing.updateSlug(newSlug);
                    listingRepository.save(listing);
                    updated++;
                }
            }

            if (updated > 0) {
                log.info("Slug regeneration completed: {} listings updated", updated);
            } else {
                log.info("Slug regeneration skipped: all slugs already in correct format");
            }
        } catch (Exception e) {
            log.error("Failed to regenerate slugs", e);
            // Don't throw - allow application to start even if slug regeneration fails
        }
    }
}
