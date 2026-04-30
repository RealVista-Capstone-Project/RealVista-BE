package com.sep.realvista.application.report;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.domain.report.Report;
import com.sep.realvista.domain.report.ReportRepository;
import com.sep.realvista.domain.report.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportApplicationService {

    private final ReportRepository reportRepository;
    private final com.sep.realvista.application.report.mapper.ReportMapper reportMapper;

    @Transactional(readOnly = true)
    public PageResponse<com.sep.realvista.application.report.dto.ReportDto> getPagedReports(
            ReportStatus status, Pageable pageable) {
        Page<Report> page = (status == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findByStatus(status, pageable);
        return PageResponse.of(page.map(reportMapper::toDto));
    }


    @Transactional
    public void startReview(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.startReview();
        reportRepository.save(report);
    }

    @Transactional
    public void resolveReport(UUID reportId, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.resolve(adminNote);
        reportRepository.save(report);
    }

    @Transactional
    public void dismissReport(UUID reportId, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.dismiss(adminNote);
        reportRepository.save(report);
    }
}
