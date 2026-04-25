package com.sep.realvista.infrastructure.persistence.agent.lead;

import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLead;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListingLeadRepositoryImpl implements ListingLeadRepository {

    private final ListingLeadJpaRepository jpaRepository;

    @Override
    public ListingLead save(ListingLead lead) {
        return jpaRepository.save(lead);
    }

    @Override
    public Optional<ListingLead> findByIdAndAgentId(UUID listingLeadId, UUID agentId) {
        return jpaRepository.findByIdAndAgentId(listingLeadId, agentId);
    }

    @Override
    public Page<ListingLead> findAllByAgentId(UUID agentId, Pageable pageable) {
        return jpaRepository.findAllByAgentId(agentId, pageable);
    }

    @Override
    public Page<ListingLead> findAllByAgentIdAndStatus(UUID agentId, LeadStatus status, Pageable pageable) {
        return jpaRepository.findAllByAgentIdAndStatus(agentId, status, pageable);
    }
}
