package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingAnalyticsDTO;
import com.sep.realvista.domain.listing.analytics.ListingView;
import com.sep.realvista.domain.listing.analytics.ListingViewRepository;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for listing analytics operations.
 * <p>
 * Handles view tracking and analytics aggregation for listings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ListingAnalyticsService {

    private final ListingViewRepository listingViewRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Record a view for a listing by a user.
     * If the user has already viewed the listing, increment the view count.
     * Otherwise, create a new view record.
     * <p>
     * This method is asynchronous to avoid slowing down the listing detail response.
     *
     * @param listingId the listing ID
     * @param userId    the user ID
     */
    @Async
    @Transactional
    public void recordView(UUID listingId, UUID userId) {
        try {
            log.debug("Recording view for listing: {} by user: {}", listingId, userId);

            Optional<ListingView> existingView = listingViewRepository.findByListingIdAndUserId(listingId, userId);

            if (existingView.isPresent()) {
                ListingView view = existingView.get();
                view.incrementViewCount();
                listingViewRepository.save(view);
                log.debug("Incremented view count to {} for listing: {} by user: {}",
                        view.getViewCount(), listingId, userId);
            } else {
                ListingView newView = ListingView.builder()
                        .listingId(listingId)
                        .userId(userId)
                        .viewCount(1)
                        .build();
                listingViewRepository.save(newView);
                log.debug("Created new view record for listing: {} by user: {}", listingId, userId);
            }
        } catch (Exception e) {
            log.error("Failed to record view for listing: {} by user: {}", listingId, userId, e);
            // Don't rethrow - view tracking failure should not affect user experience
        }
    }

    /**
     * Get aggregated analytics for a listing.
     * <p>
     * Calculates:
     * - Total views (sum of all view_count)
     * - Unique viewers (distinct users)
     * - Tour bookings (appointments with type=TOUR)
     * - Conversion rate (tour bookings / total views * 100)
     *
     * @param listingId the listing ID
     * @return the analytics DTO
     */
    @Transactional(readOnly = true)
    public ListingAnalyticsDTO getListingAnalytics(UUID listingId) {
        log.debug("Fetching analytics for listing: {}", listingId);

        // Get view metrics
        Integer totalViews = listingViewRepository.getTotalViewCountByListingId(listingId);
        Integer uniqueViewers = listingViewRepository.countDistinctUsersByListingId(listingId);

        // Get tour booking count
        long tourBookingsLong = appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getListingId().equals(listingId))
                .filter(Appointment::isTour)
                .count();
        Integer tourBookings = (int) tourBookingsLong;

        // Calculate conversion rate
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalViews > 0) {
            conversionRate = BigDecimal.valueOf(tourBookings)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalViews), 2, RoundingMode.HALF_UP);
        }

        log.debug("Analytics for listing {}: totalViews={}, uniqueViewers={}, tourBookings={}, conversionRate={}",
                listingId, totalViews, uniqueViewers, tourBookings, conversionRate);

        return ListingAnalyticsDTO.builder()
                .totalViews(totalViews)
                .uniqueViewers(uniqueViewers)
                .tourBookings(tourBookings)
                .conversionRate(conversionRate)
                .build();
    }
}
