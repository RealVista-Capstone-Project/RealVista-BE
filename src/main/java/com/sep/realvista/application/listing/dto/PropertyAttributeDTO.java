package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Property attribute/amenity nested DTO.
 * Represents a single attribute like bedroom count, bathroom count, amenities, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAttributeDTO {
    @JsonProperty("attribute_id")
    private UUID attributeId;
    @JsonProperty("attribute_code")
    private String attributeCode;
    @JsonProperty("attribute_name")
    private String attributeName;
    @JsonProperty("data_type")
    private String dataType;
    private String icon;
    private String unit;

    // Value fields (only one will be populated based on data_type)
    @JsonProperty("value_number")
    private BigDecimal valueNumber;
    @JsonProperty("value_text")
    private String valueText;
    @JsonProperty("value_boolean")
    private Boolean valueBoolean;

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
    @JsonProperty("display_value")
    public String getDisplayValue() {
        if (valueNumber != null) {
            return unit != null ? valueNumber + " " + unit : valueNumber.toString();
        }
        if (valueBoolean != null) {
            return valueBoolean ? "Yes" : "No";
        }
        return valueText;
    }
}
