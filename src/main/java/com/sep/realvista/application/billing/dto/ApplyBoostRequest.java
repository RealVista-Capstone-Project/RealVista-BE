package com.sep.realvista.application.billing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApplyBoostRequest {

    @Pattern(regexp = "FEATURED|HOT_BADGE", message = "boostType must be FEATURED or HOT_BADGE")
    private String boostType;
}
