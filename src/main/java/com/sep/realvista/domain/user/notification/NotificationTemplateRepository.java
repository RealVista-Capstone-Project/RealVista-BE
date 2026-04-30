package com.sep.realvista.domain.user.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    Optional<NotificationTemplate> findByTemplateKeyAndLanguage(String templateKey, String language);
    Page<NotificationTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
