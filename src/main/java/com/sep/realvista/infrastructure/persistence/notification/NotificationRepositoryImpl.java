package com.sep.realvista.infrastructure.persistence.notification;

import com.sep.realvista.domain.user.notification.Notification;
import com.sep.realvista.domain.user.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        return jpaRepository.saveAll(notifications);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<Notification> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    @Override
    public List<Notification> findByUserIdAndIsReadFalseAndDeletedFalse(UUID userId) {
        return jpaRepository.findByUserIdAndIsReadFalseAndDeletedFalse(userId);
    }

    @Override
    public long countByUserIdAndIsReadFalseAndDeletedFalse(UUID userId) {
        return jpaRepository.countByUserIdAndIsReadFalseAndDeletedFalse(userId);
    }

    @Override
    public void markAllAsReadByUserId(UUID userId) {
        jpaRepository.markAllAsReadByUserId(userId);
    }
}
