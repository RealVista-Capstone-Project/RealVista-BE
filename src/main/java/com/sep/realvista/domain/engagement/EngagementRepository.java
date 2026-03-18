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
     * Finds all accepted engagements where the given owner is involved
     * (as receiver for AGENT_PROPOSAL or as initiator for OWNER_INVITATION).
     * Eagerly fetches initiator, receiver, and property data.
     *
     * @param ownerId the owner's user ID
     * @param pageable pagination parameters
     * @return page of accepted engagements for the owner
     */
    Page<Engagement> findHiredAgentEngagements(UUID ownerId, Pageable pageable);
}
