package com.sep.realvista.unit.application.appointment;

import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.application.appointment.dto.AppointmentSummaryResponse;
import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.domain.agent.lead.LeadSource;
import com.sep.realvista.domain.agent.lead.ListingLead;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentApplicationService Unit Tests")
class AppointmentApplicationServiceTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private ListingLeadRepository leadRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationApplicationService notificationApplicationService;

    @Mock
    private com.sep.realvista.domain.user.preference.SettingPreferenceRepository settingPreferenceRepository;

    @Mock
    private com.sep.realvista.infrastructure.service.NotificationMessageService notificationMessageService;

    @InjectMocks
    private AppointmentApplicationService applicationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(applicationService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    @DisplayName("Should delegate getAvailableSlots to domain service")
    void shouldDelegateGetAvailableSlots() {
        // Arrange
        UUID listingId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        List<LocalTime> expected = List.of(LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(appointmentService.getAvailableSlots(listingId, date, null)).thenReturn(expected);

        // Act
        List<LocalTime> result = applicationService.getAvailableSlots(listingId, date, null);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(appointmentService).getAvailableSlots(listingId, date, null);
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
                .appointmentId(UUID.randomUUID())
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
        when(leadRepository.findByAgentIdAndBuyerIdAndListingId(ownerId, userId, listingId))
                .thenReturn(Optional.empty());
        when(leadRepository.save(any(ListingLead.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(settingPreferenceRepository.findByUserId(any()))
                .thenReturn(java.util.Optional.empty());

        // Act
        applicationService.bookTour(userId, request);

        // Assert
        verify(appointmentService).bookTour(listingId, userId, List.of(slot), "test notes");
        ArgumentCaptor<ListingLead> leadCaptor = ArgumentCaptor.forClass(ListingLead.class);
        verify(leadRepository).save(leadCaptor.capture());
        ListingLead lead = leadCaptor.getValue();
        assertThat(lead.getAgentId()).isEqualTo(ownerId);
        assertThat(lead.getBuyerId()).isEqualTo(userId);
        assertThat(lead.getListingId()).isEqualTo(listingId);
        assertThat(lead.getSource()).isEqualTo(LeadSource.TOUR);
        assertThat(lead.getFullName()).isEqualTo("Sender");
        assertThat(lead.getEmail()).isEqualTo("sender@test.com");

        // Verify confirmation email sent to sender (DB template)
        verify(emailService).sendDbTemplateMessageAsync(
                eq("sender@test.com"),
                eq("TOUR_BOOKING_CONFIRMATION"),
                eq("vi"),
                anyMap()
        );

        // Verify notification email sent to owner
        verify(emailService).sendDbTemplateMessageAsync(
                eq("owner@test.com"),
                eq("TOUR_BOOKING_NOTIFICATION"),
                eq("vi"),
                anyMap()
        );

        // Verify in-app notifications sent to both owner and sender
        verify(notificationApplicationService, atLeastOnce())
                .sendDbNotification(any(), anyString(), anyString(), anyMap(), any(), any(), any());
    }

    @Test
    @DisplayName("Should exclude busy blocks from upcoming appointments in summary")
    void shouldExcludeBusyBlocksFromUpcomingAppointmentsInSummary() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        LocalDateTime futureSlot = LocalDateTime.now().plusHours(3);

        Appointment futureTour = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .senderId(userId)
                .receiverId(UUID.randomUUID())
                .startTime(futureSlot)
                .endTime(futureSlot.plusMinutes(30))
                .status(AppointmentStatus.ACCEPTED)
                .appointmentType(AppointmentType.TOUR)
                .build();

        Appointment futureBusyBlock = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .senderId(userId)
                .receiverId(userId)
                .startTime(futureSlot.plusHours(1))
                .endTime(futureSlot.plusHours(1).plusMinutes(30))
                .status(AppointmentStatus.ACCEPTED)
                .appointmentType(AppointmentType.BLOCK)
                .build();

        when(appointmentService.getAppointmentsByUserId(eq(userId), eq(start), eq(end), isNull()))
                .thenReturn(List.of(futureTour, futureBusyBlock));

        // Act
        AppointmentSummaryResponse result = applicationService.getAppointmentSummary(userId, start, end);

        // Assert
        assertThat(result.getUpcomingAppointments()).isEqualTo(1);
        assertThat(result.getTotalAppointments()).isEqualTo(2);
        assertThat(result.getAcceptedAppointments()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should keep legacy status counters unchanged in summary")
    void shouldKeepLegacyStatusCountersUnchangedInSummary() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        LocalDateTime now = LocalDateTime.now();

        Appointment pendingTour = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .senderId(userId)
                .receiverId(UUID.randomUUID())
                .startTime(now.plusHours(2))
                .endTime(now.plusHours(2).plusMinutes(30))
                .status(AppointmentStatus.PENDING)
                .appointmentType(AppointmentType.TOUR)
                .build();

        Appointment acceptedBlock = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .senderId(userId)
                .receiverId(userId)
                .startTime(now.plusHours(4))
                .endTime(now.plusHours(4).plusMinutes(30))
                .status(AppointmentStatus.ACCEPTED)
                .appointmentType(AppointmentType.BLOCK)
                .build();

        Appointment rejectedTour = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .senderId(userId)
                .receiverId(UUID.randomUUID())
                .startTime(now.minusHours(3))
                .endTime(now.minusHours(2).plusMinutes(30))
                .status(AppointmentStatus.REJECTED)
                .appointmentType(AppointmentType.TOUR)
                .build();

        Appointment canceledTour = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .senderId(userId)
                .receiverId(UUID.randomUUID())
                .startTime(now.minusHours(6))
                .endTime(now.minusHours(5).plusMinutes(30))
                .status(AppointmentStatus.CANCELED)
                .appointmentType(AppointmentType.TOUR)
                .build();

        Appointment completedTour = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .senderId(userId)
                .receiverId(UUID.randomUUID())
                .startTime(now.minusDays(1))
                .endTime(now.minusDays(1).plusMinutes(30))
                .status(AppointmentStatus.COMPLETED)
                .appointmentType(AppointmentType.TOUR)
                .build();

        when(appointmentService.getAppointmentsByUserId(eq(userId), eq(start), eq(end), isNull()))
                .thenReturn(List.of(pendingTour, acceptedBlock, rejectedTour, canceledTour, completedTour));

        // Act
        AppointmentSummaryResponse result = applicationService.getAppointmentSummary(userId, start, end);

        // Assert
        assertThat(result.getTotalAppointments()).isEqualTo(5);
        assertThat(result.getPendingAppointments()).isEqualTo(1);
        assertThat(result.getAcceptedAppointments()).isEqualTo(1);
        assertThat(result.getRejectedAppointments()).isEqualTo(1);
        assertThat(result.getCanceledAppointments()).isEqualTo(1);
        assertThat(result.getCompletedAppointments()).isEqualTo(1);
        assertThat(result.getUpcomingAppointments()).isEqualTo(1);
    }
}
