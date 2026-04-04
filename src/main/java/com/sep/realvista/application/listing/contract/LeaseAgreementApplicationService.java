package com.sep.realvista.application.listing.contract;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.contract.dto.CreateLeaseRequest;
import com.sep.realvista.application.listing.contract.dto.LeaseResponse;
import com.sep.realvista.application.listing.contract.dto.LeaseTemplateData;
import com.sep.realvista.application.listing.contract.dto.SigningUrlResponse;
import com.sep.realvista.application.listing.contract.mapper.LeaseAgreementMapper;
import com.sep.realvista.application.service.DocuSignService;
import com.sep.realvista.infrastructure.config.DocuSignConfig;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Application service for lease agreement management and DocuSign eSignature workflow.
 * <p>
 * Orchestrates lease CRUD operations and coordinates with DocuSign for embedded signing.
 * Signing operations degrade gracefully when DocuSign is not configured.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeaseAgreementApplicationService {

    private final LeaseAgreementRepository leaseAgreementRepository;
    private final UserRepository userRepository;
    private final DocuSignService docuSignService;
    private final DocuSignConfig docuSignConfig;
    private final LeaseAgreementMapper leaseAgreementMapper;
    private final RestTemplate restTemplate;

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /**
     * Creates a new lease agreement in DRAFT status.
     */
    public LeaseResponse createLease(CreateLeaseRequest request) {
        LeaseAgreement lease = LeaseAgreement.builder()
                .listingId(request.getListingId())
                .renterId(request.getRenterId())
                .landlordId(request.getLandlordId())
                .agentId(request.getAgentId())
                .leaseStartDate(request.getLeaseStartDate())
                .leaseEndDate(request.getLeaseEndDate())
                .leaseDurationMonths(request.getLeaseDurationMonths())
                .monthlyRent(request.getMonthlyRent())
                .securityDeposit(request.getSecurityDeposit())
                .leaseDocumentUrl(request.getLeaseDocumentUrl())
                .build();

        LeaseAgreement saved = leaseAgreementRepository.save(lease);
        log.info("Lease agreement created: {}", saved.getLeaseAgreementId());
        return leaseAgreementMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LeaseResponse getLeaseById(UUID leaseId) {
        LeaseAgreement lease = findLeaseOrThrow(leaseId);
        return leaseAgreementMapper.toResponse(lease);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaseResponse> getLeasesByRenter(UUID renterId, int page, int size) {
        Page<LeaseAgreement> leases = leaseAgreementRepository.findByRenterId(
                renterId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return toPageResponse(leases);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaseResponse> getLeasesByLandlord(UUID landlordId, int page, int size) {
        Page<LeaseAgreement> leases = leaseAgreementRepository.findByLandlordId(
                landlordId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return toPageResponse(leases);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaseResponse> getLeasesByListing(UUID listingId, int page, int size) {
        Page<LeaseAgreement> leases = leaseAgreementRepository.findByListingId(
                listingId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return toPageResponse(leases);
    }

    // ── DocuSign Signing Workflow ─────────────────────────────────────────────

    /**
     * Sends the lease to the renter for embedded signing after the landlord has signed.
     * This is the second step in the signing workflow (PENDING_LANDLORD → PENDING_RENTER).
     * For template flow, the renter is already a recipient in the existing envelope —
     * no new envelope is created. For PDF flow, adds the renter as a new signer.
     *
     * @param leaseId    UUID of the lease agreement
     * @param returnUrl  Frontend URL to redirect to after signing (null = use default from config)
     * @return {@link SigningUrlResponse} with the embedded signing URL
     */
    public SigningUrlResponse sendToRenterForSigning(UUID leaseId, String returnUrl) {
        LeaseAgreement lease = findLeaseOrThrow(leaseId);

        if (lease.getStatus() != LeaseStatus.PENDING_LANDLORD) {
            throw new BusinessConflictException(
                    "Lease must be in PENDING_LANDLORD status to send renter for signing. "
                            + "Current status: " + lease.getStatus()
            );
        }

        User renter = findUserOrThrow(lease.getRenterId());

        if (!docuSignService.isAvailable()) {
            // Graceful degradation: update status without DocuSign
            log.warn("DocuSign not available — marking lease {} as PENDING_RENTER without envelope", leaseId);
            lease.submitToRenter();
            leaseAgreementRepository.save(lease);
            return SigningUrlResponse.builder()
                    .envelopeId(null)
                    .signingUrl(null)
                    .signerRole("renter")
                    .build();
        }

        String envelopeId = lease.getDocusignEnvelopeId();

        if (!docuSignConfig.isTemplateAvailable()) {
            // PDF flow: add renter as a new recipient to the existing envelope
            byte[] documentBytes = downloadDocument(lease.getLeaseDocumentUrl());

            envelopeId = docuSignService.createEnvelopeForSigning(
                    documentBytes,
                    "Lease Agreement",
                    renter.getEmail().getValue(),
                    renter.getFirstName() + " " + renter.getLastName(),
                    renter.getUserId().toString()
            );

            // Update envelope ID if a new one was created
            if (!envelopeId.equals(lease.getDocusignEnvelopeId())) {
                lease.assignDocuSignEnvelope(envelopeId);
            }
        }
        // Template flow: renter is already a recipient (routing order 2), just generate URL

        // Update status to PENDING_RENTER
        lease.submitToRenter();
        leaseAgreementRepository.save(lease);

        // Generate embedded signing URL
        String effectiveReturnUrl = returnUrl != null ? returnUrl : buildDefaultReturnUrl(leaseId, "renter");
        String signingUrl = docuSignService.getEmbeddedSigningUrl(
                envelopeId,
                renter.getEmail().getValue(),
                renter.getFirstName() + " " + renter.getLastName(),
                renter.getUserId().toString(),
                effectiveReturnUrl
        );

        log.info("Renter signing URL generated for lease {} (envelope {})", leaseId, envelopeId);
        return SigningUrlResponse.builder()
                .signingUrl(signingUrl)
                .envelopeId(envelopeId)
                .signerRole("renter")
                .build();
    }

    /**
     * Re-generates the embedded signing URL for the renter (URL expires after ~5 minutes).
     */
    public SigningUrlResponse getRenterSigningUrl(UUID leaseId, String returnUrl) {
        LeaseAgreement lease = findLeaseOrThrow(leaseId);

        if (lease.getStatus() != LeaseStatus.PENDING_RENTER) {
            throw new BusinessConflictException(
                    "Lease must be in PENDING_RENTER status. Current status: " + lease.getStatus()
            );
        }
        if (lease.getDocusignEnvelopeId() == null || !docuSignService.isAvailable()) {
            throw new BusinessConflictException("DocuSign is not available or envelope was not created.");
        }

        User renter = findUserOrThrow(lease.getRenterId());
        String effectiveReturnUrl = returnUrl != null ? returnUrl : buildDefaultReturnUrl(leaseId, "renter");
        String signingUrl = docuSignService.getEmbeddedSigningUrl(
                lease.getDocusignEnvelopeId(),
                renter.getEmail().getValue(),
                renter.getFirstName() + " " + renter.getLastName(),
                renter.getUserId().toString(),
                effectiveReturnUrl
        );

        return SigningUrlResponse.builder()
                .signingUrl(signingUrl)
                .envelopeId(lease.getDocusignEnvelopeId())
                .signerRole("renter")
                .build();
    }

    /**
     * Sends the lease to the landlord for embedded signing first.
     * This is the first step in the signing workflow (DRAFT → PENDING_LANDLORD).
     * Uses template-based flow if a lease template is configured, otherwise
     * downloads the lease PDF from the stored URL and creates a DocuSign envelope.
     *
     * @param leaseId    UUID of the lease agreement
     * @param returnUrl  Frontend URL to redirect to after signing (null = use default from config)
     * @return {@link SigningUrlResponse} with the embedded signing URL
     */
    public SigningUrlResponse sendToLandlordForSigning(UUID leaseId, String returnUrl) {
        LeaseAgreement lease = findLeaseOrThrow(leaseId);

        if (lease.getStatus() != LeaseStatus.DRAFT) {
            throw new BusinessConflictException(
                    "Lease must be in DRAFT status to send for signing. Current status: " + lease.getStatus()
            );
        }

        User landlord = findUserOrThrow(lease.getLandlordId());

        if (!docuSignService.isAvailable()) {
            log.warn("DocuSign not available — marking lease {} as PENDING_LANDLORD without envelope", leaseId);
            lease.submitToLandlord();
            leaseAgreementRepository.save(lease);
            return SigningUrlResponse.builder()
                    .envelopeId(null)
                    .signingUrl(null)
                    .signerRole("landlord")
                    .build();
        }

        String envelopeId;

        if (docuSignConfig.isTemplateAvailable()) {
            // Template-based flow: populate dynamic fields from lease + user data
            User renter = findUserOrThrow(lease.getRenterId());

            LeaseTemplateData templateData = LeaseTemplateData.builder()
                    .renterName(renter.getFirstName() + " " + renter.getLastName())
                    .renterEmail(renter.getEmail().getValue())
                    .renterClientUserId(renter.getUserId().toString())
                    .landlordName(landlord.getFirstName() + " " + landlord.getLastName())
                    .landlordEmail(landlord.getEmail().getValue())
                    .landlordClientUserId(landlord.getUserId().toString())
                    .leaseStartDate(lease.getLeaseStartDate() != null ? lease.getLeaseStartDate().toString() : "")
                    .leaseEndDate(lease.getLeaseEndDate() != null ? lease.getLeaseEndDate().toString() : "")
                    .leaseDurationMonths(String.valueOf(lease.getLeaseDurationMonths()))
                    .monthlyRent(lease.getMonthlyRent() != null ? lease.getMonthlyRent().toPlainString() : "")
                    .securityDeposit(lease.getSecurityDeposit() != null
                            ? lease.getSecurityDeposit().toPlainString() : "")
                    .build();

            envelopeId = docuSignService.createEnvelopeFromTemplate(
                    docuSignConfig.getLeaseTemplateId(), templateData
            );

            log.info("Template envelope created for lease {} (envelope {})", leaseId, envelopeId);
        } else {
            // PDF-upload flow: download document and create envelope
            if (lease.getLeaseDocumentUrl() == null || lease.getLeaseDocumentUrl().isBlank()) {
                throw new BusinessConflictException(
                        "Lease document URL is required before sending for signing. Upload the lease PDF first."
                );
            }

            byte[] documentBytes = downloadDocument(lease.getLeaseDocumentUrl());
            envelopeId = docuSignService.createEnvelopeForSigning(
                    documentBytes,
                    "Lease Agreement",
                    landlord.getEmail().getValue(),
                    landlord.getFirstName() + " " + landlord.getLastName(),
                    landlord.getUserId().toString()
            );
        }

        // Persist envelope ID and update status
        lease.assignDocuSignEnvelope(envelopeId);
        lease.submitToLandlord();
        leaseAgreementRepository.save(lease);

        // Generate embedded signing URL for landlord
        String effectiveReturnUrl = returnUrl != null ? returnUrl : buildDefaultReturnUrl(leaseId, "landlord");
        String signingUrl = docuSignService.getEmbeddedSigningUrl(
                envelopeId,
                landlord.getEmail().getValue(),
                landlord.getFirstName() + " " + landlord.getLastName(),
                landlord.getUserId().toString(),
                effectiveReturnUrl
        );

        log.info("Landlord signing URL generated for lease {} (envelope {})", leaseId, envelopeId);
        return SigningUrlResponse.builder()
                .signingUrl(signingUrl)
                .envelopeId(envelopeId)
                .signerRole("landlord")
                .build();
    }

    /**
     * Re-generates the embedded signing URL for the landlord.
     */
    public SigningUrlResponse getLandlordSigningUrl(UUID leaseId, String returnUrl) {
        LeaseAgreement lease = findLeaseOrThrow(leaseId);

        if (lease.getStatus() != LeaseStatus.PENDING_LANDLORD) {
            throw new BusinessConflictException(
                    "Lease must be in PENDING_LANDLORD status. Current status: " + lease.getStatus()
            );
        }
        if (lease.getDocusignEnvelopeId() == null || !docuSignService.isAvailable()) {
            throw new BusinessConflictException("DocuSign is not available or envelope was not created.");
        }

        User landlord = findUserOrThrow(lease.getLandlordId());
        String effectiveReturnUrl = returnUrl != null ? returnUrl : buildDefaultReturnUrl(leaseId, "landlord");
        String signingUrl = docuSignService.getEmbeddedSigningUrl(
                lease.getDocusignEnvelopeId(),
                landlord.getEmail().getValue(),
                landlord.getFirstName() + " " + landlord.getLastName(),
                landlord.getUserId().toString(),
                effectiveReturnUrl
        );

        return SigningUrlResponse.builder()
                .signingUrl(signingUrl)
                .envelopeId(lease.getDocusignEnvelopeId())
                .signerRole("landlord")
                .build();
    }

    // ── Webhook Handler ───────────────────────────────────────────────────────

    /**
     * Handles DocuSign Connect webhook events (envelope status updates).
     * Called by the controller after signature verification.
     *
     * @param envelopeId    DocuSign envelope ID from the webhook payload
     * @param eventStatus   Envelope status from DocuSign (e.g. "completed", "declined")
     */
    public void handleWebhookEvent(String envelopeId, String eventStatus) {
        leaseAgreementRepository.findByDocusignEnvelopeId(envelopeId).ifPresentOrElse(
                lease -> {
                    log.info("DocuSign webhook: envelope {} -> status {} (lease {})",
                            envelopeId, eventStatus, lease.getLeaseAgreementId());
                    processEnvelopeStatusUpdate(lease, eventStatus);
                    leaseAgreementRepository.save(lease);
                },
                () -> log.warn("DocuSign webhook: no lease found for envelope {}", envelopeId)
        );
    }

    private void processEnvelopeStatusUpdate(LeaseAgreement lease, String eventStatus) {
        switch (eventStatus.toLowerCase()) {
            case "completed" -> {
                // Envelope completed means all signers are done — renter is the last signer
                if (lease.getStatus() == LeaseStatus.PENDING_RENTER) {
                    lease.renterSignViaDocuSign();
                }
            }
            case "declined", "voided" -> {
                lease.reject("DocuSign envelope was " + eventStatus);
            }
            default -> lease.updateDocuSignStatus(eventStatus);
        }
    }

    // ── Lease State Transitions ───────────────────────────────────────────────

    public LeaseResponse rejectLease(UUID leaseId, String reason) {
        LeaseAgreement lease = findLeaseOrThrow(leaseId);
        lease.reject(reason);
        return leaseAgreementMapper.toResponse(leaseAgreementRepository.save(lease));
    }

    public LeaseResponse terminateLease(UUID leaseId) {
        LeaseAgreement lease = findLeaseOrThrow(leaseId);
        lease.terminate();
        return leaseAgreementMapper.toResponse(leaseAgreementRepository.save(lease));
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private LeaseAgreement findLeaseOrThrow(UUID leaseId) {
        return leaseAgreementRepository.findById(leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaseAgreement", leaseId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    /**
     * Downloads a document from a URL (e.g. DigitalOcean Spaces CDN) as raw bytes.
     */
    private byte[] downloadDocument(String documentUrl) {
        try {
            log.debug("Downloading lease document from: {}", documentUrl);
            return restTemplate.getForObject(documentUrl, byte[].class);
        } catch (Exception e) {
            throw new BusinessConflictException(
                    "Failed to download lease document from URL: " + documentUrl
            );
        }
    }

    private String buildDefaultReturnUrl(UUID leaseId, String role) {
        return docuSignConfig.getReturnUrl() + "?leaseId=" + leaseId + "&role=" + role;
    }

    private PageResponse<LeaseResponse> toPageResponse(Page<LeaseAgreement> page) {
        return PageResponse.<LeaseResponse>builder()
                .content(page.getContent().stream().map(leaseAgreementMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
