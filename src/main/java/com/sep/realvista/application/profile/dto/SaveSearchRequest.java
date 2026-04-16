package com.sep.realvista.application.profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.profile.SearchType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for saving search criteria")
public class SaveSearchRequest {
    @NotNull(message = "Search type cannot be null")
    @Schema(description = "Type of search (RENT, SALE, etc.)", example = "RENT")
    private SearchType searchType;

    @NotNull(message = "Criteria cannot be null")
    @Schema(description = "Map of search filter criteria")
    private Map<String, Object> criteria;

    @Schema(description = "Optional board ID to save the search into")
    private String boardId;
    
    @Schema(description = "Optional profile ID to associate with the saved search")
    private UUID profileId;

    @JsonProperty("is_recommendation")
    @Schema(description = "Whether this saved search should be used for AI recommendations", defaultValue = "false")
    private boolean isRecommendation;
}
