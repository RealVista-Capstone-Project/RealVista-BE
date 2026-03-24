package com.sep.realvista.infrastructure.persistence.notification;

import com.sep.realvista.domain.user.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalseAndDeletedFalse(UUID userId);

    long countByUserIdAndIsReadFalseAndDeletedFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true "
            + "WHERE n.userId = :userId AND n.isRead = false "
            + "AND n.deleted = false")
    void markAllAsReadByUserId(@Param("userId") UUID userId);
}
