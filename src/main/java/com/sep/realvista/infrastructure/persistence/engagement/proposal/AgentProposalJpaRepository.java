package com.sep.realvista.infrastructure.persistence.engagement.proposal;

import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for AgentProposal entity.
 */
public interface AgentProposalJpaRepository extends JpaRepository<AgentProposal, UUID> {

    /**
     * Finds all property IDs where the given agent has a proposal that is not ARCHIVED.
     */
    @Query("SELECT ap.propertyId FROM AgentProposal ap "
            + "WHERE ap.userId = :agentUserId AND ap.status <> :archivedStatus AND ap.deleted = false")
    List<UUID> findActiveProposalPropertyIds(
            @Param("agentUserId") UUID agentUserId,
            @Param("archivedStatus") AgentProposalStatus archivedStatus);
}
