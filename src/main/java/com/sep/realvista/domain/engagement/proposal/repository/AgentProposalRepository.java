package com.sep.realvista.domain.engagement.proposal.repository;

import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AgentProposalRepository {
    AgentProposal save(AgentProposal agentProposal);
    Optional<AgentProposal> findById(UUID id);
    Page<AgentProposal> findByUserId(UUID userId, Pageable pageable);
    boolean existsByUserIdAndTitle(UUID userId, String title);
    void deleteById(UUID id);
}
