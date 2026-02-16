package com.sep.realvista.application.appointment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookTourRequest {
    @Schema(description = "ID of the listing to book a tour for", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("listing_id")
    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @Schema(description = "List of preferred time slots for the tour", example = "[\"2023-10-27T10:00:00\"]")
    @JsonProperty("selected_slots")
    @NotEmpty(message = "At least one time slot must be selected")
    private List<LocalDateTime> selectedSlots;

    @Schema(description = "Optional notes for the agent/owner", example = "I prefer a morning tour if possible.")
    private String notes;
}
