package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeaturePackageJpaRepository extends JpaRepository<FeaturePackage, UUID> {
    List<FeaturePackage> findByIsActiveTrueAndDeletedFalse();
    List<FeaturePackage> findByDeletedFalse();
    List<FeaturePackage> findByFeatureTypeAndIsActiveTrueAndDeletedFalse(FeatureType featureType);
    Optional<FeaturePackage> findByCodeAndDeletedFalse(String code);

    @Query("SELECT COUNT(ufs) FROM UserFeatureSubscription ufs "
            + "WHERE ufs.featurePackageId = :id AND ufs.status = 'ACTIVE' AND ufs.deleted = false")
    long countActiveSubscriptionsByPackageId(@Param("id") UUID id);
}
