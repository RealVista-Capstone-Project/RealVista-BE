package com.sep.realvista.application.property.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PropertySummaryMetricsResponse {
    long totalProperties;
    long currentMonthTotalProperties;
    long previousTotalProperties;
    long availableProperties;
    long reservedProperties;
    long soldProperties;
    long rentedProperties;
    long draftProperties;
    long pendingProperties;
    long verifiedProperties;
    long rejectedProperties;
}
