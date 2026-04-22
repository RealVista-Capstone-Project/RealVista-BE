package com.sep.realvista.application.profile.dto;

import com.sep.realvista.domain.profile.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearchDto {
    private UUID savedSearchId;
    private UUID profileId;
    private SearchType searchType;
    private Map<String, Object> criteria;
    private String boardId;
    private LocalDateTime createdAt;
    @com.fasterxml.jackson.annotation.JsonProperty("is_recommendation")
    private boolean isRecommendation;
}
