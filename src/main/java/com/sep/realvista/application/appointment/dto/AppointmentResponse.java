package com.sep.realvista.application.appointment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    @Schema(description = "Appointment ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("appointment_id")
    private UUID appointmentId;

    @Schema(description = "Listing ID (optional for BLOCK type)", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("listing_id")
    private UUID listingId;

    @Schema(description = "Listing name", example = "Sunrise Apartment")
    @JsonProperty("listing_name")
    private String listingName;

    @Schema(description = "Listing address", example = "123 Main Street, District 1, Ho Chi Minh City")
    @JsonProperty("listing_address")
    private String listingAddress;

    @Schema(description = "Sender (booker) ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("sender_id")
    private UUID senderId;

    @Schema(description = "Sender name", example = "John Doe")
    @JsonProperty("sender_name")
    private String senderName;

    @Schema(description = "Receiver (owner/agent) ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("receiver_id")
    private UUID receiverId;

    @Schema(description = "Receiver name", example = "Jane Smith")
    @JsonProperty("receiver_name")
    private String receiverName;

    @Schema(description = "Start time of the appointment")
    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @Schema(description = "End time of the appointment")
    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @Schema(description = "Current status of the appointment")
    private String status;

    @Schema(description = "Type of appointment (TOUR or BLOCK)")
    @JsonProperty("appointment_type")
    private String appointmentType;

    @Schema(description = "Notes from the sender")
    @JsonProperty("sender_notes")
    private String senderNotes;

    @Schema(description = "Rejection reason if rejected")
    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @Schema(description = "Cancellation reason if canceled")
    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @Schema(description = "ID of user who canceled (if applicable)")
    @JsonProperty("canceled_by_user_id")
    private UUID canceledByUserId;

    @Schema(description = "Whether the current user is the sender")
    @JsonProperty("is_sender")
    private boolean isSender;
}