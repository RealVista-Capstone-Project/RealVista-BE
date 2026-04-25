package com.sep.realvista.application.crm.dto;

import com.sep.realvista.domain.agent.lead.LeadPriority;
import com.sep.realvista.domain.agent.lead.LeadSource;
import com.sep.realvista.domain.agent.lead.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private UUID listingLeadId;
    private UUID agentId;
    private UUID listingId;
    private UUID buyerId;
    private String fullName;
    private String email;
    private String phone;
    private LeadSource source;
    private LeadStatus status;
    private LeadPriority priority;
    private BigDecimal budget;
    private List<LeadNoteResponse> notes;
    private LocalDateTime lastContactedAt;
    private LocalDateTime nextFollowUpAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
