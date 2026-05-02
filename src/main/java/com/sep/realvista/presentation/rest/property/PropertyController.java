package com.sep.realvista.presentation.rest.property;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
import com.sep.realvista.application.property.dto.CreatePropertyRequest;
import com.sep.realvista.application.property.dto.PropertyDetailResponse;
import com.sep.realvista.application.property.dto.PropertyFeedCriteria;
import com.sep.realvista.application.property.dto.PropertyFeedItemResponse;
import com.sep.realvista.application.property.dto.PropertySearchCriteria;
import com.sep.realvista.application.property.dto.PropertySummaryMetricsResponse;
import com.sep.realvista.application.property.dto.PropertySummaryResponse;
import com.sep.realvista.application.property.dto.UpdatePropertyRequest;
import com.sep.realvista.application.property.service.PropertyApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Endpoints for Property CRUD operations")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class PropertyController {

  private final PropertyApplicationService propertyApplicationService;

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Create a new property", description = "Allows an authenticated user to create a draft property")
  public ResponseEntity<ApiResponse<PropertyDetailResponse>> createProperty(
      @Valid @RequestBody CreatePropertyRequest request) {
    log.info("REST request to create Property");
    PropertyDetailResponse response = propertyApplicationService.createProperty(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Property created successfully", response));
  }

  @PutMapping("/{propertyId}")
  @PreAuthorize("isAuthenticated()")
    @Operation(
      summary = "Update an existing property",
      description = "Allows the owner to update their property")
  public ResponseEntity<ApiResponse<PropertyDetailResponse>> updateProperty(
      @PathVariable UUID propertyId,
      @Valid @RequestBody UpdatePropertyRequest request) {
    log.info("REST request to update Property: {}", propertyId);
    PropertyDetailResponse response = propertyApplicationService.updateProperty(propertyId, request);
    return ResponseEntity.ok(ApiResponse.success("Property updated successfully", response));
  }

  @GetMapping("/{propertyId}")
  @Operation(
      summary = "Get property details by ID",
      description = "Retrieves complete property details including attributes and media")
  public ResponseEntity<ApiResponse<PropertyDetailResponse>> getPropertyDetails(@PathVariable UUID propertyId) {
    log.info("REST request to get Property details: {}", propertyId);
    PropertyDetailResponse response = propertyApplicationService.getPropertyDetails(propertyId);
    return ResponseEntity.ok(ApiResponse.success("Property details retrieved successfully", response));
  }

  @GetMapping("/me")
  @Operation(summary = "Get current user's properties", description = "Retrieves a paginated list of properties owned "
      + "by the authenticated user with optional search")
  public ResponseEntity<ApiResponse<PageResponse<PropertySummaryResponse>>> getMyProperties(
      @ParameterObject PropertySearchCriteria criteria,
      @PageableDefault() Pageable pageable) {
    log.info("REST request to get current user's properties with criteria: {}", criteria);

    PageResponse<PropertySummaryResponse> response = propertyApplicationService.getMyProperties(criteria,
        pageable);

    return ResponseEntity.ok(ApiResponse.success("My properties retrieved successfully", response));
  }

  @GetMapping("/me/summary")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get current user's property summary metrics", description = "Retrieves aggregated property"
      + " counts by status for the authenticated user.")
  public ResponseEntity<ApiResponse<PropertySummaryMetricsResponse>> getMyPropertiesSummary() {
    log.info("REST request to get current user's property summary metrics");
    PropertySummaryMetricsResponse response = propertyApplicationService.getMyPropertiesSummary();
    return ResponseEntity.ok(ApiResponse.success("Property summary metrics retrieved successfully", response));
  }

  @GetMapping("/amenities")
  @Operation(
      summary = "Get all amenities",
      description = "Retrieves the master list of all available property amenities")
  public ResponseEntity<ApiResponse<List<AmenityDTO>>> getAmenities() {
    log.info("REST request to get amenities");
    List<AmenityDTO> response = propertyApplicationService.getAmenities();
    return ResponseEntity.ok(ApiResponse.success("Amenities retrieved successfully", response));
  }

  @GetMapping("/attributes")
    @Operation(
      summary = "Get property attributes with ranges",
      description = "If property_type_code is provided, returns only attributes for that type ordered by priority. "
      + "Otherwise returns all searchable attributes.")
  public ResponseEntity<ApiResponse<List<PropertyAttributeDTO>>> getAttributes(
      @RequestParam(name = "property_type_id", required = false) String propertyTypeCode) {
    log.info("REST request to get attributes, property_type_code={}", propertyTypeCode);
    List<PropertyAttributeDTO> response = propertyTypeCode != null
        ? propertyApplicationService.getAttributesByPropertyType(propertyTypeCode)
        : propertyApplicationService.getAttributesWithRanges();
    return ResponseEntity.ok(ApiResponse.success("Attributes retrieved successfully", response));
  }

  @GetMapping("/types")
    @Operation(
      summary = "Get all active property types",
      description = "Retrieves the full list of active property types with their category info."
      + " Useful for populating filter dropdowns on the client.")
  public ResponseEntity<ApiResponse<List<PropertyTypeInfoDTO>>> getPropertyTypes() {
    log.info("REST request to get all active property types");
    List<PropertyTypeInfoDTO> response = propertyApplicationService.getPropertyTypes();
    return ResponseEntity.ok(ApiResponse.success("Property types retrieved successfully", response));
  }

  @PostMapping("/{propertyId}/verify-agent")
  @PreAuthorize("isAuthenticated()")
    @Operation(
      summary = "Verify property by agent",
      description = "Allows an agent to verify a property they created for an owner via OTP success")
  public ResponseEntity<ApiResponse<PropertyDetailResponse>> verifyPropertyByAgent(
      @PathVariable UUID propertyId) {
    log.info("REST request to verify Property: {}", propertyId);
    PropertyDetailResponse response = propertyApplicationService.verifyPropertyByAgent(propertyId);
    return ResponseEntity.ok(ApiResponse.success("Property verified successfully", response));
  }

  @PostMapping("/{propertyId}/assign-agent")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Assign current user as agent to an existing property",
      description = "Creates a link in property_agent table for the authenticated user")
  public ResponseEntity<ApiResponse<PropertyDetailResponse>> assignAgentToProperty(
      @PathVariable UUID propertyId) {
    log.info("REST request to assign Agent to Property: {}", propertyId);
    PropertyDetailResponse response = propertyApplicationService.assignAgentToProperty(propertyId);
    return ResponseEntity.ok(ApiResponse.success("Agent assigned successfully", response));
  }

  @GetMapping("/search")
  @Operation(
      summary = "Search for properties",
      description = "Search for properties by address text or geographical bounding box")
  public ResponseEntity<ApiResponse<List<PropertySummaryResponse>>> searchProperties(
      @RequestParam(required = false) String address,
      @RequestParam(name = "north_lat", required = false) BigDecimal northLat,
      @RequestParam(name = "south_lat", required = false) BigDecimal southLat,
      @RequestParam(name = "east_lng", required = false) BigDecimal eastLng,
      @RequestParam(name = "west_lng", required = false) BigDecimal westLng) {
    log.info("REST request to search properties by address or bbox");
    List<PropertySummaryResponse> response = propertyApplicationService.searchProperties(
        address, northLat, southLat, eastLng, westLng);
    return ResponseEntity.ok(ApiResponse.success("Properties found successfully", response));
  }

  @PatchMapping("/{propertyId}/status")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Update property status",
      description = "Allows the property owner to update the status of their property")
  public ResponseEntity<ApiResponse<PropertyDetailResponse>> updatePropertyStatus(
      @PathVariable UUID propertyId,
      @RequestParam String status) {
    log.info("REST request to update status of Property {} to {}", propertyId, status);
    PropertyDetailResponse response = propertyApplicationService.updatePropertyStatus(propertyId, status);
    return ResponseEntity.ok(ApiResponse.success("Property status updated successfully", response));
  }

  @DeleteMapping("/{propertyId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Soft delete a property",
      description = "Marks a property as deleted (soft delete). Only the owner can delete their property.")
  public ResponseEntity<ApiResponse<Void>> deleteProperty(@PathVariable UUID propertyId) {
    log.info("REST request to soft-delete Property: {}", propertyId);
    propertyApplicationService.softDeleteProperty(propertyId);
    return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
  }

  @GetMapping("/feed")
  @PreAuthorize("hasRole('AGENT')")
  @Operation(summary = "Get property feed for agents", description = "Returns a paginated feed of AVAILABLE properties"
      + " that agents can browse and submit proposals for."
      + " Excludes properties the agent is already assigned to."
      + " The 'has_active_proposal' flag indicates the agent"
      + " has already submitted a proposal for that property."
      + " Supports optional filtering by keyword, type, location.")
  public ResponseEntity<ApiResponse<PageResponse<PropertyFeedItemResponse>>> getPropertyFeed(
      @AuthenticationPrincipal SecurityUserDetails userDetails,
      @ParameterObject PropertyFeedCriteria criteria,
      @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
    UUID agentId = userDetails.getUserId();
    log.info("REST request to get property feed for agent: {}", agentId);
    org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
        org.springframework.data.domain.Sort.by("createdAt").descending());
    PageResponse<PropertyFeedItemResponse> response = propertyApplicationService.getPropertyFeed(agentId, criteria,
        pageable);
    return ResponseEntity.ok(ApiResponse.success("Property feed retrieved successfully", response));
  }
}
