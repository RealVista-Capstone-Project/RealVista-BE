package com.sep.realvista.domain.listing.appointment;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID listingId, LocalDate date) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        User owner = listing.getUser(); // Assuming listing has User relation loaded
        if (owner == null) {
             // Fallback if lazy loading issue, though repository should handle it if EntityGraph used
             // For now assuming it is loaded or we fetch it. 
             // Better to fetch owner directly if needed, but Listing.getUser() is @ManyToOne
             owner = userRepository.findById(listing.getUserId())
                     .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        }

        LocalTime start = owner.getWorkingStartTime();
        LocalTime end = owner.getWorkingEndTime();
        
        if (start == null) {
            start = LocalTime.of(8, 0);
        }
        if (end == null) {
            end = LocalTime.of(17, 0);
        }

        List<LocalTime> allSlots = new ArrayList<>();
        LocalTime current = start;
        while (current.isBefore(end)) {
            allSlots.add(current);
            current = current.plusMinutes(30);
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        List<Appointment> existingAppointments = appointmentRepository.findByReceiverIdAndStartTimeBetweenAndStatusIn(
                owner.getUserId(),
                dayStart,
                dayEnd,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED)
        );

        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slotTime : allSlots) {
            LocalDateTime slotDateTime = date.atTime(slotTime);
            boolean isBooked = existingAppointments.stream().anyMatch(appt -> {
                // Check if slot overlaps with appointment
                // Simple case: exact match of start time
                // Extended: intersection logic if appointments vary in length
                // For now assuming 30 min slots match
                return appt.getStartTime().isEqual(slotDateTime)
                        || (appt.getStartTime().isBefore(slotDateTime.plusMinutes(30))
                        && appt.getEndTime().isAfter(slotDateTime));
            });

            if (!isBooked) {
                availableSlots.add(slotTime);
            }
        }

        return availableSlots;
    }

    @Transactional
    public void bookTour(UUID listingId, UUID senderId, List<LocalDateTime> selectedSlots, String notes) {
        // Simple implementation: book all selected slots as separate appointments or one? 
        // Plan said "Select up to 3 times", usually implies alternative options.
        // But if they are distinct slots, we create appointments.
        // Let's create one appointment per slot for now, or maybe the user meant "preferable times"?
        // "Select up to 3 times" -> usually implies requesting *one* tour but giving 3 options.
        // However, the requirement says "Book Tour". 
        // Let's assume we create PENDING appointments for all of them, and owner picks one?
        // Or if it's "Direct Booking" (auto verify), but user said "Approved by agent".
        // So we create 3 PENDING appointments. Agent accepts one, others might be auto-rejected? 
        // Or just create 3 requests.
        
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        for (LocalDateTime slot : selectedSlots) {
            List<Appointment> existing = appointmentRepository.findByReceiverIdAndStartTimeBetweenAndStatusIn(
                    listing.getUserId(),
                    slot,
                    slot,
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED)
            );

            if (!existing.isEmpty()) {
                throw new IllegalStateException("Slot " + slot + " is already booked.");
            }

            Appointment appointment = Appointment.builder()
                    .listingId(listingId)
                    .listing(listing)
                    .senderId(senderId)
                    .sender(sender)
                    .receiverId(listing.getUserId())
                    .receiver(listing.getUser())
                    .startTime(slot)
                    .endTime(slot.plusMinutes(30))
                    .status(AppointmentStatus.PENDING)
                    .appointmentType(AppointmentType.TOUR)
                    .senderNotes(notes)
                    .build();
            appointmentRepository.save(appointment);
        }
    }
}
