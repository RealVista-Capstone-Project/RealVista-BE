package com.sep.realvista.domain.agent.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ListingLeadRepository {

    ListingLead save(ListingLead lead);

    Optional<ListingLead> findByIdAndAgentId(UUID listingLeadId, UUID agentId);

    Page<ListingLead> findAllByAgentId(UUID agentId, Pageable pageable);

    Page<ListingLead> findAllByAgentIdAndStatus(UUID agentId, LeadStatus status, Pageable pageable);
}
