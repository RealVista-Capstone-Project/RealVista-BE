package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.PropertyTypeAttribute;
import com.sep.realvista.infrastructure.persistence.listing.PropertyTypeRepository;
import com.sep.realvista.infrastructure.persistence.listing.PropertyTypeAttributeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/listings/metadata")
@RequiredArgsConstructor
@Tag(name = "Search Metadata", 
        description = "Endpoints for retrieving search filters and attributes")
public class SearchMetadataController {

    private final PropertyTypeRepository propertyTypeRepository;
    private final PropertyTypeAttributeRepository propertyTypeAttributeRepository;

    @Operation(summary = "Get Property Types", 
            description = "Get all available property types (e.g., Apartment, House)")
    @GetMapping("/property-types")
    public ResponseEntity<List<PropertyType>> getPropertyTypes() {
        return ResponseEntity.ok(propertyTypeRepository.findAll());
    }

    @Operation(summary = "Get Search Attributes", 
            description = "Get dynamic search attributes for a specific property type")
    @GetMapping("/attributes")
    public ResponseEntity<List<PropertyAttribute>> getAttributes(
            @RequestParam UUID propertyTypeId) {
        List<PropertyTypeAttribute> typeAttributes = 
                propertyTypeAttributeRepository
                        .findByPropertyTypeIdAndIsRequiredTrue(propertyTypeId);
        List<PropertyAttribute> attributes = typeAttributes.stream()
                .map(PropertyTypeAttribute::getPropertyAttribute)
                .filter(PropertyAttribute::getIsSearchable)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attributes);
    }
}
