package com.sep.realvista.domain.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    Page<Report> findByReportTargetType(ReportTargetType targetType, Pageable pageable);

    Page<Report> findByStatusAndReportTargetType(
            ReportStatus status, ReportTargetType targetType, Pageable pageable);

    long countByStatus(ReportStatus status);

    List<Report> findTop10ByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query(value = "SELECT AVG(EXTRACT(EPOCH FROM "
            + "(updated_at - created_at))/3600) FROM reports "
            + "WHERE status IN ('RESOLVED', 'DISMISSED')", nativeQuery = true)
    Double findAverageResolutionTimeInHours();
}
