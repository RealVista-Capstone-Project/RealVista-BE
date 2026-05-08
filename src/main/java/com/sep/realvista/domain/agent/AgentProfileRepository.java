package com.sep.realvista.domain.agent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for AgentProfile aggregate.
 *
 * Defines contract for agent profile data access operations.
 * Implementation will be provided in infrastructure layer.
 */
public interface AgentProfileRepository {

    /**
     * Finds an agent profile by user ID.
     *
     * @param userId the user ID
     * @return optional agent profile if found
     */
    Optional<AgentProfile> findByUserId(UUID userId);

    /**
     * Finds agent profiles by a list of user IDs.
     *
     * @param userIds the list of user IDs
     * @return list of agent profiles
     */
    List<AgentProfile> findByUserIds(List<UUID> userIds);

    /**
     * Finds all active (non-deleted) agent profiles ordered by rating descending.
     *
     * @return list of all active agent profiles
     */
    List<AgentProfile> findAllActive();

    /**
     * Non-deleted agent profiles whose user is not deleted and has ACTIVE or VERIFIED status.
     *
     * @return agents browseable for owner flows (e.g. hire agent), excludes banned/suspended users
     */
    List<AgentProfile> findAllActiveWithEligibleUserAccount();

    /**
     * Saves an agent profile.
     *
     * @param agentProfile the agent profile to save
     * @return the saved agent profile
     */
    AgentProfile save(AgentProfile agentProfile);
}
