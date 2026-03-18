package com.sep.realvista.infrastructure.persistence.engagement.hired;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Spring Data JPA repository for Engagement entity.
 *
 * Provides data access methods for hired agent engagement queries.
 */
public interface EngagementJpaRepository extends JpaRepository<Engagement, UUID> {

    /**
     * Finds all accepted engagements where the given owner is involved.
     * For AGENT_PROPOSAL: owner is the receiver (agent initiated).
     * For OWNER_INVITATION: owner is the initiator (owner invited agent).
     *
     * Eagerly fetches initiator, receiver, and property to avoid N+1.
     *
     * @param ownerId the owner's user ID
     * @param status  the engagement status to filter by
     * @param pageable pagination and sort
     * @return page of engagements
     */
    @Query(
        value = """
                SELECT e FROM Engagement e
                LEFT JOIN FETCH e.initiator
                LEFT JOIN FETCH e.receiver
                LEFT JOIN FETCH e.property p
                LEFT JOIN FETCH p.location
                LEFT JOIN FETCH p.propertyType
                WHERE e.status = :status
                AND e.deleted = false
                AND (
                    (e.engagementType = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND e.receiverId = :ownerId)
                    OR
                    (e.engagementType = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND e.initiatorId = :ownerId)
                )
                """,
        countQuery = """
                SELECT COUNT(e) FROM Engagement e
                WHERE e.status = :status
                AND e.deleted = false
                AND (
                    (e.engagementType = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND e.receiverId = :ownerId)
                    OR
                    (e.engagementType = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND e.initiatorId = :ownerId)
                )
                """
    )
    Page<Engagement> findHiredAgentEngagements(
            @Param("ownerId") UUID ownerId,
            @Param("status") EngagementStatus status,
            Pageable pageable
    );
}
