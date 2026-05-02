package com.sep.realvista.domain.listing.repository;

import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    @Query("SELECT a FROM Appointment a WHERE a.receiverId = :receiverId "
            + "AND a.deleted = false "
            + "AND a.startTime BETWEEN :start AND :end "
            + "AND a.status IN :statuses")
    List<Appointment> findByReceiverIdAndStartTimeBetweenAndStatusIn(
            UUID receiverId,
            LocalDateTime start,
            LocalDateTime end,
            List<AppointmentStatus> statuses
    );

    @Query("SELECT a FROM Appointment a WHERE (a.senderId = :userId OR a.receiverId = :userId) "
            + "AND a.deleted = false "
            + "AND a.startTime >= :startDate AND a.startTime < :endDate "
            + "ORDER BY a.startTime ASC")
    List<Appointment> findByUserIdAndDateRange(
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT a FROM Appointment a WHERE (a.senderId = :userId OR a.receiverId = :userId) "
            + "AND a.deleted = false "
            + "AND a.startTime >= :startDate AND a.startTime < :endDate "
            + "AND a.status IN :statuses ORDER BY a.startTime ASC")
    List<Appointment> findByUserIdAndDateRangeAndStatusIn(
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<AppointmentStatus> statuses
    );

    List<Appointment> findByAppointmentIdAndStatus(UUID appointmentId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.listingId = :listingId "
            + "AND a.deleted = false "
            + "AND a.status IN :statuses")
    List<Appointment> findByListingIdAndStatusIn(UUID listingId, List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE a.listingId IN :listingIds "
            + "AND a.deleted = false "
            + "AND a.status IN :statuses")
    List<Appointment> findByListingIdInAndStatusIn(List<UUID> listingIds, List<AppointmentStatus> statuses);

    List<Appointment> findByListingIdInAndDeletedFalse(List<UUID> listingIds);

    @Query("SELECT a FROM Appointment a WHERE a.deleted = false "
            + "AND a.status = :status "
            + "AND a.endTime < :endTime")
    List<Appointment> findByStatusAndEndTimeBefore(AppointmentStatus status, LocalDateTime endTime);
}
