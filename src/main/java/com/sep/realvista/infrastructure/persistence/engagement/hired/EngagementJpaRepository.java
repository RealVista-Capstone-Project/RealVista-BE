package com.sep.realvista.infrastructure.persistence.engagement.hired;

import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Engagement entity.
 * <p>
 * Provides data access methods for hired agent engagement queries.
 */
public interface EngagementJpaRepository extends JpaRepository<Engagement, UUID> {
    Optional<Engagement>
    findTopByInitiatorIdAndReceiverIdAndPropertyIdAndEngagementTypeAndDeletedFalseOrderByUpdatedAtDesc(
            UUID initiatorId,
            UUID receiverId,
            UUID propertyId,
            com.sep.realvista.domain.engagement.EngagementType engagementType
    );

  /**
   * Finds hired agent engagements filtered by a specific status,
   * with optional search on agent name.
   */
  @Query(value = """
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
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND e.initiatorId = :ownerId)
      )
      AND (:search IS NULL OR :search = '' OR
          (e.engagementType
           = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
           AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
               COALESCE(i.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
          OR
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
               COALESCE(r.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
      )
      """, countQuery = """
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
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND e.initiatorId = :ownerId)
      )
      AND (:search IS NULL OR :search = '' OR
          (e.engagementType
           = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
           AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
               COALESCE(i.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
          OR
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
               COALESCE(r.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
      )
      """)
  Page<Engagement> findHiredAgentEngagements(
      @Param("ownerId") UUID ownerId,
      @Param("status") EngagementStatus status,
      @Param("search") String search,
      Pageable pageable);

  /**
   * Finds all hired agent engagements with statuses indicating active/completed
   * relationships
   * (ACCEPTED, FINISHED, CANCELLED), with optional search on agent name.
   */
  @Query(value = """
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
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND e.initiatorId = :ownerId)
      )
      AND (:search IS NULL OR :search = '' OR
          (e.engagementType
           = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
           AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
               COALESCE(i.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
          OR
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
               COALESCE(r.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
      )
      """, countQuery = """
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
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND e.initiatorId = :ownerId)
      )
      AND (:search IS NULL OR :search = '' OR
          (e.engagementType
           = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
           AND LOWER(CONCAT(COALESCE(i.firstName, ''), ' ',
               COALESCE(i.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
          OR
          (e.engagementType IN (
               com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
               com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
           )
           AND LOWER(CONCAT(COALESCE(r.firstName, ''), ' ',
               COALESCE(r.lastName, '')))
               LIKE LOWER(CONCAT('%', :search, '%')))
      )
      """)
  Page<Engagement> findAllHiredAgentEngagements(
      @Param("ownerId") UUID ownerId,
      @Param("statuses") List<EngagementStatus> statuses,
      @Param("search") String search,
      Pageable pageable);

    /**
     * Finds property IDs of properties where the agent has submitted a proposal that is not REJECTED or CANCELLED.
     */
    @Query("""
            SELECT e.propertyId FROM Engagement e
            WHERE e.initiatorId = :agentId
            AND e.engagementType = com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL
            AND e.status NOT IN (
                com.sep.realvista.domain.engagement.EngagementStatus.REJECTED,
                com.sep.realvista.domain.engagement.EngagementStatus.CANCELLED
            )
            AND e.deleted = false
            AND e.propertyId IS NOT NULL
            """)
    List<UUID> findAgentActiveProposalPropertyIds(@Param("agentId") UUID agentId);

    /**
     * Finds all engagements where the user is a participant (initiator or receiver),
     * with all associations eagerly fetched.
     */
    @Query("""
            SELECT e FROM Engagement e
            LEFT JOIN FETCH e.initiator i
            LEFT JOIN FETCH e.receiver r
            LEFT JOIN FETCH e.property p
            LEFT JOIN FETCH p.location
            LEFT JOIN FETCH p.propertyType
            WHERE (e.initiatorId = :userId OR e.receiverId = :userId)
            AND e.deleted = false
            AND (:search IS NULL OR :search = '' OR
                LOWER(CONCAT(COALESCE(i.firstName, ''), ' ', COALESCE(i.lastName, '')))
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CONCAT(COALESCE(r.firstName, ''), ' ', COALESCE(r.lastName, '')))
                    LIKE LOWER(CONCAT('%', :search, '%'))
            )
            ORDER BY e.updatedAt DESC
            """)
    List<Engagement> findByParticipantWithFetches(
            @Param("userId") UUID userId,
            @Param("search") String search);

    /**
     * Finds all engagements initiated by the given user.
     */
    List<Engagement> findByInitiatorIdAndDeletedFalse(UUID initiatorId);

  /**
   * Finds a single engagement by ID, eagerly fetching all associations needed
   * to build the detail response in a single query.
   */
  @Query("""
      SELECT e FROM Engagement e
      LEFT JOIN FETCH e.initiator
      LEFT JOIN FETCH e.receiver
      LEFT JOIN FETCH e.property p
      LEFT JOIN FETCH p.location
      LEFT JOIN FETCH p.propertyType
      WHERE e.engagementId = :id
      AND e.deleted = false
      """)
  Optional<Engagement> findByIdWithFetches(@Param("id") UUID id);
    /**
     * Finds all active engagements (not REJECTED/CANCELLED) for a specific property —
     * used to build an agent-engagement-status map on the delegate page.
     */
    @Query("""
            SELECT e FROM Engagement e
            WHERE e.propertyId = :propertyId
            AND e.deleted = false
            AND e.engagementType IN (
                com.sep.realvista.domain.engagement.EngagementType.AGENT_PROPOSAL,
                com.sep.realvista.domain.engagement.EngagementType.OWNER_INVITATION,
                com.sep.realvista.domain.engagement.EngagementType.AGENT_CREATED_PROPERTY_LINK
            )
            AND e.status NOT IN (
                com.sep.realvista.domain.engagement.EngagementStatus.REJECTED,
                com.sep.realvista.domain.engagement.EngagementStatus.CANCELLED
            )
            ORDER BY e.updatedAt DESC
            """)
    List<Engagement> findActiveEngagementsForProperty(@Param("propertyId") UUID propertyId);

    List<Engagement> findByListingIdInOrPropertyIdInAndDeletedFalse(List<UUID> listingIds, List<UUID> propertyIds);
}
