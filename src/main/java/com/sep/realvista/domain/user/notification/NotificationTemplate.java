package com.sep.realvista.domain.user.notification;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "notification_templates", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"template_key", "language"}),
       indexes = {
           @Index(name = "idx_notification_template_key_lang", columnList = "template_key, language"),
           @Index(name = "idx_notification_template_type", columnList = "type")
       })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NotificationTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // 'EMAIL' or 'IN_APP'

    @Column(name = "language", nullable = false, length = 10)
    private String language; // 'en' or 'vi'

    @Column(name = "title")
    private String title;

    @Column(name = "content_body", nullable = false, columnDefinition = "TEXT")
    private String contentBody;

    public void update(String name, String title, String contentBody) {
        this.name = name;
        this.title = title;
        this.contentBody = contentBody;
    }
}
