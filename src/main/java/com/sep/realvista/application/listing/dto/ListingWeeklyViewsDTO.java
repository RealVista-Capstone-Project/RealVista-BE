package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Seven consecutive days (Mon–Sun) of view counts starting at {@link #weekStart}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Listing views aggregated by day for one week")
public class ListingWeeklyViewsDTO {

    @Schema(description = "Monday of the week (inclusive)", example = "2026-05-04")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    @Schema(description = "Seven daily buckets in order Mon → Sun")
    private List<ListingDailyViewsDayDTO> days;
}
