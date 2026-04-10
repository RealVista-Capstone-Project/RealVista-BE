package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscriptionStatus;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserFeatureSubscriptionRepositoryImpl implements UserFeatureSubscriptionRepository {

    private final UserFeatureSubscriptionJpaRepository jpa;

    @Override
    public UserFeatureSubscription save(UserFeatureSubscription subscription) {
        return jpa.save(subscription);
    }

    @Override
    public Optional<UserFeatureSubscription> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<UserFeatureSubscription> findByUserId(UUID userId) {
        return jpa.findByUserIdAndDeletedFalse(userId);
    }

    @Override
    public List<UserFeatureSubscription> findByUserIdAndStatus(UUID userId, UserFeatureSubscriptionStatus status) {
        return jpa.findByUserIdAndStatusAndDeletedFalse(userId, status);
    }

    @Override
    public List<UserFeatureSubscription> findActiveByUserIdAndFeatureType(UUID userId, FeatureType featureType) {
        return jpa.findActiveByUserIdAndFeatureType(userId, featureType);
    }

    @Override
    public List<UserFeatureSubscription> findAllActiveByUserId(UUID userId) {
        return jpa.findAllActiveByUserId(userId);
    }

    @Override
    public List<UserFeatureSubscription> findByStatus(UserFeatureSubscriptionStatus status) {
        return jpa.findByStatusAndNotDeleted(status);
    }
}
