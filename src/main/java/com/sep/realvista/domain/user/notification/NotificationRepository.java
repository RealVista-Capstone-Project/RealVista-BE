package com.sep.realvista.domain.user.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> saveAll(List<Notification> notifications);

    Optional<Notification> findById(UUID id);

    Page<Notification> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalseAndDeletedFalse(UUID userId);

    long countByUserIdAndIsReadFalseAndDeletedFalse(UUID userId);

    void markAllAsReadByUserId(UUID userId);
}
