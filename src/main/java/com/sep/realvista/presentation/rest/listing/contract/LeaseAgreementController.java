package com.sep.realvista.presentation.rest.listing.contract;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.contract.LeaseAgreementApplicationService;
import com.sep.realvista.application.listing.contract.dto.CancelLeaseRequest;
import com.sep.realvista.application.listing.contract.dto.CreateLeaseRequest;
import com.sep.realvista.application.listing.contract.dto.LeaseResponse;
import com.sep.realvista.application.listing.contract.dto.SigningUrlResponse;
import com.sep.realvista.application.listing.contract.dto.TerminateLeaseRequest;
import com.sep.realvista.application.service.DocuSignService;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for lease agreement management and DocuSign eSignature
 * endpoints.
 * <p>
 * Endpoints:
 * 
 * <pre>
 *   POST   /api/v1/leases                              — Create lease draft
 *   GET    /api/v1/leases/{id}                         — Get lease by ID
 *   GET    /api/v1/leases/renter/{renterId}            — List by renter
 *   GET    /api/v1/leases/landlord/{landlordId}        — List by landlord
 *   GET    /api/v1/leases/agent/{agentId}              — List by agent
 *   GET    /api/v1/leases/property/{propertyId}        — List by property
 *   GET    /api/v1/leases/listing/{listingId}          — List by listing (resolves to property)
 *   POST   /api/v1/leases/{id}/send-renter             — Send to renter for signing
 *   GET    /api/v1/leases/{id}/renter-signing-url      — Get renter signing URL
 *   POST   /api/v1/leases/{id}/send-landlord           — Send to landlord for signing
 *   GET    /api/v1/leases/{id}/landlord-signing-url    — Get landlord signing URL
 *   POST   /api/v1/leases/{id}/confirm-landlord-signed — Confirm landlord signed (PENDING_LANDLORD→PENDING_RENTER)
 *   PUT    /api/v1/leases/{id}/reject                  — Reject lease
 *   PUT    /api/v1/leases/{id}/cancel                  — Cancel lease before active
 *   PUT    /api/v1/leases/{id}/terminate               — Terminate active lease
 *   POST   /api/v1/leases/docusign/webhook             — DocuSign Connect webhook (public)
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/leases")
@RequiredArgsConstructor
@Tag(name = "Lease Agreements", description = "Lease agreement management with DocuSign eSignature")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class LeaseAgreementController {

  private final LeaseAgreementApplicationService leaseService;
  private final DocuSignService docuSignService;

  // ── CRUD Endpoints ────────────────────────────────────────────────────────

  @PostMapping
  @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
  @Operation(summary = "Create lease draft", description = "Creates a new lease agreement in DRAFT status")
  public ResponseEntity<ApiResponse<LeaseResponse>> createLease(
      @Valid @RequestBody CreateLeaseRequest request) {
    LeaseResponse response = leaseService.createLease(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Lease agreement created successfully", response));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get lease by ID")
  public ResponseEntity<ApiResponse<LeaseResponse>> getLeaseById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success("Lease retrieved", leaseService.getLeaseById(id)));
  }

  @GetMapping("/renter/{renterId}")
  @PreAuthorize("hasAnyRole('TENANT', 'ADMIN')")
  @Operation(summary = "List leases by renter")
  public ResponseEntity<ApiResponse<PageResponse<LeaseResponse>>> getLeasesByRenter(
      @PathVariable UUID renterId,
      @Parameter(description = "Filter by lease status (e.g. ACTIVE, EXPIRED, TERMINATED). Omit to return all.")
      @RequestParam(required = false) LeaseStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(ApiResponse.success("Leases retrieved",
        leaseService.getLeasesByRenter(renterId, status, page, size)));
  }

  @GetMapping("/landlord/{landlordId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "List leases by landlord")
  public ResponseEntity<ApiResponse<PageResponse<LeaseResponse>>> getLeasesByLandlord(
      @PathVariable UUID landlordId,
      @Parameter(description = "Filter by lease status (e.g. ACTIVE, EXPIRED, TERMINATED). Omit to return all.")
      @RequestParam(required = false) LeaseStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(ApiResponse.success("Leases retrieved",
        leaseService.getLeasesByLandlord(landlordId, status, page, size)));
  }

  @GetMapping("/property/{propertyId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "List leases by property")
  public ResponseEntity<ApiResponse<PageResponse<LeaseResponse>>> getLeasesByProperty(
      @PathVariable UUID propertyId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(ApiResponse.success("Leases retrieved",
        leaseService.getLeasesByProperty(propertyId, page, size)));
  }

  @GetMapping("/listing/{listingId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "List leases by listing (resolves to property internally)")
  public ResponseEntity<ApiResponse<PageResponse<LeaseResponse>>> getLeasesByListing(
      @PathVariable UUID listingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(ApiResponse.success("Leases retrieved",
        leaseService.getLeasesByListing(listingId, page, size)));
  }

  @GetMapping("/agent/{agentId}")
  @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
  @Operation(summary = "List leases by agent", 
      description = "Returns all rental contracts that the agent facilitated (agentId matches). "
      + "Supports optional status filtering and pagination.")
  public ResponseEntity<ApiResponse<PageResponse<LeaseResponse>>> getLeasesByAgent(
      @PathVariable UUID agentId,
      @Parameter(description = "Filter by lease status (e.g. ACTIVE, EXPIRED, TERMINATED). Omit to return all.")
      @RequestParam(required = false) LeaseStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(ApiResponse.success("Leases retrieved",
        leaseService.getLeasesByAgent(agentId, status, page, size)));
  }

  // ── DocuSign Signing Endpoints ────────────────────────────────────────────

  @PostMapping("/{id}/send-renter")
  @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
  @Operation(summary = "Send lease to renter for signing (second step)", 
      description = "After the landlord has signed, call this to get an embedded signing URL for the renter. "
      + "The lease must be in PENDING_LANDLORD status. "
      + "The frontend should redirect the renter to the returned signingUrl.")
  public ResponseEntity<ApiResponse<SigningUrlResponse>> sendToRenterForSigning(
      @PathVariable UUID id,
      @Parameter(description = "Locale for the return URL path segment (default: vi)")
      @RequestParam(defaultValue = "vi") String locale) {
    SigningUrlResponse response = leaseService.sendToRenterForSigning(id, locale);
    return ResponseEntity.ok(ApiResponse.success("Signing URL generated for renter", response));
  }

  @GetMapping("/{id}/renter-signing-url")
  @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'TENANT', 'ADMIN')")
  @Operation(summary = "Get renter embedded signing URL", 
      description = "Regenerates the DocuSign embedded signing URL for the renter. "
      + "URLs expire in ~5 minutes so this can be called to refresh.")
  public ResponseEntity<ApiResponse<SigningUrlResponse>> getRenterSigningUrl(
      @PathVariable UUID id,
      @Parameter(description = "Locale for the return URL path segment (default: vi)")
      @RequestParam(defaultValue = "vi") String locale) {
    SigningUrlResponse response = leaseService.getRenterSigningUrl(id, locale);
    return ResponseEntity.ok(ApiResponse.success("Renter signing URL retrieved", response));
  }

  @PostMapping("/{id}/send-landlord")
  @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
  @Operation(summary = "Send lease to landlord for signing (first step)", 
      description = "Creates a DocuSign envelope and returns an embedded signing URL for the landlord. "
      + "Landlord signs first; after signing, call send-renter to get the renter signing URL.")
  public ResponseEntity<ApiResponse<SigningUrlResponse>> sendToLandlordForSigning(
      @PathVariable UUID id,
      @Parameter(description = "Locale for the return URL path segment (default: vi)")
      @RequestParam(defaultValue = "vi") String locale) {
    SigningUrlResponse response = leaseService.sendToLandlordForSigning(id, locale);
    return ResponseEntity.ok(ApiResponse.success("Signing URL generated for landlord", response));
  }

  @GetMapping("/{id}/landlord-signing-url")
  @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
  @Operation(summary = "Get landlord embedded signing URL", 
      description = "Regenerates the DocuSign embedded signing URL for the landlord.")
  public ResponseEntity<ApiResponse<SigningUrlResponse>> getLandlordSigningUrl(
      @PathVariable UUID id,
      @Parameter(description = "Locale for the return URL path segment (default: vi)")
      @RequestParam(defaultValue = "vi") String locale) {
    SigningUrlResponse response = leaseService.getLandlordSigningUrl(id, locale);
    return ResponseEntity.ok(ApiResponse.success("Landlord signing URL retrieved", response));
  }

  // ── Lease State Transitions ───────────────────────────────────────────────

  @PostMapping("/{id}/confirm-landlord-signed")
  @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
  @Operation(summary = "Confirm landlord has signed", 
      description = "Transitions the lease from PENDING_LANDLORD to PENDING_RENTER. "
      + "Call this endpoint after DocuSign redirects back to the frontend "
      + "with event=signing_complete on the landlord return URL.")
  public ResponseEntity<ApiResponse<LeaseResponse>> confirmLandlordSigned(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Landlord signing confirmed, lease is now pending renter",
        leaseService.confirmLandlordSigned(id)));
  }

  @PutMapping("/{id}/reject")
  @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
  @Operation(summary = "Reject lease agreement")
  public ResponseEntity<ApiResponse<LeaseResponse>> rejectLease(
      @PathVariable UUID id,
      @RequestBody(required = false) Map<String, String> body) {
    String reason = body != null ? body.getOrDefault("reason", null) : null;
    return ResponseEntity.ok(ApiResponse.success("Lease rejected", leaseService.rejectLease(id, reason)));
  }

  @PutMapping("/{id}/cancel")
  @PreAuthorize("hasAnyRole('OWNER', 'TENANT', 'ADMIN')")
  @Operation(summary = "Cancel lease agreement before active", 
      description = "Cancels a DRAFT, PENDING_LANDLORD, or PENDING_RENTER lease when the landlord or renter "
      + "does not want to continue the contract signing flow. This is different from termination, "
      + "which is only for ACTIVE leases.")
  public ResponseEntity<ApiResponse<LeaseResponse>> cancelLease(
      @PathVariable UUID id,
      @RequestBody(required = false) CancelLeaseRequest request) {
    return ResponseEntity.ok(ApiResponse.success("Lease cancelled", leaseService.cancelLease(id, request)));
  }

  @PutMapping("/{id}/terminate")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Terminate active lease agreement", 
      description = "Terminates an ACTIVE lease. Caller must be the landlord of the lease. "
      + "The property is automatically reset to AVAILABLE and the renter is notified.")
  public ResponseEntity<ApiResponse<LeaseResponse>> terminateLease(
      @PathVariable UUID id,
      @RequestBody(required = false) TerminateLeaseRequest request) {
    return ResponseEntity.ok(ApiResponse.success("Lease terminated",
        leaseService.terminateLease(id, request)));
  }

  // ── DocuSign Webhook (Public — no auth required) ──────────────────────────

  @PostMapping("/docusign/webhook")
  @Operation(summary = "DocuSign Connect webhook", 
      description = "Receives envelope status update events from DocuSign Connect. "
      + "Payload is HMAC-verified using the configured webhook key.")
  public ResponseEntity<Void> docuSignWebhook(
      @RequestHeader(value = "X-DocuSign-Signature-1", required = false) String hmacSignature,
      @RequestBody byte[] payload) {

    // Verify HMAC signature
    if (!docuSignService.verifyWebhookSignature(payload, hmacSignature)) {
      log.warn("DocuSign webhook rejected: invalid HMAC signature");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Parse envelope ID and status from the XML/JSON payload
    String payloadStr = new String(payload, StandardCharsets.UTF_8);
    log.info("DocuSign webhook received. Payload: {}", payloadStr);

    // DocuSign XML uses <EnvelopeID> (capital I and D); JSON uses "envelopeId"
    // nested in "data"
    String envelopeId = extractField(payloadStr, "EnvelopeID", "envelopeId");

    // DocuSign JSON uses "event": "envelope-completed"; XML uses
    // <Status>completed</Status>
    // Normalize by stripping the "envelope-" prefix from the event name
    String rawStatus = extractField(payloadStr, "Status", "event");
    String status = normalizeEventStatus(rawStatus);

    if (envelopeId != null && status != null) {
      leaseService.handleWebhookEvent(envelopeId, status);
    } else {
      log.warn("DocuSign webhook: could not extract envelopeId or status from payload");
    }

    // Always return 200 to DocuSign to acknowledge receipt
    return ResponseEntity.ok().build();
  }

  /**
   * Normalizes DocuSign event/status values to a consistent lowercase status
   * string.
   * <p>
   * DocuSign JSON payloads use an "event" field with values like
   * "envelope-completed",
   * while XML payloads use a "Status" field with values like "completed".
   * This method strips the "envelope-" prefix so both formats produce the same
   * value.
   */
  private String normalizeEventStatus(String rawStatus) {
    if (rawStatus == null) {
      return null;
    }
    return rawStatus.toLowerCase().replace("envelope-", "");
  }

  /**
   * Simple field extractor that handles both XML ({@code <Tag>value</Tag>}) and
   * JSON ({@code "field": "value"}) webhook payloads from DocuSign.
   */
  private String extractField(String payload, String xmlTag, String jsonField) {
    // Try XML format: <EnvelopeId>xxx</EnvelopeId>
    String xmlPattern = "<" + xmlTag + ">";
    int xmlStart = payload.indexOf(xmlPattern);
    if (xmlStart >= 0) {
      int valueStart = xmlStart + xmlPattern.length();
      int valueEnd = payload.indexOf("</" + xmlTag + ">", valueStart);
      if (valueEnd > valueStart) {
        return payload.substring(valueStart, valueEnd).trim();
      }
    }

    // Try JSON format: "envelopeId": "xxx"
    String jsonPattern = "\"" + jsonField + "\":";
    int jsonStart = payload.indexOf(jsonPattern);
    if (jsonStart >= 0) {
      int valueStart = payload.indexOf("\"", jsonStart + jsonPattern.length()) + 1;
      int valueEnd = payload.indexOf("\"", valueStart);
      if (valueEnd > valueStart) {
        return payload.substring(valueStart, valueEnd).trim();
      }
    }

    return null;
  }
}
