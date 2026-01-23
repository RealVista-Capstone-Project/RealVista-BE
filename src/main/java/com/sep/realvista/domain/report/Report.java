package com.sep.realvista.domain.report;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_reporter", columnList = "reporter_user_id"),
        @Index(name = "idx_report_reported_user", columnList = "reported_user_id"),
        @Index(name = "idx_report_reported_listing", columnList = "reported_listing_id"),
        @Index(name = "idx_report_target_type", columnList = "report_target_type"),
        @Index(name = "idx_report_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private UUID reportId;

    @Column(name = "reporter_user_id", nullable = false)
    private UUID reporterUserId;

    @Column(name = "reported_user_id")
    private UUID reportedUserId;

    @Column(name = "reported_listing_id")
    private UUID reportedListingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_target_type", nullable = false, length = 20)
    private ReportTargetType reportTargetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_reason", nullable = false, length = 20)
    private ReportReason reportReason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_media_url", columnDefinition = "TEXT")
    private String evidenceMediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    public void startReview() {
        this.status = ReportStatus.REVIEWING;
    }

    public void resolve(String adminNote) {
        this.status = ReportStatus.RESOLVED;
        this.adminNote = adminNote;
    }

    public void dismiss(String adminNote) {
        this.status = ReportStatus.DISMISSED;
        this.adminNote = adminNote;
    }
}
