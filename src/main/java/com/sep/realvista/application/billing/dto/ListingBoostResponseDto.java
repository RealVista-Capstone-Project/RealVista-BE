package com.sep.realvista.application.billing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ListingBoostResponseDto {
    private UUID listingBoostId;
    private UUID listingId;
    private String boostType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
