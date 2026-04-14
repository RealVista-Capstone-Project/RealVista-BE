package com.sep.realvista.infrastructure.persistence.notification;

import com.sep.realvista.domain.user.notification.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceToken, UUID> {

    List<DeviceToken> findByUserIdAndActiveTrueAndDeletedFalse(UUID userId);

    Optional<DeviceToken> findByUserIdAndFcmToken(UUID userId, String fcmToken);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.active = false WHERE d.userId = :userId")
    void deactivateAllByUserId(@Param("userId") UUID userId);

    void deleteByUserIdAndFcmToken(UUID userId, String fcmToken);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.active = false WHERE d.fcmToken IN :tokens")
    void deactivateByFcmTokenIn(@Param("tokens") List<String> tokens);
}
