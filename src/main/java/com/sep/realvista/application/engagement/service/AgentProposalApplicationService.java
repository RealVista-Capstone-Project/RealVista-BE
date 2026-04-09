package com.sep.realvista.application.engagement.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.AgentProposalDto;
import com.sep.realvista.application.engagement.dto.ApplyAgentProposalRequest;
import com.sep.realvista.application.engagement.mapper.AgentProposalMapper;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentProposalApplicationService {

    private final AgentProposalRepository agentProposalRepository;
    private final AgentProposalMapper agentProposalMapper;

    @Transactional
    public AgentProposalDto createProposal(UUID userId, ApplyAgentProposalRequest request) {
        log.info("Agent {} is creating a new proposal template: {}", userId, request.getTitle());

        // Check if title already exists for this agent
        if (agentProposalRepository.existsByUserIdAndTitle(userId, request.getTitle())) {
            throw new BusinessConflictException(
                    "You already have a proposal with this title", 
                    "DUPLICATE_PROPOSAL_TITLE");
        }

        AgentProposal proposal = AgentProposal.builder()
                .userId(userId)
                .title(request.getTitle())
                .commissionRate(request.getCommissionRate())
                .experienceYears(request.getExperienceYears())
                .pitchContent(request.getPitchContent())
                .specialty(request.getSpecialty())
                .priceRange(request.getPriceRange())
                .status(AgentProposalStatus.ACTIVE)
                .build();

        AgentProposal savedProposal = agentProposalRepository.save(proposal);
        return agentProposalMapper.toDto(savedProposal);
    }

    @Transactional
    public AgentProposalDto updateProposal(UUID proposalId, UUID userId, ApplyAgentProposalRequest request) {
        log.info("Agent {} is updating proposal template: {}", userId, proposalId);

        AgentProposal proposal = agentProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentProposal", proposalId));

        if (!proposal.getUserId().equals(userId)) {
            throw new SecurityException("You do not have permission to modify this proposal");
        }

        proposal.update(
                request.getTitle(),
                request.getCommissionRate(),
                request.getExperienceYears(),
                request.getPitchContent(),
                request.getSpecialty(),
                request.getPriceRange()
        );

        AgentProposal updatedProposal = agentProposalRepository.save(proposal);
        return agentProposalMapper.toDto(updatedProposal);
    }

    @Transactional(readOnly = true)
    public PageResponse<AgentProposalDto> getMyProposals(UUID userId, int page, int size) {
        log.info("Getting proposal templates for agent {}", userId);
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<AgentProposal> proposals = agentProposalRepository.findByUserId(userId, pageable);

        List<AgentProposalDto> content = proposals.getContent().stream()
                .map(agentProposalMapper::toDto)
                .collect(Collectors.toList());

        return PageResponse.<AgentProposalDto>builder()
                .content(content)
                .page(proposals.getNumber())
                .size(proposals.getSize())
                .totalElements(proposals.getTotalElements())
                .totalPages(proposals.getTotalPages())
                .last(proposals.isLast())
                .first(proposals.isFirst())
                .build();
    }

    @Transactional
    public void archiveProposal(UUID proposalId, UUID userId) {
        log.info("Agent {} is archiving proposal template {}", userId, proposalId);
        
        AgentProposal proposal = agentProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentProposal", proposalId));

        if (!proposal.getUserId().equals(userId)) {
            throw new SecurityException("You do not have permission to modify this proposal");
        }

        proposal.archive();
        agentProposalRepository.save(proposal);
    }
}
