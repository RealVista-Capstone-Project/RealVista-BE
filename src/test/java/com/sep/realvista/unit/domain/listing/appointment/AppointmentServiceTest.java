package com.sep.realvista.unit.domain.listing.appointment;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.InvalidBookingRequestException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentService;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.domain.listing.appointment.BookTourResult;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private UUID listingId;
    private UUID ownerId;
    private UUID senderId;
    private Listing listing;
    private User owner;
    private User sender;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        listing = Listing.builder()
                .listingId(listingId)
                .userId(ownerId)
                .name("Test Listing")
                .slug("test-listing")
                .price(BigDecimal.valueOf(1000))
                .listingType(ListingType.RENT)
                .build();

        owner = User.builder()
                .userId(ownerId)
                .email(Email.of("owner@test.com"))
                .passwordHash("hash")
                .businessName("Owner")
                .workingStartTime(LocalTime.of(8, 0))
                .workingEndTime(LocalTime.of(17, 0))
                .build();

        sender = User.builder()
                .userId(senderId)
                .email(Email.of("sender@test.com"))
                .passwordHash("hash")
                .businessName("Sender")
                .build();
    }

    @Nested
    @DisplayName("getAvailableSlots")
    class GetAvailableSlots {

        @Test
        @DisplayName("Should reject past date")
        void shouldRejectPastDate() {
            LocalDate pastDate = LocalDate.now().minusDays(1);

            assertThatThrownBy(() -> appointmentService.getAvailableSlots(listingId, pastDate, null))
                    .isInstanceOf(InvalidBookingRequestException.class)
                    .hasMessageContaining("past date");
        }

        @Test
        @DisplayName("Should throw when listing not found")
        void shouldThrowWhenListingNotFound() {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.getAvailableSlots(listingId, futureDate, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return all slots when no existing appointments")
        void shouldReturnAllSlotsWhenNoExistingAppointments() {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(appointmentRepository.findOverlappingAppointments(
                    any(), any(), any(), any(), any()
            )).thenReturn(Collections.emptyList());

            List<LocalTime> slots = appointmentService.getAvailableSlots(listingId, futureDate, null);

            // 8:00 to 17:00 in 30-min intervals = 18 slots
            assertThat(slots).hasSize(18);
            assertThat(slots.getFirst()).isEqualTo(LocalTime.of(8, 0));
            assertThat(slots.getLast()).isEqualTo(LocalTime.of(16, 30));
        }

        @Test
        @DisplayName("Should filter out booked slots using overlap detection")
        void shouldFilterOutBookedSlots() {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

            LocalDateTime bookedStart = futureDate.atTime(10, 0);
            LocalDateTime bookedEnd = futureDate.atTime(10, 30);
            Appointment bookedAppointment = Appointment.builder()
                    .startTime(bookedStart)
                    .endTime(bookedEnd)
                    .status(AppointmentStatus.PENDING)
                    .build();

            when(appointmentRepository.findOverlappingAppointments(
                    any(), any(), any(), any(), any()
            )).thenReturn(List.of(bookedAppointment));

            List<LocalTime> slots = appointmentService.getAvailableSlots(listingId, futureDate, null);

            assertThat(slots).doesNotContain(LocalTime.of(10, 0));
            assertThat(slots).hasSize(17);
        }
    }

    @Nested
    @DisplayName("bookTour")
    class BookTour {

        @Test
        @DisplayName("Should reject self-booking")
        void shouldRejectSelfBooking() {
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

            LocalDateTime futureSlot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

            assertThatThrownBy(() -> appointmentService.bookTour(
                    listingId, ownerId, List.of(futureSlot), "test"
            )).isInstanceOf(InvalidBookingRequestException.class)
                    .hasMessageContaining("own listing");
        }

        @Test
        @DisplayName("Should reject past timestamps")
        void shouldRejectPastTimestamps() {
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

            LocalDateTime pastSlot = LocalDateTime.now().minusHours(1);

            assertThatThrownBy(() -> appointmentService.bookTour(
                    listingId, senderId, List.of(pastSlot), "test"
            )).isInstanceOf(InvalidBookingRequestException.class)
                    .hasMessageContaining("past");
        }

        @Test
        @DisplayName("Should reject slot outside working hours")
        void shouldRejectSlotOutsideWorkingHours() {
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

            LocalDateTime lateSlot = LocalDateTime.now().plusDays(1).withHour(2).withMinute(0);

            assertThatThrownBy(() -> appointmentService.bookTour(
                    listingId, senderId, List.of(lateSlot), "test"
            )).isInstanceOf(InvalidBookingRequestException.class)
                    .hasMessageContaining("working hours");
        }

        @Test
        @DisplayName("Should reject conflicting slot")
        void shouldRejectConflictingSlot() {
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

            LocalDateTime futureSlot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            Appointment existing = Appointment.builder()
                    .startTime(futureSlot)
                    .endTime(futureSlot.plusMinutes(30))
                    .status(AppointmentStatus.PENDING)
                    .build();

            when(appointmentRepository.findOverlappingAppointments(
                    any(), any(), any(), any(), any()
            )).thenReturn(List.of(existing));

            assertThatThrownBy(() -> appointmentService.bookTour(
                    listingId, senderId, List.of(futureSlot), "test"
            )).isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("already booked");
        }

        @Test
        @DisplayName("Should save appointment on valid request")
        void shouldSaveAppointmentOnValidRequest() {
            when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(appointmentRepository.findOverlappingAppointments(
                    any(), any(), any(), any(), any()
            )).thenReturn(Collections.emptyList());
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            LocalDateTime futureSlot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

            BookTourResult result = appointmentService.bookTour(listingId, senderId, List.of(futureSlot), "test notes");

            verify(appointmentRepository).save(any(Appointment.class));
            assertThat(result.appointments()).hasSize(1);
            assertThat(result.listing()).isEqualTo(listing);
            assertThat(result.sender()).isEqualTo(sender);
            assertThat(result.owner()).isEqualTo(owner);
        }

        @Test
        @DisplayName("Should throw when listing not found")
        void shouldThrowWhenListingNotFound() {
            when(listingRepository.findById(listingId)).thenReturn(Optional.empty());
            LocalDateTime futureSlot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

            assertThatThrownBy(() -> appointmentService.bookTour(
                    listingId, senderId, List.of(futureSlot), "test"
            )).isInstanceOf(ResourceNotFoundException.class);

            verify(appointmentRepository, never()).save(any());
        }
    }
}
