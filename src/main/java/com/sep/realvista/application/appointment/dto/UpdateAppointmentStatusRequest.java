package com.sep.realvista.application.appointment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentStatusRequest {
    @Schema(description = "New status for the appointment", example = "ACCEPTED")
    @JsonProperty("status")
    @NotNull(message = "Status is required")
    private String status;

    @Schema(description = "Reason for rejection or cancellation", example = "Unable to make it on that day")
    private String reason;
}