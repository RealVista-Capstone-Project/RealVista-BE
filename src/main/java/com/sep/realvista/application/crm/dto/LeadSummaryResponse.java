package com.sep.realvista.application.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSummaryResponse {

    private long totalLeads;
    private long closedLeads;
    private long previousTotalLeads;
    private long previousClosedLeads;
    private List<LeadSourceSummaryResponse> bySource;
}
