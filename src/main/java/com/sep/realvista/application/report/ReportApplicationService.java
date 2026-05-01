package com.sep.realvista.application.report;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.report.dto.ReportDto;
import com.sep.realvista.application.report.mapper.ReportMapper;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.report.Report;
import com.sep.realvista.domain.report.ReportRepository;
import com.sep.realvista.domain.report.ReportStatus;
import com.sep.realvista.domain.report.ReportTargetType;
import com.sep.realvista.domain.user.UserRepository;
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
    private final ReportMapper reportMapper;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingApplicationService listingApplicationService;

    @Transactional(readOnly = true)
    public PageResponse<ReportDto> getPagedReports(
            com.sep.realvista.domain.report.ReportStatus status, Pageable pageable) {
        Page<Report> page = (status == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findByStatus(status, pageable);
        return PageResponse.of(page.map(reportMapper::toDto));
    }

    @Transactional(readOnly = true)
    public ReportDto getReportById(UUID reportId) {
        return reportRepository.findById(reportId)
                .map(reportMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
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

        // Perform Ban Action based on target type
        if (report.getReportTargetType() == null) {
            return;
        }

        if (report.getReportTargetType() == com.sep.realvista.domain.report.ReportTargetType.LISTING) {
            UUID listingId = report.getReportedListingId();
            if (listingId != null) {
                listingApplicationService.banListing(listingId);
            }
        } else if (report.getReportTargetType() == com.sep.realvista.domain.report.ReportTargetType.USER) {
            UUID userId = report.getReportedUserId();
            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    user.ban();
                    userRepository.save(user);
                    
                    // Also ban all listings of the banned user
                    listingRepository.findByUserId(userId).forEach(listing -> {
                        listingApplicationService.banListing(listing.getListingId());
                    });
                });
            }
        }
    }

    @Transactional
    public void dismissReport(UUID reportId, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.dismiss(adminNote);
        reportRepository.save(report);
    }
}
