package com.sep.realvista.application.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAttributeDTO {
    private UUID propertyAttributeId;
    private String name;
    private String code;
    private String dataType; // NUMBER, BOOLEAN, TEXT
    private Boolean isSearchable;
    private String icon;
    private String unit;
    private Boolean isRequired;
}
