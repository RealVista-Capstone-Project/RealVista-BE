package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
        """)
    List<UserFeatureSubscription> findActiveByUserIdAndFeatureType(
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
        WHERE ufs.status = :status
        AND ufs.deleted = false
        """)
    List<UserFeatureSubscription> findByStatusAndNotDeleted(
            @Param("status") UserFeatureSubscriptionStatus status);
}
