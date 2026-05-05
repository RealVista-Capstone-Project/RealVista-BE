package com.sep.realvista.application.appointment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondRescheduleRequest {
    @JsonProperty("confirm")
    private Boolean confirm;

    @JsonProperty("reason")
    private String reason;
}
