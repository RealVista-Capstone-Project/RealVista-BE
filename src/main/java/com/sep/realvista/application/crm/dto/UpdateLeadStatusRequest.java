package com.sep.realvista.application.crm.dto;

import com.sep.realvista.domain.agent.lead.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLeadStatusRequest {

    @NotNull
    private LeadStatus status;
}
