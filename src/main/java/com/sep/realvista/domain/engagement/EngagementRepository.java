package com.sep.realvista.domain.engagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Engagement aggregate.
 * Defines contract for engagement data access operations.
 * Implementation will be provided in infrastructure layer.
 */
public interface EngagementRepository {

    Engagement save(Engagement engagement);

    List<Engagement> saveAll(List<Engagement> engagements);

    /**
     * Finds an engagement by ID.
     *
     * @param id the engagement ID
     * @return optional engagement if found
     */
    Optional<Engagement> findById(UUID id);

    /**
     * Finds an engagement by ID with all associated entities eagerly fetched
     * (initiator, receiver, property, property type, location).
     *
     * <p>Prefer this over {@link #findById} when the caller needs to access
     * agent user details or property information, to avoid N+1 queries.
     *
     * @param id the engagement ID
     * @return optional engagement with all associations loaded
     */
    Optional<Engagement> findByIdWithFetches(UUID id);

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

    /**
     * Finds all engagements where the user is a participant (initiator or receiver).
     *
     * @param userId the user ID
     * @param search optional search query
     * @return list of engagements
     */
    List<Engagement> findByParticipantWithFetches(UUID userId, String search);

    /**
     * Finds engagements where the user is the initiator.
     *
     * @param userId the user ID
     * @return list of engagements
     */
    List<Engagement> findByInitiatorId(UUID userId);

    /**
     * Finds the most recently updated AGENT_PROPOSAL engagement for a property
     * between an initiator (agent) and receiver (owner).
     *
     * @param initiatorId initiator user ID
     * @param receiverId receiver user ID
     * @param propertyId property ID
     * @return latest engagement if exists
     */
    Optional<Engagement> findLatestAgentProposalEngagement(
            UUID initiatorId, UUID receiverId, UUID propertyId);

    /**
     * Finds all active (not REJECTED/CANCELLED) engagements for a property.
     *
     * @param propertyId the property ID
     * @return list of active engagements
     */
    List<Engagement> findActiveEngagementsForProperty(UUID propertyId);

    /**
     * Finds engagements associated with any of the listing IDs or property IDs.
     *
     * @param listingIds list of listing IDs
     * @param propertyIds list of property IDs
     * @return list of engagements
     */
    List<Engagement> findByListingIdInOrPropertyIdIn(List<UUID> listingIds, List<UUID> propertyIds);
}
