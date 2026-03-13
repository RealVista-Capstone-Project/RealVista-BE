package com.sep.realvista.unit.application.appointment;

import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentService;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.domain.listing.appointment.AppointmentType;
import com.sep.realvista.domain.listing.appointment.BookTourResult;
import com.sep.realvista.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentApplicationService Unit Tests")
class AppointmentApplicationServiceTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private EmailService emailService;

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
    @DisplayName("Should delegate bookTour to domain service and send emails")
    void shouldDelegateBookTourAndSendEmails() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        LocalDateTime slot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

        BookTourRequest request = BookTourRequest.builder()
                .listingId(listingId)
                .selectedSlots(List.of(slot))
                .notes("test notes")
                .build();

        User sender = User.builder()
                .userId(userId)
                .email(Email.of("sender@test.com"))
                .passwordHash("hash")
                .businessName("Sender")
                .firstName("John")
                .lastName("Doe")
                .build();

        User owner = User.builder()
                .userId(ownerId)
                .email(Email.of("owner@test.com"))
                .passwordHash("hash")
                .businessName("Owner")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Listing listing = Listing.builder()
                .listingId(listingId)
                .userId(ownerId)
                .name("Test Listing")
                .slug("test-listing")
                .price(BigDecimal.valueOf(1000))
                .listingType(ListingType.RENT)
                .build();

        Appointment appointment = Appointment.builder()
                .listingId(listingId)
                .listing(listing)
                .senderId(userId)
                .sender(sender)
                .receiverId(ownerId)
                .receiver(owner)
                .startTime(slot)
                .endTime(slot.plusMinutes(30))
                .status(AppointmentStatus.PENDING)
                .appointmentType(AppointmentType.TOUR)
                .senderNotes("test notes")
                .build();

        BookTourResult bookTourResult = new BookTourResult(
                List.of(appointment), listing, sender, owner
        );

        when(appointmentService.bookTour(listingId, userId, List.of(slot), "test notes"))
                .thenReturn(bookTourResult);

        // Act
        applicationService.bookTour(userId, request);

        // Assert
        verify(appointmentService).bookTour(listingId, userId, List.of(slot), "test notes");

        // Verify confirmation email sent to sender
        verify(emailService).sendTemplateMessageAsync(
                eq("sender@test.com"),
                eq("Đặt lịch tham quan: Test Listing"),
                eq("tour-booking-confirmation"),
                anyMap()
        );

        // Verify notification email sent to owner
        verify(emailService).sendTemplateMessageAsync(
                eq("owner@test.com"),
                eq("Yêu cầu tham quan mới: Test Listing"),
                eq("tour-booking-notification"),
                anyMap()
        );
    }
}
