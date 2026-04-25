package com.sep.realvista.infrastructure.persistence.agent.lead;

import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ListingLeadJpaRepository extends JpaRepository<ListingLead, UUID> {

    @Query("SELECT l FROM ListingLead l WHERE l.listingLeadId = :id AND l.agentId = :agentId AND l.deleted = false")
    Optional<ListingLead> findByIdAndAgentId(@Param("id") UUID id, @Param("agentId") UUID agentId);

    @Query("SELECT l FROM ListingLead l WHERE l.agentId = :agentId AND l.deleted = false")
    Page<ListingLead> findAllByAgentId(@Param("agentId") UUID agentId, Pageable pageable);

    @Query("SELECT l FROM ListingLead l WHERE l.agentId = :agentId AND l.status = :status AND l.deleted = false")
    Page<ListingLead> findAllByAgentIdAndStatus(@Param("agentId") UUID agentId,
                                                @Param("status") LeadStatus status,
                                                Pageable pageable);
}
