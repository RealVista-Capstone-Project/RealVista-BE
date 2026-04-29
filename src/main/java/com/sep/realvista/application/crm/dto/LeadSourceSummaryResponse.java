package com.sep.realvista.application.crm.dto;

import com.sep.realvista.domain.agent.lead.LeadSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSourceSummaryResponse {

    private LeadSource source;
    private long count;
}
