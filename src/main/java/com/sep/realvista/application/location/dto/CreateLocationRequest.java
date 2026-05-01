package com.sep.realvista.application.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.property.location.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLocationRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    @NotNull(message = "Level is required")
    private LocationType level;

    @JsonProperty("parent_id")
    private UUID parentId;

    @JsonProperty("sort_order")
    private Integer sortOrder;
}
