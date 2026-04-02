package com.sep.realvista.infrastructure.persistence.notification;

import com.sep.realvista.domain.user.notification.DeviceToken;
import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DeviceTokenRepositoryImpl implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository jpaRepository;

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        return jpaRepository.save(deviceToken);
    }

    @Override
    public Optional<DeviceToken> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<DeviceToken> findByUserIdAndActiveTrue(UUID userId) {
        return jpaRepository.findByUserIdAndActiveTrueAndDeletedFalse(userId);
    }

    @Override
    public Optional<DeviceToken> findByUserIdAndFcmToken(UUID userId, String fcmToken) {
        return jpaRepository.findByUserIdAndFcmToken(userId, fcmToken);
    }

    @Override
    public void deleteByUserIdAndFcmToken(UUID userId, String fcmToken) {
        jpaRepository.deleteByUserIdAndFcmToken(userId, fcmToken);
    }

    @Override
    public void deactivateAllByUserId(UUID userId) {
        jpaRepository.deactivateAllByUserId(userId);
    }
}
