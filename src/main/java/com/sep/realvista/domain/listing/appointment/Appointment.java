package com.sep.realvista.domain.listing.appointment;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_listing", columnList = "listing_id"),
        @Index(name = "idx_appointment_sender", columnList = "sender_id"),
        @Index(name = "idx_appointment_receiver", columnList = "receiver_id"),
        @Index(name = "idx_appointment_status", columnList = "status"),
        @Index(name = "idx_appointment_type", columnList = "appointment_type"),
        @Index(name = "idx_appointment_start", columnList = "start_time")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", insertable = false, updatable = false)
    private User receiver;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false, length = 20)
    private AppointmentType appointmentType;

    @Column(name = "sender_notes", columnDefinition = "TEXT")
    private String senderNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "canceled_by_user_id")
    private UUID canceledByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by_user_id", insertable = false, updatable = false)
    private User canceledByUser;

    @Column(name = "reminder_before")
    @Builder.Default
    private Integer reminderBefore = 60;

    public void accept() {
        if (status != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only pending appointments can be accepted");
        }
        this.status = AppointmentStatus.ACCEPTED;
    }

    public void reject(String reason) {
        if (status != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only pending appointments can be rejected");
        }
        this.status = AppointmentStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void cancel(UUID userId, String reason) {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Cannot cancel completed or already canceled appointments");
        }
        this.status = AppointmentStatus.CANCELED;
        this.canceledByUserId = userId;
        this.cancellationReason = reason;
    }

    public void complete() {
        if (status != AppointmentStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted appointments can be completed");
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    public void reschedule(LocalDateTime newStartTime, LocalDateTime newEndTime) {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Cannot reschedule completed or canceled appointments");
        }
        this.startTime = newStartTime;
        this.endTime = newEndTime;
        this.status = AppointmentStatus.PENDING;
    }

    public boolean isTour() {
        return appointmentType == AppointmentType.TOUR;
    }

    public boolean isBlock() {
        return appointmentType == AppointmentType.BLOCK;
    }

    public boolean isUpcoming() {
        return startTime.isAfter(LocalDateTime.now()) && status == AppointmentStatus.ACCEPTED;
    }
}
