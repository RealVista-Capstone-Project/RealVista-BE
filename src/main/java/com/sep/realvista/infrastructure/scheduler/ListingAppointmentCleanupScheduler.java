package com.sep.realvista.infrastructure.scheduler;

import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingAppointmentCleanupScheduler {

    private final ListingRepository listingRepository;
    private final AppointmentApplicationService appointmentApplicationService;

    /**
     * Runs every hour to clean up appointments for listings that have been in DRAFT status
     * for more than 24 hours (Grace Period Expiry).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupStaleDraftAppointments() {
        log.info("Starting scheduled cleanup for stale DRAFT listings...");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Listing> staleDrafts = listingRepository.findByStatusAndUpdatedAtBefore(ListingStatus.DRAFT, cutoff);

        if (staleDrafts.isEmpty()) {
            log.info("No stale DRAFT listings found for cleanup.");
            return;
        }

        log.info("Found {} stale DRAFT listings. Triggering appointment cancellation...", staleDrafts.size());

        for (Listing listing : staleDrafts) {
            try {
                // Use a system-level reason for automatic cleanup
                String reason = "Listing has been unpublished for more than 24 hours.";
                appointmentApplicationService.cancelActiveAppointmentsByListingId(
                        listing.getListingId(), listing.getUserId(), reason);
                
                log.info("Successfully cleaned up appointments for listing: {}", listing.getListingId());
            } catch (Exception e) {
                log.error("Failed to cleanup appointments for listing {}: {}", 
                        listing.getListingId(), e.getMessage());
            }
        }
    }
}
