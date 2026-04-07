package com.sep.realvista.domain.engagement.proposal;

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
