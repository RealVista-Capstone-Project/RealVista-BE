package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDTO {
    @JsonProperty("location_id")
    private UUID locationId;
    private String code;
    private String name;
    @JsonProperty("parent_id")
    private UUID parentId;
    private String level;
    private String status;
    @JsonProperty("sort_order")
    private Integer sortOrder;
    @JsonProperty("used_by_properties_count")
    private Long usedByPropertiesCount;
    @JsonProperty("north_lat")
    private BigDecimal northLat;
    @JsonProperty("south_lat")
    private BigDecimal southLat;
    @JsonProperty("east_lng")
    private BigDecimal eastLng;
    @JsonProperty("west_lng")
    private BigDecimal westLng;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
