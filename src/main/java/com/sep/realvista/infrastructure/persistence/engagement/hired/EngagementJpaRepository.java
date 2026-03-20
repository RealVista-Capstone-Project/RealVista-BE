package com.sep.realvista.infrastructure.persistence.engagement.hired;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Engagement entity.
 *
 * Provides data access methods for hired agent engagement queries.
 */
public interface EngagementJpaRepository extends JpaRepository<Engagement, UUID> {

    /**
     * Finds hired agent engagements filtered by a specific status,
     * with optional search on agent name.
     */
    @Query(
        value = """
                SELECT e FROM Engagement e
                LEFT JOIN FETCH e.initiator i
                LEFT JOIN FETCH e.receiver r
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
                AND (:search IS NULL OR :search = '' OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
                         COALESCE(i.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                    OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
                         COALESCE(r.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                )
                """,
        countQuery = """
                SELECT COUNT(e) FROM Engagement e
                LEFT JOIN e.initiator i
                LEFT JOIN e.receiver r
                WHERE e.status = :status
                AND e.deleted = false
                AND (
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND e.receiverId = :ownerId)
                    OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND e.initiatorId = :ownerId)
                )
                AND (:search IS NULL OR :search = '' OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
                         COALESCE(i.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                    OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
                         COALESCE(r.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                )
                """
    )
    Page<Engagement> findHiredAgentEngagements(
            @Param("ownerId") UUID ownerId,
            @Param("status") EngagementStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Finds all hired agent engagements with statuses indicating active/completed relationships
     * (ACCEPTED, FINISHED, CANCELLED), with optional search on agent name.
     */
    @Query(
        value = """
                SELECT e FROM Engagement e
                LEFT JOIN FETCH e.initiator i
                LEFT JOIN FETCH e.receiver r
                LEFT JOIN FETCH e.property p
                LEFT JOIN FETCH p.location
                LEFT JOIN FETCH p.propertyType
                WHERE e.status IN :statuses
                AND e.deleted = false
                AND (
                    (e.engagementType = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND e.receiverId = :ownerId)
                    OR
                    (e.engagementType = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND e.initiatorId = :ownerId)
                )
                AND (:search IS NULL OR :search = '' OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
                         COALESCE(i.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                    OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
                         COALESCE(r.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                )
                """,
        countQuery = """
                SELECT COUNT(e) FROM Engagement e
                LEFT JOIN e.initiator i
                LEFT JOIN e.receiver r
                WHERE e.status IN :statuses
                AND e.deleted = false
                AND (
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND e.receiverId = :ownerId)
                    OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND e.initiatorId = :ownerId)
                )
                AND (:search IS NULL OR :search = '' OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
                     AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
                         COALESCE(i.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                    OR
                    (e.engagementType
                     = com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION
                     AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
                         COALESCE(r.lastName, '')))
                         LIKE LOWER(CONCAT('%', :search, '%')))
                )
                """
    )
    Page<Engagement> findAllHiredAgentEngagements(
            @Param("ownerId") UUID ownerId,
            @Param("statuses") List<EngagementStatus> statuses,
            @Param("search") String search,
            Pageable pageable
    );
}
