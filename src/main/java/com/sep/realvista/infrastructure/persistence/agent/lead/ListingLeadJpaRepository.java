package com.sep.realvista.infrastructure.persistence.agent.lead;

import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingLeadJpaRepository extends JpaRepository<ListingLead, UUID> {

    @Query("SELECT l FROM ListingLead l WHERE l.listingLeadId = :id AND l.agentId = :agentId AND l.deleted = false")
    Optional<ListingLead> findByIdAndAgentId(@Param("id") UUID id, @Param("agentId") UUID agentId);

    @Query("""
            SELECT l FROM ListingLead l
            WHERE l.agentId = :agentId
              AND l.buyerId = :buyerId
              AND l.listingId = :listingId
              AND l.deleted = false
            """)
    Optional<ListingLead> findByAgentIdAndBuyerIdAndListingId(@Param("agentId") UUID agentId,
                                                              @Param("buyerId") UUID buyerId,
                                                              @Param("listingId") UUID listingId);

    @Query("SELECT l FROM ListingLead l WHERE l.agentId = :agentId AND l.deleted = false")
    Page<ListingLead> findAllByAgentId(@Param("agentId") UUID agentId, Pageable pageable);

    @Query("SELECT l FROM ListingLead l WHERE l.agentId = :agentId AND l.status = :status AND l.deleted = false")
    Page<ListingLead> findAllByAgentIdAndStatus(@Param("agentId") UUID agentId,
                                                 @Param("status") LeadStatus status,
                                                 Pageable pageable);

    @Query("""
            SELECT l FROM ListingLead l
            WHERE l.agentId = :agentId
              AND l.deleted = false
              AND (:status IS NULL OR l.status = :status)
              AND (:listingId IS NULL OR l.listingId = :listingId)
              AND l.createdAt >= :from
              AND l.createdAt < :toExclusive
              AND (
                :query IS NULL
                OR LOWER(COALESCE(l.fullName, '')) LIKE :query
                OR LOWER(COALESCE(l.email, '')) LIKE :query
                OR LOWER(COALESCE(l.phone, '')) LIKE :query
                OR EXISTS (
                    SELECT n FROM LeadNote n
                    WHERE n.listingLeadId = l.listingLeadId
                      AND n.deleted = false
                      AND LOWER(COALESCE(n.content, '')) LIKE :query
                )
                OR EXISTS (
                    SELECT listing FROM Listing listing
                    WHERE listing.listingId = l.listingId
                      AND listing.deleted = false
                      AND LOWER(COALESCE(listing.name, '')) LIKE :query
                )
              )
            """)
    Page<ListingLead> findAllByAgentIdWithFilters(@Param("agentId") UUID agentId,
                                                     @Param("status") LeadStatus status,
                                                     @Param("from") LocalDateTime from,
                                                     @Param("toExclusive") LocalDateTime toExclusive,
                                                     @Param("listingId") UUID listingId,
                                                     @Param("query") String query,
                                                     Pageable pageable);

    @Query("""
            SELECT COUNT(l) FROM ListingLead l
            WHERE l.agentId = :agentId
              AND l.deleted = false
              AND (:status IS NULL OR l.status = :status)
              AND (:listingId IS NULL OR l.listingId = :listingId)
              AND l.createdAt >= :from
              AND l.createdAt < :toExclusive
              AND (
                :query IS NULL
                OR LOWER(COALESCE(l.fullName, '')) LIKE :query
                OR LOWER(COALESCE(l.email, '')) LIKE :query
                OR LOWER(COALESCE(l.phone, '')) LIKE :query
                OR EXISTS (
                    SELECT n FROM LeadNote n
                    WHERE n.listingLeadId = l.listingLeadId
                      AND n.deleted = false
                      AND LOWER(COALESCE(n.content, '')) LIKE :query
                )
                OR EXISTS (
                    SELECT listing FROM Listing listing
                    WHERE listing.listingId = l.listingId
                      AND listing.deleted = false
                      AND LOWER(COALESCE(listing.name, '')) LIKE :query
                )
              )
            """)
    long countByAgentIdWithFilters(@Param("agentId") UUID agentId,
                                     @Param("status") LeadStatus status,
                                     @Param("from") LocalDateTime from,
                                     @Param("toExclusive") LocalDateTime toExclusive,
                                     @Param("listingId") UUID listingId,
                                     @Param("query") String query);

    @Query("""
            SELECT l.source, COUNT(l) FROM ListingLead l
            WHERE l.agentId = :agentId
              AND l.deleted = false
              AND (:listingId IS NULL OR l.listingId = :listingId)
              AND l.createdAt >= :from
              AND l.createdAt < :toExclusive
              AND (
                :query IS NULL
                OR LOWER(COALESCE(l.fullName, '')) LIKE :query
                OR LOWER(COALESCE(l.email, '')) LIKE :query
                OR LOWER(COALESCE(l.phone, '')) LIKE :query
                OR EXISTS (
                    SELECT n FROM LeadNote n
                    WHERE n.listingLeadId = l.listingLeadId
                      AND n.deleted = false
                      AND LOWER(COALESCE(n.content, '')) LIKE :query
                )
                OR EXISTS (
                    SELECT listing FROM Listing listing
                    WHERE listing.listingId = l.listingId
                      AND listing.deleted = false
                      AND LOWER(COALESCE(listing.name, '')) LIKE :query
                )
              )
            GROUP BY l.source
            """)
    List<Object[]> countBySourceWithFilters(@Param("agentId") UUID agentId,
                                            @Param("from") LocalDateTime from,
                                            @Param("toExclusive") LocalDateTime toExclusive,
                                            @Param("listingId") UUID listingId,
                                            @Param("query") String query);
}
