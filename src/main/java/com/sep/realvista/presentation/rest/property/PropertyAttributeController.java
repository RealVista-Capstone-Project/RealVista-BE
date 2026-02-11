package com.sep.realvista.presentation.rest.property;

import com.sep.realvista.application.property.PropertyAttributeService;
import com.sep.realvista.application.property.dto.PropertyAttributeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/property-attributes")
@RequiredArgsConstructor
@Tag(name = "Property Attributes", description = "Endpoints for property attribute metadata")
public class PropertyAttributeController {

    private final PropertyAttributeService propertyAttributeService;

    @Operation(summary = "Get Searchable Attributes by Property Type", 
               description = "Returns all searchable attributes for a " +
                             "specific property type (e.g., APARTMENT, VILLA)")
    @GetMapping("/property-types/{typeCode}")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PropertyAttributeDTO>> getAttributesByPropertyType(
            @PathVariable String typeCode) {
        List<PropertyAttributeDTO> attributes = propertyAttributeService
                .getSearchableAttributesByPropertyType(typeCode);
        return ResponseEntity.ok(attributes);
    }

    @Operation(summary = "Get Searchable Attributes by Property Category",
               description = "Returns all searchable attributes for a " +
                             "property category (e.g., RESIDENTIAL, COMMERCIAL)")
    @GetMapping("/property-categories/{categoryCode}")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PropertyAttributeDTO>> getAttributesByPropertyCategory(
            @PathVariable String categoryCode) {
        List<PropertyAttributeDTO> attributes = propertyAttributeService
                .getSearchableAttributesByPropertyCategory(categoryCode);
        return ResponseEntity.ok(attributes);
    }
}
