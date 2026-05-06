package com.sep.realvista.application.listing.contract;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.contract.dto.CancelLeaseRequest;
import com.sep.realvista.application.listing.contract.dto.CreateLeaseRequest;
import com.sep.realvista.application.listing.contract.dto.LeaseResponse;
import com.sep.realvista.application.listing.contract.dto.LeaseTemplateData;
import com.sep.realvista.application.listing.contract.dto.SigningUrlResponse;
import com.sep.realvista.application.listing.contract.dto.TerminateLeaseRequest;
import com.sep.realvista.application.listing.contract.mapper.LeaseAgreementMapper;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.DocuSignService;
import com.sep.realvista.infrastructure.config.DocuSignConfig;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
  private final PropertyMediaRepository propertyMediaRepository;
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
          "Property is not available for leasing. Current status: " + property.getStatus(),
          "ERROR_LEASE_PROPERTY_NOT_AVAILABLE",
          new Object[] {property.getStatus()});
    }

    // 3. Validate no active lease already exists for this property
    List<LeaseAgreement> activeLeases = leaseAgreementRepository
        .findActiveLeasesByPropertyId(request.getPropertyId());
    if (!activeLeases.isEmpty()) {
      throw new BusinessConflictException(
          "An active lease already exists for this property.", "ERROR_LEASE_ACTIVE_EXISTS");
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

  /**
   * Expires active leases whose fixed end date has already passed.
   * <p>
   * Property availability is intentionally unchanged because an expired contract
   * does not prove the tenant has returned possession.
   */
  @Scheduled(cron = "0 0 1 * * *")
  public void expireActiveLeasesPastEndDate() {
    LocalDate today = LocalDate.now();
    List<LeaseAgreement> leasesToExpire = leaseAgreementRepository.findActiveLeasesEndingBefore(today);

    for (LeaseAgreement lease : leasesToExpire) {
      lease.expire();
      leaseAgreementRepository.save(lease);
      log.info("Lease {} expired after end date {}", lease.getLeaseAgreementId(), lease.getLeaseEndDate());
    }
  }

  /**
   * Sends idempotent lease expiry reminders for the supported reminder windows.
   */
  @Scheduled(cron = "0 15 1 * * *")
  public void sendLeaseExpiryReminders() {
    LocalDate today = LocalDate.now();
    sendLeaseExpiryRemindersForWindow(30, today.plusDays(30));
    sendLeaseExpiryRemindersForWindow(7, today.plusDays(7));
    sendLeaseExpiryRemindersForWindow(0, today);
  }

  private void sendLeaseExpiryRemindersForWindow(int daysBeforeExpiry, LocalDate targetDate) {
    List<LeaseAgreement> leases = leaseAgreementRepository.findActiveLeasesEndingOn(targetDate);
    for (LeaseAgreement lease : leases) {
      if (lease.isExpiryReminderSent(daysBeforeExpiry)) {
        continue;
      }

      notifyLeaseExpiryReminder(lease, daysBeforeExpiry);
      lease.markExpiryReminderSent(daysBeforeExpiry);
      leaseAgreementRepository.save(lease);
      log.info("Lease {} expiry reminder sent for {} day window", lease.getLeaseAgreementId(), daysBeforeExpiry);
    }
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

  /**
   * Lists leases by agent ID with optional status filter.
   * Allows agents to view all rental contracts they facilitated.
   */
  @Transactional(readOnly = true)
  public PageResponse<LeaseResponse> getLeasesByAgent(UUID agentId, LeaseStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<LeaseAgreement> leases = (status != null)
        ? leaseAgreementRepository.findByAgentIdAndStatus(agentId, status, pageable)
        : leaseAgreementRepository.findByAgentId(agentId, pageable);
    return toPageResponse(leases);
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
              + "Current status: " + lease.getStatus(),
          "ERROR_LEASE_INVALID_STATUS_FOR_LANDLORD");
    }

    User renter = findUserOrThrow(lease.getRenterId());

    if (renter.getPhone() == null || renter.getPhone().isBlank()) {
      throw new BusinessConflictException(
          "Renter must have a valid phone number for SMS Authentication.",
          "ERROR_LEASE_PHONE_REQUIRED_FOR_SIGNING");
    }

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
          renter.getPhone(),
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
          "Lease must be in PENDING_RENTER status. Current status: " + lease.getStatus(),
          "ERROR_LEASE_INVALID_STATUS_FOR_RENTER");
    }
    if (lease.getDocusignEnvelopeId() == null || !docuSignService.isAvailable()) {
      throw new BusinessConflictException(
          "DocuSign is not available or envelope was not created.",
          "ERROR_LEASE_DOCUSIGN_UNAVAILABLE");
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
          "Lease must be in DRAFT status to send for signing. Current status: " + lease.getStatus(),
          "ERROR_LEASE_INVALID_STATUS_FOR_DRAFT");
    }

    User landlord = findUserOrThrow(lease.getLandlordId());

    if (landlord.getPhone() == null || landlord.getPhone().isBlank()) {
      throw new BusinessConflictException(
          "Landlord must have a valid phone number for SMS Authentication.",
          "ERROR_LEASE_PHONE_REQUIRED_FOR_SIGNING");
    }

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
      Property property = propertyRepository.findById(lease.getPropertyId())
          .orElseThrow(() -> new ResourceNotFoundException("Property", lease.getPropertyId()));

      // Compute contract creation date fields (current date at signing time)
      LocalDate contractDate = LocalDate.now();
      DateTimeFormatter handoverFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

      LeaseTemplateData templateData = LeaseTemplateData.builder()
          .renterName(renter.getFirstName() + " " + renter.getLastName())
          .renterEmail(renter.getEmail().getValue())
          .renterClientUserId(renter.getUserId().toString())
          .renterPhone(renter.getPhone())
          .landlordName(landlord.getFirstName() + " " + landlord.getLastName())
          .landlordEmail(landlord.getEmail().getValue())
          .landlordClientUserId(landlord.getUserId().toString())
          .landlordPhone(landlord.getPhone())
          .handoverDate(lease.getLeaseStartDate() != null
              ? lease.getLeaseStartDate().format(handoverFormatter)
              : "")
          .leaseDurationMonths(formatLeaseDuration(lease.getLeaseDurationMonths()))
          .propertyAvailable(formatArea(property.getLandSizeM2()))
          .propertyUsed(formatArea(property.getUsableSizeM2()))
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
          .currentWeekday(formatCurrentWeekday(contractDate))
          .currentDay(String.valueOf(contractDate.getDayOfMonth()))
          .currentMonth(String.valueOf(contractDate.getMonthValue()))
          .currentYear(String.valueOf(contractDate.getYear()))
          .build();

      envelopeId = docuSignService.createEnvelopeFromTemplate(
          docuSignConfig.getLeaseTemplateId(), templateData);

      log.info("Template envelope created for lease {} (envelope {})", leaseId, envelopeId);
    } else {
      // PDF-upload flow: download document and create envelope
      if (lease.getLeaseDocumentUrl() == null || lease.getLeaseDocumentUrl().isBlank()) {
        throw new BusinessConflictException(
            "Lease document URL is required before sending for signing. Upload the lease PDF first.",
            "ERROR_LEASE_DOCUMENT_URL_REQUIRED");
      }

      byte[] documentBytes = downloadDocument(lease.getLeaseDocumentUrl());
      envelopeId = docuSignService.createEnvelopeForSigning(
          documentBytes,
          "Lease Agreement",
          landlord.getEmail().getValue(),
          landlord.getFirstName() + " " + landlord.getLastName(),
          landlord.getPhone(),
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
          "Lease must be in PENDING_LANDLORD status. Current status: " + lease.getStatus(),
          "ERROR_LEASE_INVALID_STATUS_FOR_LANDLORD");
    }
    if (lease.getDocusignEnvelopeId() == null || !docuSignService.isAvailable()) {
      throw new BusinessConflictException(
          "DocuSign is not available or envelope was not created.",
          "ERROR_LEASE_DOCUSIGN_UNAVAILABLE");
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
          assertNoOtherActiveLeaseForProperty(lease);
          lease.renterSignViaDocuSign();
          lease.markSignedDocumentPending();
          // Auto-mark the property and its published rent listings as RENTED now that the lease is ACTIVE
          markPropertyAndRentListingsAsRented(lease);
          notifyLeaseSigned(lease);
        }
      }
      case "declined", "voided" -> {
        lease.reject("DocuSign envelope was " + eventStatus);
      }
      default -> lease.updateDocuSignStatus(eventStatus);
    }
  }

  private void markPropertyAndRentListingsAsRented(LeaseAgreement lease) {
    propertyRepository.findById(lease.getPropertyId()).ifPresent(property -> {
      property.markAsRented();
      propertyRepository.save(property);
      log.info("Property {} automatically marked as RENTED after lease {} completed signing",
          lease.getPropertyId(), lease.getLeaseAgreementId());
    });

    UUID closedByUserId = lease.getAgentId() != null ? lease.getAgentId() : lease.getLandlordId();
    List<Listing> rentedListings = listingRepository.findByPropertyId(lease.getPropertyId()).stream()
        .filter(listing -> listing.getListingType() == ListingType.RENT)
        .filter(listing -> listing.getStatus() == ListingStatus.PUBLISHED)
        .peek(listing -> listing.markAsRentedDueToPropertyClosure(closedByUserId))
        .toList();

    if (!rentedListings.isEmpty()) {
      listingRepository.saveAll(rentedListings);
      log.info("Marked {} rent listings as RENTED after lease {} completed signing for property {}",
          rentedListings.size(), lease.getLeaseAgreementId(), lease.getPropertyId());
    }
  }

  private void notifyLeaseSigned(LeaseAgreement lease) {
    String propertyAddress = propertyRepository.findById(lease.getPropertyId())
        .map(Property::getStreetAddress)
        .orElse("");
    Map<String, Object> variables = Map.of(
        "leaseId", lease.getLeaseAgreementId().toString(),
        "propertyAddress", propertyAddress);

    notifyLeaseSignedRecipient(lease.getLandlordId(), variables, lease.getLeaseAgreementId());
    notifyLeaseSignedRecipient(lease.getRenterId(), variables, lease.getLeaseAgreementId());
  }

  private void notifyLeaseSignedRecipient(UUID userId, Map<String, Object> variables, UUID leaseId) {
    try {
      notificationService.sendDbNotification(
          userId,
          "LEASE_SIGNED",
          "vi",
          variables,
          EventType.LEASE_SIGNED,
          EntityType.LEASE,
          leaseId);
    } catch (Exception e) {
      log.warn("Failed to send lease signed notification to user {} for lease {}: {}",
          userId, leaseId, e.getMessage());
    }
  }

  private void notifyLeaseExpiryReminder(LeaseAgreement lease, int daysBeforeExpiry) {
    String propertyAddress = propertyRepository.findById(lease.getPropertyId())
        .map(Property::getStreetAddress)
        .orElse("");
    Map<String, Object> variables = Map.of(
        "leaseId", lease.getLeaseAgreementId().toString(),
        "propertyAddress", propertyAddress,
        "leaseEndDate", lease.getLeaseEndDate() != null ? lease.getLeaseEndDate().toString() : "",
        "daysBeforeExpiry", daysBeforeExpiry
    );

    notifyLeaseExpiryReminderRecipient(lease.getLandlordId(), variables, lease.getLeaseAgreementId());
    notifyLeaseExpiryReminderRecipient(lease.getRenterId(), variables, lease.getLeaseAgreementId());
    if (lease.getAgentId() != null) {
      notifyLeaseExpiryReminderRecipient(lease.getAgentId(), variables, lease.getLeaseAgreementId());
    }
  }

  private void notifyLeaseExpiryReminderRecipient(UUID userId, Map<String, Object> variables, UUID leaseId) {
    try {
      notificationService.sendDbNotification(
          userId,
          "LEASE_EXPIRY_REMINDER",
          "vi",
          variables,
          EventType.LEASE_EXPIRY_REMINDER,
          EntityType.LEASE,
          leaseId
      );
    } catch (RuntimeException e) {
      log.warn("Failed to send lease expiry reminder to user {} for lease {}: {}",
          userId, leaseId, e.getMessage());
      throw e;
    }
  }

  private void notifyLandlordSigned(LeaseAgreement lease) {
    String propertyAddress = propertyRepository.findById(lease.getPropertyId())
        .map(Property::getStreetAddress)
        .orElse("");
    Map<String, Object> variables = Map.of(
        "leaseId", lease.getLeaseAgreementId().toString(),
        "propertyAddress", propertyAddress);

    notifyLandlordSignedRecipient(lease.getLandlordId(), variables, lease.getLeaseAgreementId());
    notifyLandlordSignedRecipient(lease.getRenterId(), variables, lease.getLeaseAgreementId());
  }

  private void notifyLandlordSignedRecipient(UUID userId, Map<String, Object> variables, UUID leaseId) {
    try {
      notificationService.sendDbNotification(
          userId,
          "LEASE_LANDLORD_SIGNED",
          "vi",
          variables,
          EventType.LEASE_LANDLORD_SIGNED,
          EntityType.LEASE,
          leaseId);
    } catch (Exception e) {
      log.warn("Failed to send landlord signed notification to user {} for lease {}: {}",
          userId, leaseId, e.getMessage());
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

    if (lease.getStatus() == LeaseStatus.PENDING_RENTER) {
      log.info("Lease {} landlord signing was already confirmed; returning current PENDING_RENTER state", leaseId);
      return toEnrichedResponse(lease);
    }

    if (lease.getStatus() != LeaseStatus.PENDING_LANDLORD) {
      throw new BusinessConflictException(
          "Lease must be in PENDING_LANDLORD status to confirm landlord signing. "
              + "Current status: " + lease.getStatus(),
          "ERROR_LEASE_INVALID_STATUS_FOR_LANDLORD");
    }

    lease.submitToRenter();
    notifyLandlordSigned(lease);
    log.info("Lease {} transitioned to PENDING_RENTER after landlord confirmed signing", leaseId);
    return toEnrichedResponse(leaseAgreementRepository.save(lease));
  }

  public LeaseResponse rejectLease(UUID leaseId, String reason) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);
    lease.reject(reason);
    return toEnrichedResponse(leaseAgreementRepository.save(lease));
  }

  /**
   * Cancels a lease agreement before it becomes active.
   * <p>
   * This is different from termination: cancellation is allowed only while the
   * contract is still a draft or in the DocuSign signing phase. Either the renter
   * or landlord can cancel their own contract; admins can cancel any contract.
   */
  public LeaseResponse cancelLease(UUID leaseId, CancelLeaseRequest request) {
    LeaseAgreement lease = findLeaseOrThrow(leaseId);
    UUID callerId = getCurrentUserId();

    if (!lease.getLandlordId().equals(callerId)
        && !lease.getRenterId().equals(callerId)
        && !hasCurrentUserRole("ROLE_ADMIN")) {
      throw new BusinessConflictException(
          "Only the landlord, renter, or an admin can cancel this lease.",
          "ERROR_LEASE_CANCEL_PARTICIPANT_ONLY");
    }

    String reason = request != null ? request.getReason() : null;
    lease.cancel(reason, callerId);
    LeaseAgreement saved = leaseAgreementRepository.save(lease);
    log.info("Lease {} cancelled by user {} — reason: {}", leaseId, callerId, reason);
    return toEnrichedResponse(saved);
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
      throw new BusinessConflictException(
          "Only the landlord of this lease can terminate it.",
          "ERROR_LEASE_TERMINATE_LANDLORD_ONLY");
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
    notificationService.sendDbNotification(
        renter.getUserId(),
        "LEASE_TERMINATED",
        "vi",
        Map.of("reason", reason != null ? reason : "Không có lý do cụ thể"),
        EventType.LEASE_TERMINATED,
        EntityType.LEASE,
        leaseId);

    return toEnrichedResponse(lease);
  }

  // ── Private Helpers ───────────────────────────────────────────────────────

  private void assertNoOtherActiveLeaseForProperty(LeaseAgreement lease) {
    boolean anotherActiveLeaseExists = leaseAgreementRepository.findActiveLeasesByPropertyId(lease.getPropertyId())
        .stream()
        .anyMatch(activeLease -> !activeLease.getLeaseAgreementId().equals(lease.getLeaseAgreementId()));
    if (anotherActiveLeaseExists) {
      throw new BusinessConflictException(
          "Another active lease already exists for this property.",
          "ERROR_LEASE_ACTIVE_EXISTS");
    }
  }

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
    throw new BusinessConflictException(
        "Authenticated user not found in security context.",
        "ERROR_USER_NOT_IN_SECURITY_CONTEXT");
  }

  private boolean hasCurrentUserRole(String role) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.getAuthorities().stream()
        .anyMatch(authority -> role.equals(authority.getAuthority()));
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
          "Failed to download lease document from URL: " + documentUrl, "ERROR_LEASE_DOCUMENT_DOWNLOAD_FAILED");
    }
  }

  private String formatLeaseDuration(Integer durationMonths) {
    if (durationMonths == null) {
      return "";
    }

    if (durationMonths % 12 == 0) {
      return String.valueOf(durationMonths / 12);
    }

    return java.math.BigDecimal.valueOf(durationMonths)
        .divide(java.math.BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString();
  }

  private String formatArea(java.math.BigDecimal area) {
    return area != null ? area.stripTrailingZeros().toPlainString() : "";
  }

  private String formatCurrentWeekday(LocalDate date) {
    if (date.getDayOfWeek().getValue() == 7) {
      return "Chủ Nhật";
    }
    return "Thứ " + (date.getDayOfWeek().getValue() + 1);
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
      response.setRenterAvatarUrl(nullToEmpty(renter.getAvatarUrl()));
    });
    response.setRenterAvatarUrl(nullToEmpty(response.getRenterAvatarUrl()));

    // Enrich landlord info
    userRepository.findById(lease.getLandlordId()).ifPresent(landlord -> {
      response.setLandlordFullName(landlord.getFullName());
      response.setLandlordEmail(landlord.getEmail().getValue());
      response.setLandlordPhone(landlord.getPhone());
      response.setLandlordAvatarUrl(nullToEmpty(landlord.getAvatarUrl()));
    });
    response.setLandlordAvatarUrl(nullToEmpty(response.getLandlordAvatarUrl()));

    // Enrich property info. The lazy association may be absent in paged list
    // queries,
    // so fall back to the repository to keep list responses display-ready.
    Property property = lease.getProperty();
    if (property == null) {
      property = propertyRepository.findById(lease.getPropertyId()).orElse(null);
    }
    if (property != null) {
      response.setPropertyTitle(property.getStreetAddress());
      response.setPropertyAddress(property.getStreetAddress());
      if (property.getPropertyType() != null) {
        response.setPropertyType(property.getPropertyType().getName());
      }
      enrichPropertyMedia(response, property.getPropertyId());
    }

    return response;
  }

  private void enrichPropertyMedia(LeaseResponse response, UUID propertyId) {
    List<PropertyMedia> media = propertyMediaRepository.findByPropertyId(propertyId).stream()
        .filter(PropertyMedia::isImage)
        .toList();

    PropertyMedia selected = media.stream()
        .filter(item -> Boolean.TRUE.equals(item.getIsPrimary()))
        .findFirst()
        .orElseGet(() -> media.stream().findFirst().orElse(null));

    if (selected != null) {
      response.setPropertyThumbnailUrl(nullToEmpty(selected.getThumbnailUrl() != null
          ? selected.getThumbnailUrl()
          : selected.getMediaUrl()));
      response.setPropertyImageUrl(nullToEmpty(selected.getMediaUrl()));
      return;
    }

    response.setPropertyThumbnailUrl("");
    response.setPropertyImageUrl("");
  }

  private String nullToEmpty(String value) {
    return value != null ? value : "";
  }
}
