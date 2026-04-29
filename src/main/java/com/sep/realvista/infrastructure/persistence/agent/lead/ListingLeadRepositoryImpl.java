package com.sep.realvista.infrastructure.persistence.agent.lead;

import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLead;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
    public Optional<ListingLead> findByAgentIdAndBuyerIdAndListingId(UUID agentId, UUID buyerId, UUID listingId) {
        return jpaRepository.findByAgentIdAndBuyerIdAndListingId(agentId, buyerId, listingId);
    }

    @Override
    public Page<ListingLead> findAllByAgentId(UUID agentId, Pageable pageable) {
        return jpaRepository.findAllByAgentId(agentId, pageable);
    }

    @Override
    public Page<ListingLead> findAllByAgentIdAndStatus(UUID agentId, LeadStatus status, Pageable pageable) {
        return jpaRepository.findAllByAgentIdAndStatus(agentId, status, pageable);
    }

    @Override
    public Page<ListingLead> findAllByAgentIdWithFilters(UUID agentId, LeadStatus status, LocalDateTime from,
                                                          LocalDateTime toExclusive, String query,
                                                          Pageable pageable) {
        return jpaRepository.findAllByAgentIdWithFilters(agentId, status, from, toExclusive, query, pageable);
    }

    @Override
    public long countByAgentIdWithFilters(UUID agentId, LeadStatus status, LocalDateTime from,
                                           LocalDateTime toExclusive, String query) {
        return jpaRepository.countByAgentIdWithFilters(agentId, status, from, toExclusive, query);
    }

    @Override
    public List<Object[]> countBySourceWithFilters(UUID agentId, LocalDateTime from, LocalDateTime toExclusive,
                                                   String query) {
        return jpaRepository.countBySourceWithFilters(agentId, from, toExclusive, query);
    }
}
