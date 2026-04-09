package com.sep.realvista.application.engagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyAgentProposalRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Commission rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Commission rate must be greater than 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Commission rate cannot exceed 100")
    private BigDecimal commissionRate;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @NotBlank(message = "Pitch content is required")
    private String pitchContent;
}
