package com.sep.realvista.domain.engagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Engagement aggregate.
 *
 * Defines contract for engagement data access operations.
 * Implementation will be provided in infrastructure layer.
 */
public interface EngagementRepository {

    /**
     * Saves an engagement.
     *
     * @param engagement the engagement to save
     * @return the saved engagement
     */
    Engagement save(Engagement engagement);

    /**
     * Finds an engagement by ID.
     *
     * @param id the engagement ID
     * @return optional engagement if found
     */
    Optional<Engagement> findById(UUID id);

    /**
     * Finds hired agent engagements filtered by a specific status.
     *
     * @param ownerId the owner's user ID
     * @param status the engagement status to filter by
     * @param search optional search query for agent name
     * @param pageable pagination parameters
     * @return page of engagements matching the criteria
     */
    Page<Engagement> findHiredAgentEngagements(UUID ownerId, EngagementStatus status, String search, Pageable pageable);

    /**
     * Finds all hired agent engagements (ACCEPTED, FINISHED, CANCELLED).
     * Used when no status filter is specified.
     *
     * @param ownerId the owner's user ID
     * @param search optional search query for agent name
     * @param pageable pagination parameters
     * @return page of engagements
     */
    Page<Engagement> findAllHiredAgentEngagements(UUID ownerId, String search, Pageable pageable);
}
