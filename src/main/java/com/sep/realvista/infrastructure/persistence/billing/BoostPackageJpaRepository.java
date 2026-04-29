package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.BoostPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoostPackageJpaRepository extends JpaRepository<BoostPackage, UUID> {
    List<BoostPackage> findByIsActiveTrue();
    List<BoostPackage> findByDeletedFalse();
    Optional<BoostPackage> findByCode(String code);
    Optional<BoostPackage> findByBoostPackageIdAndDeletedFalse(UUID id);

    @Query("SELECT COUNT(ulbp) FROM UserListingBoostPackage ulbp "
            + "WHERE ulbp.boostPackageId = :id AND ulbp.status = 'ACTIVE' AND ulbp.deleted = false")
    long countActiveUserBoostPackages(@Param("id") UUID id);

    @Query("SELECT COUNT(lb) FROM ListingBoost lb "
            + "WHERE lb.boostPackageId = :id AND lb.status = 'ACTIVE' AND lb.deleted = false")
    long countActiveListingBoosts(@Param("id") UUID id);
}
