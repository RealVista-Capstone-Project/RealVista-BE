package com.sep.realvista.presentation.rest.property;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.property.dto.CreatePropertyFeeRequest;
import com.sep.realvista.application.property.dto.PropertyFeeResponse;
import com.sep.realvista.application.property.dto.SyncPropertyFeesRequest;
import com.sep.realvista.application.property.service.PropertyFeeApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/fees")
@RequiredArgsConstructor
@Tag(name = "Property Fees", description = "Manage recurring service fees for a property")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class PropertyFeeController {

    private final PropertyFeeApplicationService feeService;

    @GetMapping
    @Operation(summary = "List fees for a property",
            description = "Returns all active service fees linked to the property")
    public ResponseEntity<ApiResponse<List<PropertyFeeResponse>>> getFees(@PathVariable UUID propertyId) {
        log.info("REST request to get fees for property {}", propertyId);
        List<PropertyFeeResponse> fees = feeService.getFees(propertyId);
        return ResponseEntity.ok(ApiResponse.success("Fees retrieved successfully", fees));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a single service fee", description = "Appends one fee entry to the property")
    public ResponseEntity<ApiResponse<PropertyFeeResponse>> addFee(
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreatePropertyFeeRequest request) {
        log.info("REST request to add fee to property {}", propertyId);
        PropertyFeeResponse fee = feeService.addFee(propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee added successfully", fee));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Sync service fees",
        description = "Replaces all existing fees for the property with the provided list. "
            + "Pass an empty list to remove all fees.")
    public ResponseEntity<ApiResponse<List<PropertyFeeResponse>>> syncFees(
            @PathVariable UUID propertyId,
            @Valid @RequestBody SyncPropertyFeesRequest request) {
        log.info("REST request to sync fees for property {}", propertyId);
        List<PropertyFeeResponse> fees = feeService.syncFees(propertyId, request.getFees());
        return ResponseEntity.ok(ApiResponse.success("Fees synced successfully", fees));
    }

    @DeleteMapping("/{feeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a service fee", description = "Soft-deletes a single fee entry")
    public ResponseEntity<ApiResponse<Void>> deleteFee(
            @PathVariable UUID propertyId,
            @PathVariable UUID feeId) {
        log.info("REST request to delete fee {} from property {}", feeId, propertyId);
        feeService.deleteFee(propertyId, feeId);
        return ResponseEntity.ok(ApiResponse.success("Fee deleted successfully", null));
    }
}
