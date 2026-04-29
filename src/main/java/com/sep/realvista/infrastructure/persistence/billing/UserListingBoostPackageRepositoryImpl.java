package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.UserListingBoostPackage;
import com.sep.realvista.domain.billing.boost.UserListingBoostPackageStatus;
import com.sep.realvista.domain.billing.boost.repository.UserListingBoostPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserListingBoostPackageRepositoryImpl implements UserListingBoostPackageRepository {

    private final UserListingBoostPackageJpaRepository jpa;

    @Override
    public UserListingBoostPackage save(UserListingBoostPackage boostPackage) {
        return jpa.save(boostPackage);
    }

    @Override
    public Optional<UserListingBoostPackage> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<UserListingBoostPackage> findAllActiveByUserId(UUID userId) {
        return jpa.findAllActiveByUserId(userId);
    }

    @Override
    public List<UserListingBoostPackage> findByUserIdAndStatus(UUID userId, UserListingBoostPackageStatus status) {
        return jpa.findByUserIdAndStatusAndDeletedFalse(userId, status);
    }

    @Override
    public long countActiveByBoostPackageId(UUID boostPackageId) {
        return jpa.countActiveByBoostPackageId(boostPackageId);
    }
}
