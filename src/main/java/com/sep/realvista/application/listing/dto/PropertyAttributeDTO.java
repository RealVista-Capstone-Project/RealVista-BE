package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Property attribute/amenity nested DTO.
 * Represents a single attribute like bedroom count, bathroom count, amenities,
 * etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PropertyAttributeDTO {
    private UUID attributeId;
    private String attributeCode;
    private String attributeName;
    private String dataType;
    private String icon;
    private String unit;

    private Integer priority;

    // Value fields (only one will be populated based on data_type)
    private BigDecimal valueNumber;
    private String valueText;
    private Boolean valueBoolean;

    private List<PropertyAttributeRangeDTO> ranges;

    // Helper methods for common attributes
    public boolean isNumber() {
        return valueNumber != null;
    }

    public boolean isText() {
        return valueText != null;
    }

    public boolean isBoolean() {
        return valueBoolean != null;
    }

    /**
     * Formats the value for display in UI.
     * For example: "3" for bedrooms, "2" for bathrooms, "Yes" for boolean amenities
     */
    public String getDisplayValue() {
        if (valueNumber != null) {
            return unit != null ? valueNumber.toBigInteger() + " " + unit : valueNumber.toString();
        }
        if (valueBoolean != null) {
            return valueBoolean ? "Có" : "Không";
        }
        return valueText;
    }
}
