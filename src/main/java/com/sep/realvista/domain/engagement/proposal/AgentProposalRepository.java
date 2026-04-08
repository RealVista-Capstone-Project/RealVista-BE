package com.sep.realvista.domain.engagement.proposal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository interface for AgentProposal aggregate.
 *
 * <p>Defines the contract for agent proposal data access operations.
 * Implementation is provided in the infrastructure layer.
 */
public interface AgentProposalRepository {

    /**
     * Saves an agent proposal.
     *
     * @param proposal the proposal to save
     * @return the saved proposal
     */
    AgentProposal save(AgentProposal proposal);

    /**
     * Finds a proposal by its ID.
     */
    Optional<AgentProposal> findById(UUID id);

    /**
     * Finds proposals by user ID with pagination.
     */
    Page<AgentProposal> findByUserId(UUID userId, Pageable pageable);

    /**
     * Checks if a proposal with given title exists for an agent.
     */
    boolean existsByUserIdAndTitle(UUID userId, String title);

    /**
     * Deletes a proposal by ID.
     */
    void deleteById(UUID id);

    /**
     * Returns the set of property IDs for which the given agent has an active (non-ARCHIVED) proposal.
     *
     * <p>Used by the property feed to mark properties where the agent already has a proposal,
     * preventing duplicate submissions.
     *
     * @param agentUserId the agent's user ID
     * @return set of property IDs with active proposals
     */
    Set<UUID> findActiveProposalPropertyIds(UUID agentUserId);
}
