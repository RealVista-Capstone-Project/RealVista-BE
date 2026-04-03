package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAttributeRequest {
    @JsonProperty("attribute_id")
    private UUID attributeId;

    @JsonProperty("attribute_code")
    private String attributeCode;
    
    @JsonProperty("value_number")
    private BigDecimal valueNumber;
    
    @JsonProperty("value_text")
    private String valueText;
    
    @JsonProperty("value_boolean")
    private Boolean valueBoolean;
}
