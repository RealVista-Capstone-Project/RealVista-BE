package com.sep.realvista.domain.listing.appointment;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "listing_id")
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
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType;

    @Column(name = "sender_notes", columnDefinition = "TEXT")
    private String senderNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "canceled_by_user_id")
    private UUID canceledByUserId;

    @Column(name = "last_modified_by_user_id")
    private UUID lastModifiedByUserId;

    @Column(name = "reschedule_reason", columnDefinition = "TEXT")
    private String rescheduleReason;

    @Column(name = "proposed_start_time")
    private LocalDateTime proposedStartTime;

    @Column(name = "proposed_end_time")
    private LocalDateTime proposedEndTime;

    @Column(name = "deleted")
    private boolean deleted;

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

    public void cancel(UUID actorId, String reason) {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Cannot cancel completed or already canceled appointments");
        }
        this.status = AppointmentStatus.CANCELED;
        this.cancellationReason = reason;
        this.canceledByUserId = actorId;
    }

    public void complete() {
        if (status != AppointmentStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted appointments can be marked as completed");
        }
        if (LocalDateTime.now().isBefore(this.startTime)) {
            throw new BusinessConflictException("Cannot complete an appointment that hasn't started yet");
        }
        this.status = AppointmentStatus.COMPLETED;
    }
    public void proposeReschedule(UUID actorId, LocalDateTime newStartTime, LocalDateTime newEndTime, String reason) {
        if (this.status != AppointmentStatus.PENDING
            && this.status != AppointmentStatus.ACCEPTED
            && this.status != AppointmentStatus.RESCHEDULE_PENDING) {
            throw new BusinessConflictException("Appointment is not in a status that allows rescheduling");
        }

        // Prevent rescheduling if it's less than 2 hours before the original start time
        if (LocalDateTime.now().isAfter(this.startTime.minusHours(2))) {
            throw new BusinessConflictException(
                "Cannot reschedule an appointment less than 2 hours before the start time");
        }

        if (newStartTime.isBefore(LocalDateTime.now())) {
            throw new BusinessConflictException("New start time cannot be in the past");
        }
        if (newEndTime.isBefore(newStartTime)) {
            throw new BusinessConflictException("End time must be after start time");
        }

        if (this.status == AppointmentStatus.PENDING) {
            // For PENDING appointments, we update the time directly.
            this.startTime = newStartTime;
            this.endTime = newEndTime;
            // Clear any previous proposals just in case
            this.proposedStartTime = null;
            this.proposedEndTime = null;
        } else {
            // For ACCEPTED or RESCHEDULE_PENDING, we propose/update the proposal.
            this.status = AppointmentStatus.RESCHEDULE_PENDING;
            this.proposedStartTime = newStartTime;
            this.proposedEndTime = newEndTime;
        }
        
        this.rescheduleReason = reason;
        this.lastModifiedByUserId = actorId;
    }

    public void confirmReschedule(UUID actorId) {
        if (this.status != AppointmentStatus.RESCHEDULE_PENDING) {
            throw new BusinessConflictException("Appointment is not in reschedule pending status");
        }
        if (actorId.equals(this.lastModifiedByUserId)) {
            throw new BusinessConflictException("You cannot confirm your own reschedule proposal");
        }
        
        // Finalize the change
        this.startTime = this.proposedStartTime;
        this.endTime = this.proposedEndTime;
        this.proposedStartTime = null;
        this.proposedEndTime = null;
        this.status = AppointmentStatus.ACCEPTED;
        this.lastModifiedByUserId = actorId;
    }

    public void rejectReschedule(UUID actorId, String reason) {
        if (this.status != AppointmentStatus.RESCHEDULE_PENDING) {
            throw new BusinessConflictException("Appointment is not in reschedule pending status");
        }
        if (actorId.equals(this.lastModifiedByUserId)) {
            throw new BusinessConflictException("You cannot reject your own reschedule proposal");
        }
        
        // Revert to original state (keep the original time)
        this.proposedStartTime = null;
        this.proposedEndTime = null;
        // If it was a reschedule, it was either PENDING or ACCEPTED. 
        // For now, we revert to ACCEPTED as it's the most common case for confirmed tours,
        // but we should ideally track the previous status. 
        // To fix the "logic is accept" confusion, we should ensure the user understands 
        // that the ORIGINAL appointment is what's now ACCEPTED.
        this.status = AppointmentStatus.ACCEPTED;
        this.rescheduleReason = reason;
        this.lastModifiedByUserId = actorId;
    }

    public void cancelRescheduleProposal(UUID actorId) {
        if (this.status != AppointmentStatus.RESCHEDULE_PENDING) {
            throw new BusinessConflictException("No pending reschedule proposal to cancel");
        }
        if (!actorId.equals(this.lastModifiedByUserId)) {
            throw new BusinessConflictException("Only the proposer can cancel the reschedule request");
        }

        // Revert to T1
        this.proposedStartTime = null;
        this.proposedEndTime = null;
        this.rescheduleReason = null;
        this.status = AppointmentStatus.ACCEPTED;
        this.lastModifiedByUserId = actorId;
    }

    public boolean isTour() {
        return appointmentType == AppointmentType.TOUR;
    }

    public boolean isBlock() {
        return appointmentType == AppointmentType.BLOCK;
    }

    public void markAsDeleted() {
        this.deleted = true;
    }
}
