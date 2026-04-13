package com.sep.realvista.application.profile.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.profile.dto.SavedSearchDto;
import com.sep.realvista.domain.profile.SavedSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SavedSearchMapper {

    private final ObjectMapper objectMapper;

    public SavedSearchDto toDto(SavedSearch entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> parsedCriteria = null;
        try {
            if (entity.getCriteria() != null && !entity.getCriteria().isBlank()) {
                parsedCriteria = objectMapper.readValue(
                        entity.getCriteria(),
                        new TypeReference<Map<String, Object>>() { }
                );
            }
        } catch (JsonProcessingException e) {
            // Can be ignored or handled globally
        }

        return SavedSearchDto.builder()
                .savedSearchId(entity.getSavedSearchId())
                .searchType(entity.getSearchType())
                .criteria(parsedCriteria)
                .boardId(entity.getBoardId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
