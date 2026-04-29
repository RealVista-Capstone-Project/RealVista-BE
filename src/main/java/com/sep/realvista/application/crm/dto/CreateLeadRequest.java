package com.sep.realvista.application.crm.dto;

import com.sep.realvista.domain.agent.lead.LeadSource;
import com.sep.realvista.domain.agent.lead.LeadPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateLeadRequest {

    @NotBlank
    private String fullName;

    private String email;
    private String phone;

    @NotNull
    private LeadSource source;

    private UUID listingId;
    private UUID buyerId;
    private BigDecimal budget;
    private LeadPriority priority;

    /** Optional first note content */
    private String note;
}
