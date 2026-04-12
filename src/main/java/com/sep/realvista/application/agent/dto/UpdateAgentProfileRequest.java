package com.sep.realvista.application.agent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Partial update for editable agent profile fields (PATCH).
 */
@Data
public class UpdateAgentProfileRequest {

    @Size(max = 8000)
    private String bio;

    @Size(max = 4000)
    private String specialties;

    @Size(max = 4000)
    private String serviceAreas;

    @Min(0)
    @Max(60)
    private Integer yearsOfExperience;
}
