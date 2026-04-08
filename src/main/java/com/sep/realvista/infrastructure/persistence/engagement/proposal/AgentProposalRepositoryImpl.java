package com.sep.realvista.infrastructure.persistence.engagement.proposal;

import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.infrastructure.persistence.engagement.hired.EngagementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Infrastructure implementation of AgentProposalRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class AgentProposalRepositoryImpl implements AgentProposalRepository {

    private final AgentProposalJpaRepository jpaRepository;
    private final EngagementJpaRepository engagementJpaRepository;

    @Override
    public AgentProposal save(AgentProposal agentProposal) {
        return jpaRepository.save(agentProposal);
    }

    @Override
    public Optional<AgentProposal> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<AgentProposal> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public boolean existsByUserIdAndTitle(UUID userId, String title) {
        return jpaRepository.existsByUserIdAndTitle(userId, title);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Set<UUID> findActiveProposalPropertyIds(UUID agentUserId) {
        return new HashSet<>(engagementJpaRepository.findAgentActiveProposalPropertyIds(agentUserId));
    }
}
