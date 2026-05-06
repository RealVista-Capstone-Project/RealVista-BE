package com.sep.realvista.unit.presentation.rest.appointment;

import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.presentation.rest.appointment.AppointmentController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController Unit Tests")
class AppointmentControllerTest {

    @Mock
    private AppointmentApplicationService appointmentApplicationService;

    @InjectMocks
    private AppointmentController appointmentController;

    @Test
    @DisplayName("getAvailableSlots should return 200 and clean up MDC")
    void getAvailableSlotsShouldReturn200AndCleanMdc() {
        // Arrange
        UUID listingId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        List<LocalTime> slots = List.of(LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(appointmentApplicationService.getAvailableSlots(listingId, date, null)).thenReturn(slots);

        // Act
        var response = appointmentController.getAvailableSlots(listingId, date, null);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(MDC.get("traceId")).isNull();
    }
}
