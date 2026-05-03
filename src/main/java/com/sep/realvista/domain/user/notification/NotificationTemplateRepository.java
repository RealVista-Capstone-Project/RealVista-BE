package com.sep.realvista.domain.user.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    Optional<NotificationTemplate> findByTemplateKeyAndLanguage(String templateKey, String language);
    
    @Query("SELECT n FROM NotificationTemplate n WHERE "
           + "(LOWER(n.name) LIKE LOWER(CONCAT('%', :search, '%')) "
           + "OR LOWER(n.templateKey) LIKE LOWER(CONCAT('%', :search, '%'))) "
           + "AND (:type IS NULL OR n.type = :type)")
    Page<NotificationTemplate> searchTemplates(
            @Param("search") String search, 
            @Param("type") String type, 
            Pageable pageable);
}
