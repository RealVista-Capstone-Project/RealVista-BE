package com.sep.realvista.application.crm.dto;

import com.sep.realvista.domain.agent.lead.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadNoteResponse {

    private UUID leadNoteId;
    private UUID listingLeadId;
    private UUID agentId;
    private String content;
    private LeadStatus statusAtTime;
    private LocalDateTime createdAt;
}
