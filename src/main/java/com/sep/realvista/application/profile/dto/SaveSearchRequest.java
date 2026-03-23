package com.sep.realvista.application.profile.dto;

import com.sep.realvista.domain.profile.SearchType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveSearchRequest {
    @NotNull(message = "Search type cannot be null")
    private SearchType searchType;

    @NotNull(message = "Criteria cannot be null")
    private Map<String, Object> criteria;
}
