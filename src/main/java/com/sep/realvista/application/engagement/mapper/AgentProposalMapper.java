package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.engagement.dto.AgentProposalDto;
import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import org.springframework.stereotype.Component;

@Component
public class AgentProposalMapper {

    public AgentProposalDto toDto(AgentProposal proposal) {
        if (proposal == null) {
            return null;
        }

        return AgentProposalDto.builder()
                .agentProposalId(proposal.getAgentProposalId())
                .userId(proposal.getUserId())
                .title(proposal.getTitle())
                .commissionRate(proposal.getCommissionRate())
                .experienceYears(proposal.getExperienceYears())
                .status(proposal.getStatus())
                .pitchContent(proposal.getPitchContent())
                .createdAt(proposal.getCreatedAt())
                .updatedAt(proposal.getUpdatedAt())
                .build();
    }
}
