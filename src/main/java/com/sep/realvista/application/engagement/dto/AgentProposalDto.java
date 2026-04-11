package com.sep.realvista.application.engagement.dto;

import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentProposalDto {
    private UUID agentProposalId;
    private UUID userId;
    private String title;
    
    private BigDecimal commissionRate;
    private Integer experienceYears;
    private AgentProposalStatus status;
    private String pitchContent;
    /**
     * Stored property type id (legacy clients).
     */
    private UUID specialty;
    /**
     * Resolved {@link com.sep.realvista.domain.property.PropertyType#getCode()} for {@link #specialty}.
     * Populated for list responses ({@code getMyProposals}); may be null if type is missing or has no code.
     */
    private String specialtyCode;
    private Map<String, Object> priceRange;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
