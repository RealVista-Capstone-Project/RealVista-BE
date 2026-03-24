package com.sep.realvista.application.property.service;

import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.application.property.dto.CreatePropertyRequest;
import com.sep.realvista.application.property.dto.PropertyAttributeRequest;
import com.sep.realvista.application.property.dto.PropertyDetailResponse;
import com.sep.realvista.application.property.dto.PropertyMediaRequest;
import com.sep.realvista.application.property.dto.PropertySummaryResponse;
import com.sep.realvista.application.property.dto.UpdatePropertyRequest;
import com.sep.realvista.application.property.mapper.PropertyMapper;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRepository;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import com.sep.realvista.infrastructure.persistence.property.amenity.AmenityJpaRepository;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyApplicationService {

    private final PropertyRepository propertyRepository;
    private final PropertyMediaRepository propertyMediaRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final AmenityJpaRepository amenityJpaRepository;
    private final PropertyAttributeValueRepository propertyAttributeValueRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;
    private final PropertyMapper propertyMapper;
    private final EntityManager entityManager;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new IllegalStateException("Current user not found in security context");
    }

    @Transactional
    public PropertyDetailResponse createProperty(CreatePropertyRequest request) {
        UUID ownerId = getCurrentUserId();
        log.info("Creating property for owner: {}", ownerId);

        String titleSlug = UUID.randomUUID().toString(); // Temporary slug generation

        Property property = Property.builder()
                .ownerId(ownerId)
                .locationId(request.getLocationId())
                .propertyTypeId(resolvePropertyTypeId(request.getPropertyTypeCode()))
                .streetAddress(request.getStreetAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .landSizeM2(request.getLandSizeM2())
                .usableSizeM2(request.getUsableSizeM2())
                .widthM(request.getWidthM())
                .lengthM(request.getLengthM())
                .descriptions(request.getDescriptions())
                .extraAttributes(request.getExtraAttributes())
                .slug(titleSlug)
                .build();

        Property savedProperty = propertyRepository.save(property);
        UUID propertyId = savedProperty.getPropertyId();

        savedProperty.updateAmenities(buildAmenities(propertyId, request.getAmenityIds()));
        savedProperty.updateAttributes(buildAttributes(propertyId, request.getAttributes()));
        savedProperty.updateMedia(buildMedia(propertyId, request.getMedia(), ownerId));
        
        propertyRepository.save(savedProperty);
        entityManager.flush();
        entityManager.clear();

        return getPropertyDetails(propertyId);
    }

    @Transactional
    public PropertyDetailResponse updateProperty(UUID propertyId, UpdatePropertyRequest request) {
        UUID ownerId = getCurrentUserId();
        log.info("Updating property {} for owner: {}", propertyId, ownerId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (!property.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not authorized to update this property");
        }

        // Update basic fields
        if (request.getLocationId() != null || request.getPropertyTypeCode() != null) {
            property.updateLocationAndType(
                request.getLocationId(),
                request.getPropertyTypeCode() != null ? resolvePropertyTypeId(request.getPropertyTypeCode()) : null
            );
        }

        property.updateDetails(
            request.getStreetAddress(),
            request.getDescriptions(),
            null // Slug update logic can be added if needed
        );

        property.updateDimensions(
            request.getLandSizeM2(),
            request.getUsableSizeM2(),
            request.getWidthM(),
            request.getLengthM()
        );

        if (request.getLatitude() != null && request.getLongitude() != null) {
            property.updateCoordinates(request.getLatitude(), request.getLongitude());
        }

        if (request.getExtraAttributes() != null) {
            property.updateExtraAttributes(request.getExtraAttributes());
        }

        if (request.getAmenityIds() != null) {
            property.updateAmenities(buildAmenities(propertyId, request.getAmenityIds()));
        }

        if (request.getAttributes() != null) {
            property.updateAttributes(buildAttributes(propertyId, request.getAttributes()));
        }

        if (request.getMedia() != null) {
            property.updateMedia(buildMedia(propertyId, request.getMedia(), ownerId));
        }

        propertyRepository.save(property);
        entityManager.flush();
        entityManager.clear();

        return getPropertyDetails(propertyId);
    }

    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyDetails(UUID propertyId) {
        log.info("Getting details for property: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        List<PropertyMedia> media = propertyMediaRepository.findByPropertyId(propertyId);
        List<PropertyAmenity> amenities = propertyAmenityRepository
                .findByPropertyIdWithAmenity(propertyId);
        List<PropertyAttributeValue> attributes = propertyAttributeValueRepository
                .findByPropertyIdWithAttribute(propertyId);

        return propertyMapper.toDetailResponse(property, media, attributes, amenities);
    }

    @Transactional(readOnly = true)
    public List<PropertySummaryResponse> getMyProperties() {
        UUID ownerId = getCurrentUserId();
        log.info("Getting properties for owner: {}", ownerId);
        
        List<Property> properties = propertyRepository.findByOwnerId(ownerId);
        
        return properties.stream().map(property -> {
            // Find thumbnail media (is_primary = true) if any exists to pass to mapper
            String thumbnailUrl = propertyMediaRepository.findByPropertyId(property.getPropertyId())
                    .stream()
                    .filter(pm -> Boolean.TRUE.equals(pm.getIsPrimary()))
                    .findFirst()
                    .map(PropertyMedia::getThumbnailUrl)
                    .orElse(null);
                    
            return propertyMapper.toSummaryResponse(property, thumbnailUrl);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.sep.realvista.application.listing.dto.AmenityDTO> getAmenities() {
        log.info("Getting all amenities");
        return amenityJpaRepository.findAll().stream()
                .map(amenity -> com.sep.realvista.application.listing.dto.AmenityDTO.builder()
                        .amenityId(amenity.getAmenityId())
                        .amenityName(amenity.getAmenityName())
                        .amenityType(amenity.getAmenityType().name())
                        .description(amenity.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private UUID resolvePropertyTypeId(String propertyTypeCode) {
        if (propertyTypeCode == null || propertyTypeCode.isBlank()) {
            throw new IllegalArgumentException("Property type code is required");
        }

        // Try parsing as UUID first (backward compatibility)
        try {
            return UUID.fromString(propertyTypeCode);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, treat as code
        }

        return propertyTypeRepository.findByCode(propertyTypeCode)
                .map(com.sep.realvista.domain.property.PropertyType::getPropertyTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("PropertyType not found with code: %s", propertyTypeCode)));
    }

    private List<PropertyAmenity> buildAmenities(UUID propertyId, List<UUID> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new ArrayList<>();
        }
        return amenityIds.stream()
                .map(id -> PropertyAmenity.builder()
                        .propertyId(propertyId)
                        .amenityId(id)
                        .build())
                .collect(Collectors.toList());
    }

    private List<PropertyAttributeValue> buildAttributes(
            UUID propertyId, List<PropertyAttributeRequest> attributeRequests) {
        if (attributeRequests == null || attributeRequests.isEmpty()) {
            return new ArrayList<>();
        }
        return attributeRequests.stream()
                .map(req -> {
                    UUID attributeId = req.getAttributeId();
                    if (attributeId == null && req.getAttributeCode() != null 
                            && !req.getAttributeCode().isBlank()) {
                        attributeId = propertyAttributeRepository.findByCode(req.getAttributeCode())
                                .map(com.sep.realvista.domain.property.attribute.PropertyAttribute
                                        ::getPropertyAttributeId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        String.format("PropertyAttribute not found with code: %s", 
                                                req.getAttributeCode())));
                    }
                    return PropertyAttributeValue.builder()
                        .propertyId(propertyId)
                        .propertyAttributeId(attributeId)
                        .valueNumber(req.getValueNumber())
                        .valueText(req.getValueText())
                        .valueBoolean(req.getValueBoolean())
                        .build();
                })
                .collect(Collectors.toList());
    }

    private List<PropertyMedia> buildMedia(
            UUID propertyId, List<PropertyMediaRequest> mediaRequests, UUID ownerId) {
        if (mediaRequests == null || mediaRequests.isEmpty()) {
            return new ArrayList<>();
        }
        return mediaRequests.stream()
                .map(req -> PropertyMedia.builder()
                        .propertyId(propertyId)
                        .mediaUrl(req.getUrl())
                        .thumbnailUrl(req.getThumbnailUrl())
                        .mediaType(req.getType())
                        .isPrimary(req.getIsThumbnail() != null && req.getIsThumbnail())
                        .uploadBy(ownerId)
                        .build())
                .collect(Collectors.toList());
    }
}
