package com.sep.realvista.unit.application.appointment;

import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.domain.listing.appointment.AppointmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentApplicationService Unit Tests")
class AppointmentApplicationServiceTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentApplicationService applicationService;

    @Test
    @DisplayName("Should delegate getAvailableSlots to domain service")
    void shouldDelegateGetAvailableSlots() {
        // Arrange
        UUID listingId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        List<LocalTime> expected = List.of(LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(appointmentService.getAvailableSlots(listingId, date)).thenReturn(expected);

        // Act
        List<LocalTime> result = applicationService.getAvailableSlots(listingId, date);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(appointmentService).getAvailableSlots(listingId, date);
    }

    @Test
    @DisplayName("Should delegate bookTour to domain service")
    void shouldDelegateBookTour() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        LocalDateTime slot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

        BookTourRequest request = BookTourRequest.builder()
                .listingId(listingId)
                .selectedSlots(List.of(slot))
                .notes("test notes")
                .build();

        // Act
        applicationService.bookTour(userId, request);

        // Assert
        verify(appointmentService).bookTour(listingId, userId, List.of(slot), "test notes");
    }
}
