package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingAnalyticsDTO;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import com.sep.realvista.domain.listing.analytics.ListingView;
import com.sep.realvista.domain.listing.analytics.ListingViewRepository;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ListingAnalyticsService}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListingAnalyticsService Unit Tests")
class ListingAnalyticsServiceTest {

    @Mock
    private ListingViewRepository listingViewRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingLeadRepository listingLeadRepository;

    @InjectMocks
    private ListingAnalyticsService listingAnalyticsService;

    private UUID listingId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("recordView should create new view record when user has not viewed listing")
    void recordView_NewUser_CreatesNewRecord() {
        // Given
        when(listingViewRepository.findByListingIdAndUserId(listingId, userId))
                .thenReturn(Optional.empty());

        // When
        listingAnalyticsService.recordView(listingId, userId);

        // Then
        verify(listingViewRepository).findByListingIdAndUserId(listingId, userId);
        verify(listingViewRepository).save(any(ListingView.class));
    }

    @Test
    @DisplayName("recordView should increment view count when user has already viewed listing")
    void recordView_ExistingUser_IncrementsViewCount() {
        // Given
        ListingView existingView = ListingView.builder()
                .listingId(listingId)
                .userId(userId)
                .viewCount(1)
                .build();
        when(listingViewRepository.findByListingIdAndUserId(listingId, userId))
                .thenReturn(Optional.of(existingView));

        // When
        listingAnalyticsService.recordView(listingId, userId);

        // Then
        verify(listingViewRepository).findByListingIdAndUserId(listingId, userId);
        verify(listingViewRepository).save(existingView);
        assertThat(existingView.getViewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("getListingAnalytics should return correct metrics with tour bookings")
    void getListingAnalytics_WithTourBookings_ReturnsCorrectMetrics() {
        // Given
        Integer totalViews = 100;
        Integer uniqueViewers = 45;
        
        Appointment tourAppointment1 = mock(Appointment.class);
        Appointment tourAppointment2 = mock(Appointment.class);
        Appointment nonTourAppointment = mock(Appointment.class);
        
        when(tourAppointment1.getListingId()).thenReturn(listingId);
        when(tourAppointment1.isTour()).thenReturn(true);
        when(tourAppointment2.getListingId()).thenReturn(listingId);
        when(tourAppointment2.isTour()).thenReturn(true);
        when(nonTourAppointment.getListingId()).thenReturn(listingId);
        when(nonTourAppointment.isTour()).thenReturn(false);
        
        when(listingViewRepository.getTotalViewCountByListingId(listingId)).thenReturn(totalViews);
        when(listingViewRepository.countDistinctUsersByListingId(listingId)).thenReturn(uniqueViewers);
        when(appointmentRepository.findAll()).thenReturn(List.of(tourAppointment1, tourAppointment2, nonTourAppointment));

        // When
        ListingAnalyticsDTO result = listingAnalyticsService.getListingAnalytics(listingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalViews()).isEqualTo(100);
        assertThat(result.getUniqueViewers()).isEqualTo(45);
        assertThat(result.getTourBookings()).isEqualTo(2);
        assertThat(result.getConversionRate()).isEqualTo(new BigDecimal("2.00"));
    }

    @Test
    @DisplayName("getListingAnalytics should return zero conversion rate when no views")
    void getListingAnalytics_NoViews_ReturnsZeroConversionRate() {
        // Given
        when(listingViewRepository.getTotalViewCountByListingId(listingId)).thenReturn(0);
        when(listingViewRepository.countDistinctUsersByListingId(listingId)).thenReturn(0);
        when(appointmentRepository.findAll()).thenReturn(List.of());

        // When
        ListingAnalyticsDTO result = listingAnalyticsService.getListingAnalytics(listingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalViews()).isEqualTo(0);
        assertThat(result.getUniqueViewers()).isEqualTo(0);
        assertThat(result.getTourBookings()).isEqualTo(0);
        assertThat(result.getConversionRate()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getListingAnalytics should calculate conversion rate correctly")
    void getListingAnalytics_CalculatesConversionRate() {
        // Given
        Integer totalViews = 200;
        Integer uniqueViewers = 75;
        
        Appointment tourAppointment = mock(Appointment.class);
        when(tourAppointment.getListingId()).thenReturn(listingId);
        when(tourAppointment.isTour()).thenReturn(true);
        
        when(listingViewRepository.getTotalViewCountByListingId(listingId)).thenReturn(totalViews);
        when(listingViewRepository.countDistinctUsersByListingId(listingId)).thenReturn(uniqueViewers);
        when(appointmentRepository.findAll()).thenReturn(List.of(tourAppointment));

        // When
        ListingAnalyticsDTO result = listingAnalyticsService.getListingAnalytics(listingId);

        // Then
        assertThat(result.getTotalViews()).isEqualTo(200);
        assertThat(result.getTourBookings()).isEqualTo(1);
        // 1 / 200 * 100 = 0.50%
        assertThat(result.getConversionRate()).isEqualTo(new BigDecimal("0.50"));
    }
}
