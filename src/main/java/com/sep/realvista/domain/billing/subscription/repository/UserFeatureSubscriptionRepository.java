package com.sep.realvista.domain.billing.subscription.repository;

import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscriptionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFeatureSubscriptionRepository {
    UserFeatureSubscription save(UserFeatureSubscription subscription);

    Optional<UserFeatureSubscription> findById(UUID id);

    List<UserFeatureSubscription> findByUserId(UUID userId);

    List<UserFeatureSubscription> findByUserIdAndStatus(UUID userId, UserFeatureSubscriptionStatus status);

    List<UserFeatureSubscription> findActiveByUserIdAndFeatureType(UUID userId, FeatureType featureType);

    List<UserFeatureSubscription> findAllActiveByUserId(UUID userId);

    List<UserFeatureSubscription> findByStatus(UserFeatureSubscriptionStatus status);
}
