package com.sep.realvista.application.listing.contract;

import com.sep.realvista.application.service.DocuSignService;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.contract.SignedDocumentStatus;
import com.sep.realvista.infrastructure.external.storage.SpacesStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Background processor that downloads final signed lease PDFs from DocuSign and uploads them to storage.
 * <p>
 * The DocuSign webhook only marks a lease as {@link SignedDocumentStatus#PENDING}; this job does the
 * slower network work out of band so webhook responses stay fast and reliable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseSignedDocumentProcessingJob {

    private static final int BATCH_SIZE = 10;
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final LeaseAgreementRepository leaseAgreementRepository;
    private final DocuSignService docuSignService;
    private final SpacesStorageService spacesStorageService;

    @Scheduled(fixedDelayString = "${realvista.lease.signed-document-processing-delay-ms:30000}")
    @Transactional
    public void processPendingSignedDocuments() {
        List<LeaseAgreement> leases = leaseAgreementRepository.findBySignedDocumentStatusIn(
                List.of(SignedDocumentStatus.PENDING, SignedDocumentStatus.FAILED), BATCH_SIZE);

        if (leases.isEmpty()) {
            return;
        }

        log.info("Processing {} signed lease document(s)", leases.size());
        for (LeaseAgreement lease : leases) {
            processLease(lease);
        }
    }

    private void processLease(LeaseAgreement lease) {
        if (lease.getDocusignEnvelopeId() == null || lease.getDocusignEnvelopeId().isBlank()) {
            lease.failSignedDocumentProcessing("Missing DocuSign envelope ID");
            leaseAgreementRepository.save(lease);
            return;
        }

        try {
            lease.markSignedDocumentProcessing();
            leaseAgreementRepository.save(lease);

            byte[] pdfBytes = docuSignService.downloadCompletedDocument(lease.getDocusignEnvelopeId());
            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new IllegalStateException("DocuSign returned an empty completed document");
            }

            String folder = "leases/" + lease.getLeaseAgreementId();
            String fileName = "signed-contract.pdf";
            String documentUrl = spacesStorageService.uploadBytes(pdfBytes, folder, fileName, PDF_CONTENT_TYPE);

            lease.completeSignedDocument(documentUrl);
            leaseAgreementRepository.save(lease);
            log.info("Signed lease document uploaded for lease {}: {}",
                    lease.getLeaseAgreementId(), documentUrl);
        } catch (Exception e) {
            String message = truncate(e.getMessage(), 1000);
            lease.failSignedDocumentProcessing(message);
            leaseAgreementRepository.save(lease);
            log.error("Failed to process signed lease document for lease {}: {}",
                    lease.getLeaseAgreementId(), e.getMessage(), e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
