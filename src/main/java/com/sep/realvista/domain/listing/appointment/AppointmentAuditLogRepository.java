package com.sep.realvista.domain.listing.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AppointmentAuditLogRepository extends JpaRepository<AppointmentAuditLog, UUID> {
    List<AppointmentAuditLog> findByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);
}
