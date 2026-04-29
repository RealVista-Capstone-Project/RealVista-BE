package com.sep.realvista.domain.agent.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingLeadRepository {

    ListingLead save(ListingLead lead);

    Optional<ListingLead> findByIdAndAgentId(UUID listingLeadId, UUID agentId);

    Optional<ListingLead> findByAgentIdAndBuyerIdAndListingId(UUID agentId, UUID buyerId, UUID listingId);

    Page<ListingLead> findAllByAgentId(UUID agentId, Pageable pageable);

    Page<ListingLead> findAllByAgentIdAndStatus(UUID agentId, LeadStatus status, Pageable pageable);

    Page<ListingLead> findAllByAgentIdWithFilters(UUID agentId, LeadStatus status, LocalDateTime from,
                                                   LocalDateTime toExclusive, UUID listingId, String query,
                                                   Pageable pageable);

    long countByAgentIdWithFilters(UUID agentId, LeadStatus status, LocalDateTime from,
                                   LocalDateTime toExclusive, UUID listingId, String query);

    List<Object[]> countBySourceWithFilters(UUID agentId, LocalDateTime from, LocalDateTime toExclusive,
                                            UUID listingId, String query);
}
