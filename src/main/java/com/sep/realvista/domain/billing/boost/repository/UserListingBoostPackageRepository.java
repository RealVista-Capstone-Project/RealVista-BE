package com.sep.realvista.domain.billing.boost.repository;

import com.sep.realvista.domain.billing.boost.UserListingBoostPackage;
import com.sep.realvista.domain.billing.boost.UserListingBoostPackageStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserListingBoostPackageRepository {
    UserListingBoostPackage save(UserListingBoostPackage boostPackage);

    Optional<UserListingBoostPackage> findById(UUID id);

    List<UserListingBoostPackage> findAllActiveByUserId(UUID userId);

    List<UserListingBoostPackage> findByUserIdAndStatus(UUID userId, UserListingBoostPackageStatus status);

    long countActiveByBoostPackageId(UUID boostPackageId);
}
