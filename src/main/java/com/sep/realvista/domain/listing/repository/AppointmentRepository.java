package com.sep.realvista.domain.listing.repository;

import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByReceiverIdAndStartTimeBetweenAndStatusIn(
            UUID receiverId,
            LocalDateTime start,
            LocalDateTime end,
            List<AppointmentStatus> statuses
    );


}
