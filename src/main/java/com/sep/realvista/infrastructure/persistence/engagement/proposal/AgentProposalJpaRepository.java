package com.sep.realvista.infrastructure.persistence.engagement.proposal;

import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Spring Data JPA repository for AgentProposal entity.
 */
public interface AgentProposalJpaRepository extends JpaRepository<AgentProposal, UUID> {

    @Query("SELECT ap FROM AgentProposal ap "
           + "WHERE ap.userId = :userId AND ap.status != 'ARCHIVED'")
    Page<AgentProposal> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    boolean existsByUserIdAndTitleAndStatusNotAndDeletedFalse(
            UUID userId,
            String title,
            AgentProposalStatus status);

    boolean existsByUserIdAndTitleAndAgentProposalIdNotAndStatusNotAndDeletedFalse(
            UUID userId,
            String title,
            UUID agentProposalId,
            AgentProposalStatus status);
}
