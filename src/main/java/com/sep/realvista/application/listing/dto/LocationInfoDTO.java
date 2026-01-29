package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Location information nested DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationInfoDTO {
    @JsonProperty("location_id")
    private UUID locationId;
    @JsonProperty("city_name")
    private String cityName;
    @JsonProperty("district_name")
    private String districtName;
    @JsonProperty("ward_name")
    private String wardName;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
