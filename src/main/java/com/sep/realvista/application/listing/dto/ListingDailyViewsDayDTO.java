package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One day in a weekly views breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "View count for a single calendar day")
public class ListingDailyViewsDayDTO {

    @Schema(description = "Calendar date (bucket)", example = "2026-05-05")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(description = "Number of recorded views for that day", example = "12")
    private int views;
}
