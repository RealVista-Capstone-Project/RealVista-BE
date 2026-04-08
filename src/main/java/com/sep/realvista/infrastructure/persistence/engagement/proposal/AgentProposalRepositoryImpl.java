package com.sep.realvista.infrastructure.persistence.engagement.proposal;

import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Infrastructure implementation of AgentProposalRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class AgentProposalRepositoryImpl implements AgentProposalRepository {

    private final AgentProposalJpaRepository jpaRepository;

    @Override
    public AgentProposal save(AgentProposal proposal) {
        return jpaRepository.save(proposal);
    }

    @Override
    public Set<UUID> findActiveProposalPropertyIds(UUID agentUserId) {
        return new HashSet<>(
                jpaRepository.findActiveProposalPropertyIds(agentUserId, AgentProposalStatus.ARCHIVED));
    }
}
