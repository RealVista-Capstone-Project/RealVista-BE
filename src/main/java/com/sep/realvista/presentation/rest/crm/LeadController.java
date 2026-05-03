package com.sep.realvista.presentation.rest.crm;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.crm.dto.AddLeadNoteRequest;
import com.sep.realvista.application.crm.dto.CreateLeadRequest;
import com.sep.realvista.application.crm.dto.LeadNoteResponse;
import com.sep.realvista.application.crm.dto.LeadResponse;
import com.sep.realvista.application.crm.dto.LeadStatusSummaryResultResponse;
import com.sep.realvista.application.crm.dto.LeadSummaryResponse;
import com.sep.realvista.application.crm.dto.UpdateLeadRequest;
import com.sep.realvista.application.crm.dto.UpdateLeadStatusRequest;
import com.sep.realvista.application.crm.service.LeadApplicationService;
import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/leads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('AGENT')")
@Tag(name = "CRM - Leads", description = "Agent CRM lead management")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class LeadController {

    private final LeadApplicationService leadApplicationService;

    @GetMapping
    @Operation(summary = "List leads", description = "Returns paginated leads owned by the authenticated agent.")
    @SuppressWarnings("checkstyle:ParameterNumber")
    public ResponseEntity<ApiResponse<PageResponse<LeadResponse>>> getLeads(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID listingId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID agentId = userDetails.getUserId();
        PageResponse<LeadResponse> result = leadApplicationService.getLeads(
                agentId, status, from, to, listingId, q, page, size);
        return ResponseEntity.ok(ApiResponse.success("Leads retrieved successfully", result));
    }

    @GetMapping("/summary")
    @Operation(summary = "Lead summary", description = "Returns CRM lead metrics for the authenticated agent.")
    public ResponseEntity<ApiResponse<LeadSummaryResponse>> getSummary(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID listingId,
            @RequestParam(required = false) String q
    ) {
        UUID agentId = userDetails.getUserId();
        LeadSummaryResponse result = leadApplicationService.getSummary(agentId, from, to, listingId, q);
        return ResponseEntity.ok(ApiResponse.success("Lead summary retrieved successfully", result));
    }

    @GetMapping("/status-summary")
    @Operation(summary = "Lead status summary", description = "Returns CRM lead counts grouped by status.")
    public ResponseEntity<ApiResponse<LeadStatusSummaryResultResponse>> getStatusSummary(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID listingId,
            @RequestParam(required = false) String q
    ) {
        UUID agentId = userDetails.getUserId();
        LeadStatusSummaryResultResponse result = leadApplicationService
                .getStatusSummary(agentId, from, to, listingId, q);
        return ResponseEntity.ok(ApiResponse.success("Lead status summary retrieved successfully", result));
    }

    @PostMapping
    @Operation(summary = "Create lead", description = "Creates a new CRM lead for the authenticated agent.")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @Valid @RequestBody CreateLeadRequest request
    ) {
        UUID agentId = userDetails.getUserId();
        LeadResponse result = leadApplicationService.createLead(agentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lead created successfully", result));
    }

    @PutMapping("/{leadId}")
    @Operation(summary = "Update lead", description = "Updates contact info and property interest of a lead.")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdateLeadRequest request
    ) {
        UUID agentId = userDetails.getUserId();
        LeadResponse result = leadApplicationService.updateLead(agentId, leadId, request);
        return ResponseEntity.ok(ApiResponse.success("Lead updated successfully", result));
    }

    @PatchMapping("/{leadId}/status")
    @Operation(summary = "Update lead status", description = "Moves a lead to a new pipeline stage.")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLeadStatus(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdateLeadStatusRequest request
    ) {
        UUID agentId = userDetails.getUserId();
        LeadResponse result = leadApplicationService.updateLeadStatus(agentId, leadId, request);
        return ResponseEntity.ok(ApiResponse.success("Lead status updated", result));
    }

    @DeleteMapping("/{leadId}")
    @Operation(summary = "Delete lead", description = "Soft-deletes a lead.")
    public ResponseEntity<ApiResponse<Void>> deleteLead(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID leadId
    ) {
        UUID agentId = userDetails.getUserId();
        leadApplicationService.deleteLead(agentId, leadId);
        return ResponseEntity.ok(ApiResponse.success("Lead deleted successfully", null));
    }

    @PostMapping("/{leadId}/notes")
    @Operation(summary = "Add note", description = "Adds a note to a lead, snapshotting current status.")
    public ResponseEntity<ApiResponse<LeadNoteResponse>> addNote(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable UUID leadId,
            @Valid @RequestBody AddLeadNoteRequest request
    ) {
        UUID agentId = userDetails.getUserId();
        LeadNoteResponse result = leadApplicationService.addNote(agentId, leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note added successfully", result));
    }
}
