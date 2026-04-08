package com.sep.realvista.application.listing.contract;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.contract.dto.CreateLeaseRequest;
import com.sep.realvista.application.listing.contract.dto.LeaseResponse;
import com.sep.realvista.application.listing.contract.dto.LeaseTemplateData;
import com.sep.realvista.application.listing.contract.dto.SigningUrlResponse;
import com.sep.realvista.application.listing.contract.dto.TerminateLeaseRequest;
import com.sep.realvista.application.listing.contract.mapper.LeaseAgreementMapper;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.DocuSignService;
import com.sep.realvista.infrastructure.config.DocuSignConfig;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.application.common.util.VietnameseCurrencyUtil;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Application service for lease agreement management and DocuSign eSignature
 * workflow.
 * <p>
 * Orchestrates lease CRUD operations and coordinates with DocuSign for embedded
 * signing.
 * Signing operations degrade gracefully when DocuSign is not configured.
 * <p>
 * Leases are now tied directly to a {@link Property} (not a listing).
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeaseAgreementApplicationService {

  private final LeaseAgreementRepository leaseAgreementRepository;
  private final PropertyRepository propertyRepository;
  private final ListingRepository listingRepository;
  private final UserRepository userRepository;
  private final DocuSignService docuSignService;
  private final DocuSignConfig docuSignConfig;
  private final LeaseAgreementMapper leaseAgreementMapper;
  private final RestTemplate restTemplate;
  private final NotificationApplicationService notificationService;

  // ── CRUD Operations ───────────────────────────────────────────────────────

  /**
   * Creates a new lease agreement in DRAFT status.
   * <p>
   * Validates:
   * <ul>
   * <li>Property exists</li>
   * <li>Property is in AVAILABLE status</li>
   * <li>No active lease already exists for the property</li>
   * </ul>
   */
  public LeaseResponse createLease(CreateLeaseRequest request) {
    // 1. Validate property exists
    Property property = propertyRepository.findById(request.getPropertyId())
        .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

    // 2. Validate property is available for leasing
    if (!property.isAvailable()) {
      throw new BusinessConflictException(
          "Property is not available for leasing. Current status: " + property.getStatus());
    }

    // 3. Validate no active lease already exists for this property
    List<LeaseAgreement> activeLeases = leaseAgreementRepository
        .findActiveLeasesByPropertyId(request.getPropertyId());
    if (!activeLeases.isEmpty()) {
      throw new BusinessConflictException(
          "An active lease already exists for this property.");
    }

    // 4. Build and save
    LeaseAgreement lease = LeaseAgreement.builder()
        .propertyId(request.getPropertyId())
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
    log.info("Lease agreement created: {} for property: {}",
        saved.getLeaseAgreementId(), saved.getPropertyId());
    return toEnrichedResponse(saved);
  }

  @Transactional(readOnly = true)
  public LeaseResponse getLeaseById(UUID leaseId) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);
    return toEnrichedResponse(lease);
  }

  @Transactional(readOnly = true)
  public PageResponse<LeaseResponse> getLeasesByRenter(UUID renterId, LeaseStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<LeaseAgreement> leases = (status != null)
        ? leaseAgreementRepository.findByRenterIdAndStatus(renterId, status, pageable)
        : leaseAgreementRepository.findByRenterId(renterId, pageable);
    return toPageResponse(leases);
  }

  @Transactional(readOnly = true)
  public PageResponse<LeaseResponse> getLeasesByLandlord(UUID landlordId, LeaseStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<LeaseAgreement> leases = (status != null)
        ? leaseAgreementRepository.findByLandlordIdAndStatus(landlordId, status, pageable)
        : leaseAgreementRepository.findByLandlordId(landlordId, pageable);
    return toPageResponse(leases);
  }

  /**
   * Lists leases by property ID.
   */
  @Transactional(readOnly = true)
  public PageResponse<LeaseResponse> getLeasesByProperty(UUID propertyId, int page, int size) {
    Page<LeaseAgreement> leases = leaseAgreementRepository.findByPropertyId(
        propertyId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    return toPageResponse(leases);
  }

  /**
   * Lists leases by listing ID (backward-compatible endpoint).
   * Resolves the listing to its property, then delegates to
   * {@link #getLeasesByProperty}.
   */
  @Transactional(readOnly = true)
  public PageResponse<LeaseResponse> getLeasesByListing(UUID listingId, int page, int size) {
    var listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));
    return getLeasesByProperty(listing.getPropertyId(), page, size);
  }

  // ── DocuSign Signing Workflow ─────────────────────────────────────────────

  /**
   * Sends the lease to the renter for embedded signing after the landlord has
   * signed.
   * This is the second step in the signing workflow (PENDING_LANDLORD →
   * PENDING_RENTER).
   * For template flow, the renter is already a recipient in the existing envelope
   * —
   * no new envelope is created. For PDF flow, adds the renter as a new signer.
   *
   * @param leaseId UUID of the lease agreement
   * @param locale  Locale segment for the frontend return URL (e.g. "vi", "en")
   * @return {@link SigningUrlResponse} with the embedded signing URL
   */
  public SigningUrlResponse sendToRenterForSigning(UUID leaseId, String locale) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);

    if (lease.getStatus() != LeaseStatus.PENDING_LANDLORD) {
      throw new BusinessConflictException(
          "Lease must be in PENDING_LANDLORD status to send renter for signing. "
              + "Current status: " + lease.getStatus());
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
          renter.getUserId().toString());

      // Update envelope ID if a new one was created
      if (!envelopeId.equals(lease.getDocusignEnvelopeId())) {
        lease.assignDocuSignEnvelope(envelopeId);
      }
    }
    // Template flow: renter is already a recipient (routing order 2), just generate
    // URL

    // Update status to PENDING_RENTER
    lease.submitToRenter();
    leaseAgreementRepository.save(lease);

    // Generate embedded signing URL
    String effectiveReturnUrl = buildDefaultReturnUrl(leaseId, "renter", locale);
    String signingUrl = docuSignService.getEmbeddedSigningUrl(
        envelopeId,
        renter.getEmail().getValue(),
        renter.getFirstName() + " " + renter.getLastName(),
        renter.getUserId().toString(),
        effectiveReturnUrl);

    log.info("Renter signing URL generated for lease {} (envelope {})", leaseId, envelopeId);
    return SigningUrlResponse.builder()
        .signingUrl(signingUrl)
        .envelopeId(envelopeId)
        .signerRole("renter")
        .build();
  }

  /**
   * Re-generates the embedded signing URL for the renter (URL expires after ~5
   * minutes).
   */
  public SigningUrlResponse getRenterSigningUrl(UUID leaseId, String locale) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);

    if (lease.getStatus() != LeaseStatus.PENDING_RENTER) {
      throw new BusinessConflictException(
          "Lease must be in PENDING_RENTER status. Current status: " + lease.getStatus());
    }
    if (lease.getDocusignEnvelopeId() == null || !docuSignService.isAvailable()) {
      throw new BusinessConflictException("DocuSign is not available or envelope was not created.");
    }

    User renter = findUserOrThrow(lease.getRenterId());
    String effectiveReturnUrl = buildDefaultReturnUrl(leaseId, "renter", locale);
    String signingUrl = docuSignService.getEmbeddedSigningUrl(
        lease.getDocusignEnvelopeId(),
        renter.getEmail().getValue(),
        renter.getFirstName() + " " + renter.getLastName(),
        renter.getUserId().toString(),
        effectiveReturnUrl);

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
   * @param leaseId UUID of the lease agreement
   * @param locale  Locale segment for the frontend return URL (e.g. "vi", "en")
   * @return {@link SigningUrlResponse} with the embedded signing URL
   */
  public SigningUrlResponse sendToLandlordForSigning(UUID leaseId, String locale) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);

    if (lease.getStatus() != LeaseStatus.DRAFT) {
      throw new BusinessConflictException(
          "Lease must be in DRAFT status to send for signing. Current status: " + lease.getStatus());
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

      // Compute contract creation date fields (current date at signing time)
      LocalDate contractDate = LocalDate.now();
      DateTimeFormatter handoverFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

      LeaseTemplateData templateData = LeaseTemplateData.builder()
          .renterName(renter.getFirstName() + " " + renter.getLastName())
          .renterEmail(renter.getEmail().getValue())
          .renterClientUserId(renter.getUserId().toString())
          .landlordName(landlord.getFirstName() + " " + landlord.getLastName())
          .landlordEmail(landlord.getEmail().getValue())
          .landlordClientUserId(landlord.getUserId().toString())
          .handoverDate(lease.getLeaseStartDate() != null
              ? lease.getLeaseStartDate().format(handoverFormatter)
              : "")
          .leaseDurationMonths(String.valueOf(lease.getLeaseDurationMonths()))
          .monthlyRent(lease.getMonthlyRent() != null
              ? VietnameseCurrencyUtil.formatAmount(lease.getMonthlyRent())
              : "")
          .monthlyRentByText(lease.getMonthlyRent() != null
              ? VietnameseCurrencyUtil.amountToWords(lease.getMonthlyRent())
              : "")
          .securityDeposit(lease.getSecurityDeposit() != null
              ? VietnameseCurrencyUtil.formatAmount(lease.getSecurityDeposit())
              : "")
          .securityDepositByText(lease.getSecurityDeposit() != null
              ? VietnameseCurrencyUtil.amountToWords(lease.getSecurityDeposit())
              : "")
          .contractDayOfWeek(VietnameseCurrencyUtil.getDayOfWeekVietnamese(contractDate.getDayOfWeek()))
          .contractDay(String.format("%02d", contractDate.getDayOfMonth()))
          .contractMonth(String.format("%02d", contractDate.getMonthValue()))
          .contractYear(String.valueOf(contractDate.getYear()))
          .build();

      envelopeId = docuSignService.createEnvelopeFromTemplate(
          docuSignConfig.getLeaseTemplateId(), templateData);

      log.info("Template envelope created for lease {} (envelope {})", leaseId, envelopeId);
    } else {
      // PDF-upload flow: download document and create envelope
      if (lease.getLeaseDocumentUrl() == null || lease.getLeaseDocumentUrl().isBlank()) {
        throw new BusinessConflictException(
            "Lease document URL is required before sending for signing. Upload the lease PDF first.");
      }

      byte[] documentBytes = downloadDocument(lease.getLeaseDocumentUrl());
      envelopeId = docuSignService.createEnvelopeForSigning(
          documentBytes,
          "Lease Agreement",
          landlord.getEmail().getValue(),
          landlord.getFirstName() + " " + landlord.getLastName(),
          landlord.getUserId().toString());
    }

    // Persist envelope ID and update status
    lease.assignDocuSignEnvelope(envelopeId);
    lease.submitToLandlord();
    leaseAgreementRepository.save(lease);

    // Generate embedded signing URL for landlord
    String effectiveReturnUrl = buildDefaultReturnUrl(leaseId, "landlord", locale);
    String signingUrl = docuSignService.getEmbeddedSigningUrl(
        envelopeId,
        landlord.getEmail().getValue(),
        landlord.getFirstName() + " " + landlord.getLastName(),
        landlord.getUserId().toString(),
        effectiveReturnUrl);

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
  public SigningUrlResponse getLandlordSigningUrl(UUID leaseId, String locale) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);

    if (lease.getStatus() != LeaseStatus.PENDING_LANDLORD) {
      throw new BusinessConflictException(
          "Lease must be in PENDING_LANDLORD status. Current status: " + lease.getStatus());
    }
    if (lease.getDocusignEnvelopeId() == null || !docuSignService.isAvailable()) {
      throw new BusinessConflictException("DocuSign is not available or envelope was not created.");
    }

    User landlord = findUserOrThrow(lease.getLandlordId());
    String effectiveReturnUrl = buildDefaultReturnUrl(leaseId, "landlord", locale);
    String signingUrl = docuSignService.getEmbeddedSigningUrl(
        lease.getDocusignEnvelopeId(),
        landlord.getEmail().getValue(),
        landlord.getFirstName() + " " + landlord.getLastName(),
        landlord.getUserId().toString(),
        effectiveReturnUrl);

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
   * @param envelopeId  DocuSign envelope ID from the webhook payload
   * @param eventStatus Envelope status from DocuSign (e.g. "completed",
   *                    "declined")
   */
  public void handleWebhookEvent(String envelopeId, String eventStatus) {
    leaseAgreementRepository.findByDocusignEnvelopeId(envelopeId).ifPresentOrElse(
        lease -> {
          log.info("DocuSign webhook: envelope {} -> status {} (lease {})",
              envelopeId, eventStatus, lease.getLeaseAgreementId());
          processEnvelopeStatusUpdate(lease, eventStatus);
          leaseAgreementRepository.save(lease);
        },
        () -> log.warn("DocuSign webhook: no lease found for envelope {}", envelopeId));
  }

  private void processEnvelopeStatusUpdate(LeaseAgreement lease, String eventStatus) {
    switch (eventStatus.toLowerCase()) {
      case "completed" -> {
        // DocuSign fires "completed" only when ALL signers are done.
        // In the template flow (landlord order-1, renter order-2) this means both
        // parties signed.
        // Accept both PENDING_RENTER (normal path after send-renter was called) and
        // PENDING_LANDLORD (template flow — renter already in envelope, send-renter not
        // needed).
        if (lease.getStatus() == LeaseStatus.PENDING_RENTER
            || lease.getStatus() == LeaseStatus.PENDING_LANDLORD) {
          lease.renterSignViaDocuSign();
          // Auto-mark the property as RENTED now that the lease is ACTIVE
          propertyRepository.findById(lease.getPropertyId()).ifPresent(property -> {
            property.markAsRented();
            propertyRepository.save(property);
            log.info("Property {} automatically marked as RENTED after lease {} completed signing",
                lease.getPropertyId(), lease.getLeaseAgreementId());
          });
        }
      }
      case "declined", "voided" -> {
        lease.reject("DocuSign envelope was " + eventStatus);
      }
      default -> lease.updateDocuSignStatus(eventStatus);
    }
  }

  // ── Lease State Transitions ───────────────────────────────────────────────

  /**
   * Confirms that the landlord has completed signing and transitions the lease
   * from PENDING_LANDLORD to PENDING_RENTER.
   * <p>
   * This is called by the frontend after DocuSign redirects back with
   * {@code event=signing_complete} on the landlord's return URL.
   */
  public LeaseResponse confirmLandlordSigned(UUID leaseId) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);

    if (lease.getStatus() != LeaseStatus.PENDING_LANDLORD) {
      throw new BusinessConflictException(
          "Lease must be in PENDING_LANDLORD status to confirm landlord signing. "
              + "Current status: " + lease.getStatus());
    }

    lease.submitToRenter();
    log.info("Lease {} transitioned to PENDING_RENTER after landlord confirmed signing", leaseId);
    return toEnrichedResponse(leaseAgreementRepository.save(lease));
  }

  public LeaseResponse rejectLease(UUID leaseId, String reason) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);
    lease.reject(reason);
    return toEnrichedResponse(leaseAgreementRepository.save(lease));
  }

  /**
   * Terminates an active lease agreement.
   * <p>
   * Business rules enforced:
   * <ul>
   * <li>The lease must be in {@link LeaseStatus#ACTIVE} status.</li>
   * <li>The caller must be the landlord of this specific lease.</li>
   * <li>The associated property is reset to AVAILABLE.</li>
   * <li>The renter is notified via all channels (WebSocket + FCM).</li>
   * </ul>
   *
   * @param leaseId the lease to terminate
   * @param request optional body carrying a termination reason
   * @return the updated lease response
   */
  public LeaseResponse terminateLease(UUID leaseId, TerminateLeaseRequest request) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);

    // Ownership check — caller must be the landlord of THIS lease
    UUID callerId = getCurrentUserId();
    if (!lease.getLandlordId().equals(callerId)) {
      throw new BusinessConflictException("Only the landlord of this lease can terminate it.");
    }

    String reason = (request != null) ? request.getReason() : null;

    // Domain method guards ACTIVE status and records terminatedAt
    lease.terminate(reason);
    leaseAgreementRepository.save(lease);
    log.info("Lease {} terminated by landlord {} — reason: {}", leaseId, callerId, reason);

    // Reset property to AVAILABLE so it can be listed again
    propertyRepository.findById(lease.getPropertyId()).ifPresent(property -> {
      property.markAsAvailable();
      propertyRepository.save(property);
      log.info("Property {} reset to AVAILABLE after lease {} termination",
          lease.getPropertyId(), leaseId);
    });

    // Notify renter
    User renter = findUserOrThrow(lease.getRenterId());
    notificationService.sendNotification(SendNotificationRequest.builder()
        .userId(renter.getUserId())
        .userEmail(renter.getEmail().getValue())
        .title("Hợp đồng thuê nhà đã bị chấm dứt")
        .message(reason != null
            ? "Chủ nhà đã chấm dứt hợp đồng thuê nhà của bạn. Lý do: " + reason
            : "Chủ nhà đã chấm dứt hợp đồng thuê nhà của bạn.")
        .eventType(EventType.LEASE_TERMINATED)
        .entityType(EntityType.LEASE)
        .entityId(leaseId)
        .build());

    return toEnrichedResponse(lease);
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

  private UUID getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof SecurityUserDetails userDetails) {
      return userDetails.getUserId();
    }
    throw new BusinessConflictException("Authenticated user not found in security context.");
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
          "Failed to download lease document from URL: " + documentUrl);
    }
  }

  private String buildDefaultReturnUrl(UUID leaseId, String role, String locale) {
    return docuSignConfig.getReturnUrl() + "/" + locale + "/leases/signing-complete"
        + "?leaseId=" + leaseId + "&role=" + role;
  }

  private PageResponse<LeaseResponse> toPageResponse(Page<LeaseAgreement> page) {
    return PageResponse.<LeaseResponse>builder()
        .content(page.getContent().stream().map(this::toEnrichedResponse).toList())
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .first(page.isFirst())
        .last(page.isLast())
        .build();
  }

  /**
   * Builds a {@link LeaseResponse} enriched with renter, landlord, and property
   * details.
   * Replaces the bare {@code leaseAgreementMapper.toResponse()} calls so that
   * list and
   * detail endpoints always include display-ready fields for the frontend.
   */
  private LeaseResponse toEnrichedResponse(LeaseAgreement lease) {
    LeaseResponse response = leaseAgreementMapper.toResponse(lease);

    // Enrich renter info
    userRepository.findById(lease.getRenterId()).ifPresent(renter -> {
      response.setRenterFullName(renter.getFullName());
      response.setRenterEmail(renter.getEmail().getValue());
      response.setRenterPhone(renter.getPhone());
      response.setRenterAvatarUrl(renter.getAvatarUrl());
    });

    // Enrich landlord info
    userRepository.findById(lease.getLandlordId()).ifPresent(landlord -> {
      response.setLandlordFullName(landlord.getFullName());
      response.setLandlordEmail(landlord.getEmail().getValue());
      response.setLandlordPhone(landlord.getPhone());
      response.setLandlordAvatarUrl(landlord.getAvatarUrl());
    });

    // Enrich property info (lazy association — safe within @Transactional context)
    Property property = lease.getProperty();
    if (property != null) {
      response.setPropertyTitle(property.getStreetAddress());
      response.setPropertyAddress(property.getStreetAddress());
      if (property.getPropertyType() != null) {
        response.setPropertyType(property.getPropertyType().getName());
      }
    }

    return response;
  }
}
