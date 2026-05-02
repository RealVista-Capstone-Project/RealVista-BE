package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.BoostType;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.ListingBoostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingBoostJpaRepository extends JpaRepository<ListingBoost, UUID> {

    List<ListingBoost> findByListingIdAndStatusAndDeletedFalse(UUID listingId, ListingBoostStatus status);

    Optional<ListingBoost> findByListingIdAndBoostTypeAndStatusAndDeletedFalse(
            UUID listingId, BoostType boostType, ListingBoostStatus status);

    List<ListingBoost> findByUserIdAndStatusAndDeletedFalse(UUID userId, ListingBoostStatus status);

    List<ListingBoost> findByListingIdInAndStatusAndDeletedFalse(List<UUID> listingIds, ListingBoostStatus status);

    @Query("SELECT lb FROM ListingBoost lb "
           + "WHERE lb.listingId IN :listingIds "
           + "AND lb.status = :status "
           + "AND lb.startDate <= :now "
           + "AND lb.endDate >= :now "
           + "AND lb.deleted = false")
    List<ListingBoost> findAllActiveByListingIds(
            @Param("listingIds") List<UUID> listingIds,
            @Param("status") ListingBoostStatus status,
            @Param("now") LocalDate now);

    @Query("SELECT COUNT(lb) FROM ListingBoost lb "
            + "WHERE lb.boostPackageId = :id AND lb.status = 'ACTIVE' AND lb.deleted = false")
    long countActiveByBoostPackageId(@Param("id") UUID id);

    @Query("SELECT COALESCE(SUM(bp.price), 0) FROM ListingBoost lb "
            + "JOIN lb.boostPackage bp WHERE lb.deleted = false")
    double sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(bp.price), 0) FROM ListingBoost lb JOIN lb.boostPackage bp "
            + "WHERE lb.deleted = false AND lb.createdAt BETWEEN :start AND :end")
    double sumTotalRevenueBetween(@Param("start") java.time.LocalDateTime start, 
                                @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(bp.price), 0) FROM ListingBoost lb JOIN lb.boostPackage bp "
            + "WHERE lb.deleted = false AND lb.boostPackageId = :id AND lb.createdAt BETWEEN :start AND :end")
    double sumRevenueByPackageAndPeriod(@Param("id") UUID id, 
                                      @Param("start") java.time.LocalDateTime start, 
                                      @Param("end") java.time.LocalDateTime end);
}
