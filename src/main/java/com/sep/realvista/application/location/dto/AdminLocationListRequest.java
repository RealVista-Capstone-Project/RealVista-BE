package com.sep.realvista.application.location.dto;

import com.sep.realvista.domain.property.location.LocationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLocationListRequest {
    private String search;
    private LocationType level;
    private UUID parentId;
}
