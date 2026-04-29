package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.UserListingBoostPackage;
import com.sep.realvista.domain.billing.boost.UserListingBoostPackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserListingBoostPackageJpaRepository extends JpaRepository<UserListingBoostPackage, UUID> {

    List<UserListingBoostPackage> findByUserIdAndDeletedFalse(UUID userId);

    List<UserListingBoostPackage> findByUserIdAndStatusAndDeletedFalse(
            UUID userId, UserListingBoostPackageStatus status);

    @Query("""
        SELECT ulbp FROM UserListingBoostPackage ulbp
        JOIN FETCH ulbp.boostPackage bp
        WHERE ulbp.userId = :userId
        AND ulbp.status = 'ACTIVE'
        AND ulbp.deleted = false
        AND (ulbp.endDate IS NULL OR ulbp.endDate >= CURRENT_DATE)
        """)
    List<UserListingBoostPackage> findAllActiveByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT ulbp FROM UserListingBoostPackage ulbp
        WHERE ulbp.status = :status
        AND ulbp.deleted = false
        """)
    List<UserListingBoostPackage> findByStatusAndNotDeleted(
            @Param("status") UserListingBoostPackageStatus status);

    @Query("SELECT COUNT(ulbp) FROM UserListingBoostPackage ulbp "
            + "WHERE ulbp.boostPackageId = :id AND ulbp.status = 'ACTIVE' AND ulbp.deleted = false")
    long countActiveByBoostPackageId(@Param("id") UUID id);
}
