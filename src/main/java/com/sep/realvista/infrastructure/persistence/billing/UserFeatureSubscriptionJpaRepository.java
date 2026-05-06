package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserFeatureSubscriptionJpaRepository extends JpaRepository<UserFeatureSubscription, UUID> {
    
    List<UserFeatureSubscription> findByUserIdAndDeletedFalse(UUID userId);
    
    List<UserFeatureSubscription> findByUserIdAndStatusAndDeletedFalse(
            UUID userId, UserFeatureSubscriptionStatus status);
    
    @Query("""
        SELECT ufs FROM UserFeatureSubscription ufs
        JOIN FETCH ufs.featurePackage fp
        WHERE ufs.userId = :userId
        AND ufs.status = 'ACTIVE'
        AND ufs.deleted = false
        AND fp.featureType = :featureType
        AND (ufs.endDate IS NULL OR ufs.endDate >= CURRENT_DATE)
        ORDER BY fp.price DESC
        """)
    List<UserFeatureSubscription> findActiveByUserIdAndFeatureType(
            @Param("userId") UUID userId,
            @Param("featureType") FeatureType featureType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT ufs FROM UserFeatureSubscription ufs
        JOIN FETCH ufs.featurePackage fp
        WHERE ufs.userId = :userId
        AND ufs.status = 'ACTIVE'
        AND ufs.deleted = false
        AND fp.featureType = :featureType
        AND (ufs.endDate IS NULL OR ufs.endDate >= CURRENT_DATE)
        ORDER BY fp.price DESC
        """)
    List<UserFeatureSubscription> findActiveByUserIdAndFeatureTypeForUpdate(
            @Param("userId") UUID userId,
            @Param("featureType") FeatureType featureType);

    @Query("""
        SELECT ufs FROM UserFeatureSubscription ufs
        JOIN FETCH ufs.featurePackage fp
        WHERE ufs.userId = :userId
        AND ufs.status = 'ACTIVE'
        AND ufs.deleted = false
        AND (ufs.endDate IS NULL OR ufs.endDate >= CURRENT_DATE)
        """)
    List<UserFeatureSubscription> findAllActiveByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT ufs FROM UserFeatureSubscription ufs
        JOIN FETCH ufs.featurePackage
        WHERE ufs.status = :status
        AND ufs.deleted = false
        """)
    List<UserFeatureSubscription> findByStatusAndNotDeleted(
            @Param("status") UserFeatureSubscriptionStatus status);

    @Query("SELECT COUNT(ufs) FROM UserFeatureSubscription ufs "
            + "WHERE ufs.featurePackageId = :id AND ufs.status = 'ACTIVE' AND ufs.deleted = false")
    long countActiveByFeaturePackageId(@Param("id") UUID id);

    @Query("SELECT COALESCE(SUM(fp.price), 0) FROM UserFeatureSubscription ufs "
            + "JOIN ufs.featurePackage fp WHERE ufs.deleted = false")
    double sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(fp.price), 0) FROM UserFeatureSubscription ufs JOIN ufs.featurePackage fp "
            + "WHERE ufs.deleted = false AND ufs.createdAt BETWEEN :start AND :end")
    double sumTotalRevenueBetween(@Param("start") java.time.LocalDateTime start, 
                                @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(fp.price), 0) FROM UserFeatureSubscription ufs JOIN ufs.featurePackage fp "
            + "WHERE ufs.deleted = false AND ufs.featurePackageId = :id AND ufs.createdAt BETWEEN :start AND :end")
    double sumRevenueByPackageAndPeriod(@Param("id") UUID id, 
                                      @Param("start") java.time.LocalDateTime start, 
                                      @Param("end") java.time.LocalDateTime end);
}
