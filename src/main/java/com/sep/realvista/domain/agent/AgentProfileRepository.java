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
}
