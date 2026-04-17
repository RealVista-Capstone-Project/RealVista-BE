package com.sep.realvista.application.property.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.property.dto.CreatePropertyRequest;
import com.sep.realvista.application.property.dto.PropertyAttributeRequest;
import com.sep.realvista.application.property.dto.PropertyDetailResponse;
import com.sep.realvista.application.property.dto.PropertyFeedCriteria;
import com.sep.realvista.application.property.dto.PropertyFeedItemResponse;
import com.sep.realvista.application.property.dto.PropertyMediaRequest;
import com.sep.realvista.application.property.dto.PropertySearchCriteria;
import com.sep.realvista.application.property.dto.PropertySummaryResponse;
import com.sep.realvista.application.property.dto.UpdatePropertyRequest;
import com.sep.realvista.application.property.mapper.PropertyMapper;
import com.sep.realvista.domain.agent.PropertyAgent;
import com.sep.realvista.domain.agent.PropertyAgentRepository;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRangeRepository;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRepository;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.attribute.repository.PropertyTypeAttributeRepository;
import com.sep.realvista.domain.property.location.LocationRepository;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeRangeDTO;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.application.listing.dto.ListingSummaryDTO;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.repository.SettingPreferenceRepository;
import com.sep.realvista.infrastructure.persistence.property.amenity.AmenityJpaRepository;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
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
    private final PropertyAgentRepository propertyAgentRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final PropertyAttributeRangeRepository propertyAttributeRangeRepository;
    private final PropertyTypeAttributeRepository propertyTypeAttributeRepository;
    private final ListingRepository listingRepository;
    private final PropertyMapper propertyMapper;
    private final EntityManager entityManager;
    private final AgentProposalRepository agentProposalRepository;
    private final SettingPreferenceRepository settingPreferenceRepository;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new IllegalStateException("Current user not found in security context");
    }

    private void applyOwnerContactForAgentView(
            PropertyFeedItemResponse item, User owner, SettingPreference preference) {
        item.setOwnerName(owner.getFullName());
        boolean showEmail = preference != null && Boolean.FALSE.equals(preference.getHideEmail());
        boolean showPhone = preference != null && Boolean.FALSE.equals(preference.getHidePhoneNumber());
        if (showEmail && owner.getEmail() != null) {
            item.setOwnerEmail(owner.getEmail().getValue());
        }
        if (showPhone && owner.getPhone() != null && !owner.getPhone().isBlank()) {
            item.setOwnerPhone(owner.getPhone());
        }
    }

    @Transactional
    public PropertyDetailResponse createProperty(CreatePropertyRequest request) {
        UUID currentUserId = getCurrentUserId();
        UUID ownerId = request.getOwnerId() != null ? request.getOwnerId() : currentUserId;
        boolean isAgentCreatingForOwner = !ownerId.equals(currentUserId);

        log.info("Creating property for owner: {}. Created by: {}", ownerId, currentUserId);

        String titleSlug = UUID.randomUUID().toString(); // Temporary slug generation

        UUID propertyLocationId = request.getLocationId() != null ? request.getLocationId()
                : resolveLocationId(request.getLatitude(), request.getLongitude());

        PropertyStatus finalStatus = isAgentCreatingForOwner ? PropertyStatus.PENDING : PropertyStatus.DRAFT;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                finalStatus = PropertyStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid property status provided: {}. Using default.", request.getStatus());
            }
        }

        Property property = Property.builder()
                .ownerId(ownerId)
                .locationId(propertyLocationId)
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
                .priceRange(request.getPriceRange())
                .status(finalStatus)
                .slug(titleSlug)
                .build();

        Property savedProperty = propertyRepository.save(property);
        UUID propertyId = savedProperty.getPropertyId();

        if (isAgentCreatingForOwner) {
            PropertyAgent propertyAgent = PropertyAgent.builder()
                    .propertyId(propertyId)
                    .agentId(currentUserId)
                    .build();
            propertyAgentRepository.save(propertyAgent);
            log.info("PropertyAgent link created for agent {} and property {}", currentUserId, propertyId);
        }

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
        UUID newLocationId = request.getLocationId();
        if (newLocationId == null && request.getLatitude() != null && request.getLongitude() != null) {
            newLocationId = resolveLocationId(request.getLatitude(), request.getLongitude());
        }

        if (newLocationId != null || request.getPropertyTypeCode() != null) {
            property.updateLocationAndType(
                    newLocationId,
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

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                property.updateStatus(PropertyStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid property status provided during update: {}", request.getStatus());
            }
        }

        if (request.getPriceRange() != null) {
            property.updatePriceRange(request.getPriceRange());
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

        PropertyDetailResponse response = propertyMapper.toDetailResponse(property, media, attributes, amenities);

        // Fetch active listings for this property
        List<ListingSummaryDTO> activeListings = listingRepository.findByPropertyId(propertyId).stream()
                .filter(l -> l.getStatus() == ListingStatus.PUBLISHED)
                .map(l -> ListingSummaryDTO.builder()
                        .listingId(l.getListingId())
                        .name(l.getName())
                        .slug(l.getSlug())
                        .price(l.getPrice())
                        .listingType(l.getListingType())
                        .thumbnailUrl(listingRepository.findThumbnailByListingId(l.getListingId()).orElse(null))
                        .agentName(l.getUser() != null ? l.getUser().getFullName() : null)
                        .build())
                .collect(Collectors.toList());

        response.setActiveListings(activeListings);
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertySummaryResponse> getMyProperties(
            PropertySearchCriteria criteria,
            Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = getCurrentUserId();

        boolean isAgent = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));

        String keyword = criteria != null ? criteria.getKeyword() : null;
        PropertyStatus status = criteria != null ? criteria.getStatus() : null;
        Page<Property> propertiesPage;

        if (isAgent) {
            log.info("Getting properties for agent: {} with criteria: {}", userId, criteria);
            propertiesPage = propertyRepository.findByAgentIdAndCriteria(userId, keyword, status, pageable);
        } else {
            log.info("Getting properties for owner: {} with criteria: {}", userId, criteria);
            propertiesPage = propertyRepository.findByOwnerIdAndCriteria(userId, keyword, status, pageable);
        }

        List<PropertySummaryResponse> content = propertiesPage.getContent().stream().map(property -> {
            UUID propId = property.getPropertyId();

            List<PropertyMedia> media = propertyMediaRepository.findByPropertyId(propId);

            List<PropertyAttributeValue> attributes =
                    propertyAttributeValueRepository.findByPropertyIdWithAttribute(propId);

            List<PropertyAmenity> amenities =
                    propertyAmenityRepository.findByPropertyIdWithAmenity(propId);

            return propertyMapper.toSummaryResponse(property, media, attributes, amenities);
        }).collect(Collectors.toList());

        return PageResponse.<PropertySummaryResponse>builder()
                .content(content)
                .page(propertiesPage.getNumber())
                .size(propertiesPage.getSize())
                .totalElements(propertiesPage.getTotalElements())
                .totalPages(propertiesPage.getTotalPages())
                .first(propertiesPage.isFirst())
                .last(propertiesPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PropertyAttributeDTO> getAttributesWithRanges() {
        log.info("Getting all searchable attributes with their ranges");
        
        return propertyAttributeRepository.findAllSearchable().stream().map(attr -> {
            var ranges = propertyAttributeRangeRepository
                    .findByPropertyAttributeId(attr.getPropertyAttributeId())
                    .stream()
                    .map(range -> PropertyAttributeRangeDTO.builder()
                            .propertyAttributeRangeId(range.getPropertyAttributeRangeId())
                            .label(range.getLabel())
                            .minValue(range.getMinValue())
                            .maxValue(range.getMaxValue())
                            .displayOrder(range.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());

            return PropertyAttributeDTO.builder()
                    .attributeId(attr.getPropertyAttributeId())
                    .attributeCode(attr.getCode())
                    .attributeName(attr.getName())
                    .dataType(attr.getDataType().name())
                    .icon(attr.getIcon())
                    .unit(attr.getUnit())
                    .ranges(ranges)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropertyAttributeDTO> getAttributesByPropertyType(String propertyTypeCode) {
        log.info("Getting attributes for property type code: {}", propertyTypeCode);

        var propertyType = propertyTypeRepository.findByCode(propertyTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Property type not found: " + propertyTypeCode));

        return propertyTypeAttributeRepository.findByPropertyTypeId(propertyType.getPropertyTypeId()).stream()
                .map(pta -> {
                    var attr = pta.getPropertyAttribute();
                    var ranges = propertyAttributeRangeRepository
                            .findByPropertyAttributeId(attr.getPropertyAttributeId())
                            .stream()
                            .map(range -> PropertyAttributeRangeDTO.builder()
                                    .propertyAttributeRangeId(range.getPropertyAttributeRangeId())
                                    .label(range.getLabel())
                                    .minValue(range.getMinValue())
                                    .maxValue(range.getMaxValue())
                                    .displayOrder(range.getDisplayOrder())
                                    .build())
                            .collect(Collectors.toList());

                    return PropertyAttributeDTO.builder()
                            .attributeId(attr.getPropertyAttributeId())
                            .attributeCode(attr.getCode())
                            .attributeName(attr.getName())
                            .dataType(attr.getDataType().name())
                            .icon(attr.getIcon())
                            .unit(attr.getUnit())
                            .ranges(ranges)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private UUID resolveLocationId(java.math.BigDecimal lat, java.math.BigDecimal lng) {
        if (lat == null || lng == null) {
            return null;
        }
        log.info("Resolving location for coordinates: [{}, {}]", lat, lng);
        var locations = locationRepository.findContainingLocations(lat, lng);
        if (locations.isEmpty()) {
            log.warn("No location found for coordinates: [{}, {}]", lat, lng);
            return null;
        }
        // Return the first one (already sorted by WARD -> DISTRICT -> CITY)
        UUID resolvedId = locations.getFirst().getLocationId();
        log.info("Resolved location ID: {} ({})", resolvedId, locations.getFirst().getName());
        return resolvedId;
    }

    /**
     * Returns a paginated feed of AVAILABLE properties for an agent to browse and submit proposals.
     *
     * <p>Excludes properties the agent is already assigned to.
     * Marks each item with {@code has_active_proposal = true} when the agent already
     * has a DRAFT or ACTIVE proposal for that property.
     *
     * @param agentId  the authenticated agent's user ID
     * @param criteria optional filter (keyword, propertyTypeId, locationId)
     * @param pageable pagination parameters
     * @return paginated feed of property items
     */
    @Transactional(readOnly = true)
    public PageResponse<PropertyFeedItemResponse> getPropertyFeed(
            UUID agentId,
            PropertyFeedCriteria criteria,
            org.springframework.data.domain.Pageable pageable) {

        log.info("Getting property feed for agent: {}, criteria: {}", agentId, criteria);

        String keyword = criteria != null ? criteria.getKeyword() : null;
        UUID propertyTypeId = criteria != null ? criteria.getPropertyTypeId() : null;
        UUID locationId = criteria != null ? criteria.getLocationId() : null;

        org.springframework.data.domain.Page<Property> page = propertyRepository.findPropertyFeed(
                agentId, keyword, propertyTypeId, locationId, pageable);

        // Batch-fetch property IDs where this agent already has an active proposal
        java.util.Set<UUID> proposalPropertyIds = agentProposalRepository.findActiveProposalPropertyIds(agentId);

        Set<UUID> ownerIds = page.getContent().stream()
                .map(Property::getOwnerId)
                .collect(Collectors.toSet());
        Map<UUID, User> ownersById = userRepository.findAllByIdIn(ownerIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
        Map<UUID, SettingPreference> prefsByUserId = settingPreferenceRepository.findByUserIdIn(ownerIds).stream()
                .collect(Collectors.toMap(SettingPreference::getUserId, Function.identity(), (a, b) -> a));

        // Get price filter criteria
        java.math.BigDecimal minRentPrice = criteria != null ? criteria.getMinRentPrice() : null;
        java.math.BigDecimal maxRentPrice = criteria != null ? criteria.getMaxRentPrice() : null;
        java.math.BigDecimal minBuyPrice = criteria != null ? criteria.getMinBuyPrice() : null;
        java.math.BigDecimal maxBuyPrice = criteria != null ? criteria.getMaxBuyPrice() : null;

        List<PropertyFeedItemResponse> content = page.getContent().stream()
                .filter(property -> filterByPrice(property, minRentPrice, maxRentPrice, minBuyPrice, maxBuyPrice))
                .map(property -> {
                    UUID propId = property.getPropertyId();

                    List<PropertyMedia> media = propertyMediaRepository.findByPropertyId(propId);
                    List<PropertyAttributeValue> attributes =
                            propertyAttributeValueRepository.findByPropertyIdWithAttribute(propId);
                    List<PropertyAmenity> amenities =
                            propertyAmenityRepository.findByPropertyIdWithAmenity(propId);

                    boolean hasActiveProposal = proposalPropertyIds.contains(propId);

                    PropertyFeedItemResponse item = propertyMapper.toFeedItemResponse(
                            property, media, attributes, amenities, hasActiveProposal);

                    User owner = ownersById.get(property.getOwnerId());
                    if (owner != null) {
                        applyOwnerContactForAgentView(item, owner, prefsByUserId.get(property.getOwnerId()));
                    }

                    return item;
                }).collect(Collectors.toList());

        log.info("Property feed retrieved: {} items for agent: {}", content.size(), agentId);

        return PageResponse.<PropertyFeedItemResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private boolean filterByPrice(Property property,
                                   java.math.BigDecimal minRentPrice, java.math.BigDecimal maxRentPrice,
                                   java.math.BigDecimal minBuyPrice, java.math.BigDecimal maxBuyPrice) {
        if (minRentPrice == null && maxRentPrice == null && minBuyPrice == null && maxBuyPrice == null) {
            return true; // No price filter, include all
        }

        var priceRange = property.getPriceRange();
        if (priceRange == null) {
            return true; // No price range set, include
        }

        // Check rent price
        if (minRentPrice != null || maxRentPrice != null) {
            var rent = priceRange.getRent();
            if (rent != null && rent.getMin() != null) {
                if (minRentPrice != null && rent.getMin().compareTo(minRentPrice) < 0) {
                    return false;
                }
                if (maxRentPrice != null && rent.getMax() != null && rent.getMax().compareTo(maxRentPrice) > 0) {
                    return false;
                }
            }
        }

        // Check buy price
        if (minBuyPrice != null || maxBuyPrice != null) {
            var buy = priceRange.getBuy();
            if (buy != null && buy.getMin() != null) {
                if (minBuyPrice != null && buy.getMin().compareTo(minBuyPrice) < 0) {
                    return false;
                }
                return maxBuyPrice == null || buy.getMax() == null || buy.getMax().compareTo(maxBuyPrice) <= 0;
            }
        }

        return true;
    }

    @Transactional(readOnly = true)
    public List<com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO> getPropertyTypes() {
        log.info("Getting all active property types");
        return propertyTypeRepository.findAllActive().stream()
                .map(pt -> {
                    var cat = pt.getPropertyCategory();
                    return com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO.builder()
                            .propertyTypeId(pt.getPropertyTypeId())
                            .propertyTypeName(pt.getName())
                            .propertyTypeCode(pt.getCode())
                            .propertyCategoryId(cat != null ? cat.getPropertyCategoryId() : null)
                            .propertyCategoryName(cat != null ? cat.getName() : null)
                            .propertyCategoryCode(cat != null ? cat.getCode() : null)
                            .build();
                })
                .collect(Collectors.toList());
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

    @Transactional
    public PropertyDetailResponse verifyPropertyByAgent(UUID propertyId) {
        UUID agentId = getCurrentUserId();
        log.info("Agent {} verifying property {}", agentId, propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (!propertyAgentRepository.existsByPropertyIdAndAgentId(propertyId, agentId)) {
            throw new IllegalArgumentException("User is not authorized to verify this property");
        }

        property.verifyByAgent();
        propertyRepository.save(property);

        log.info("Property {} verified by agent {}", propertyId, agentId);

        return getPropertyDetails(propertyId);
    }

    @Transactional
    public PropertyDetailResponse updatePropertyStatus(UUID propertyId, String status) {
        UUID currentUserId = getCurrentUserId();
        log.info("User {} updating status of property {} to {}", currentUserId, propertyId, status);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (!property.getOwnerId().equals(currentUserId)) {
            throw new IllegalArgumentException("Only the property owner can update the status");
        }

        Set<PropertyStatus> systemStatuses = Set.of(
                PropertyStatus.PENDING, PropertyStatus.VERIFIED, PropertyStatus.REJECTED);

        if (systemStatuses.contains(property.getStatus())) {
            throw new IllegalArgumentException(
                    "Cannot manually change status of a property with system-managed status: "
                    + property.getStatus());
        }

        PropertyStatus newStatus;
        try {
            newStatus = PropertyStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid property status: " + status);
        }

        if (systemStatuses.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Cannot manually set a property to system-managed status: " + newStatus);
        }

        property.updateStatus(newStatus);
        propertyRepository.save(property);
        log.info("Property {} status updated to {} by user {}", propertyId, status, currentUserId);

        return getPropertyDetails(propertyId);
    }

    @Transactional
    public void softDeleteProperty(UUID propertyId) {
        UUID currentUserId = getCurrentUserId();
        log.info("User {} soft-deleting property {}", currentUserId, propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (!property.getOwnerId().equals(currentUserId)) {
            throw new IllegalArgumentException("Only the property owner can delete this property");
        }

        property.markAsDeleted();
        propertyRepository.save(property);
        log.info("Property {} soft-deleted by user {}", propertyId, currentUserId);
    }

    @Transactional
    public PropertyDetailResponse assignAgentToProperty(UUID propertyId) {
        UUID agentId = getCurrentUserId();
        log.info("Assigning agent {} to property {}", agentId, propertyId);

        // Create link if not already exists
        if (!propertyAgentRepository.existsByPropertyIdAndAgentId(propertyId, agentId)) {
            PropertyAgent propertyAgent = PropertyAgent.builder()
                    .propertyId(propertyId)
                    .agentId(agentId)
                    .build();
            propertyAgentRepository.save(propertyAgent);
            log.info("PropertyAgent link created for agent {} and property {}", agentId, propertyId);
        } else {
            log.info("Agent {} already linked to property {}", agentId, propertyId);
        }

        return getPropertyDetails(propertyId);
    }

    @Transactional(readOnly = true)
    public List<PropertySummaryResponse> searchProperties(String address, java.math.BigDecimal nLat,
                                                          java.math.BigDecimal sLat, java.math.BigDecimal eLng,
                                                          java.math.BigDecimal wLng) {
        log.info("Searching properties with address: {}, bbox: [{}, {}, {}, {}]",
                address, nLat, sLat, eLng, wLng);

        List<Property> properties;
        if (nLat != null && sLat != null && eLng != null && wLng != null) {
            properties = propertyRepository.findInLocationRange(nLat, sLat, eLng, wLng);
        } else if (address != null && !address.isBlank()) {
            properties = propertyRepository.searchByAddress(address);
        } else {
            return new ArrayList<>();
        }

        return properties.stream().map(property -> {
            String thumbnailUrl = propertyMediaRepository.findByPropertyId(property.getPropertyId())
                    .stream()
                    .filter(pm -> Boolean.TRUE.equals(pm.getIsPrimary()))
                    .findFirst()
                    .map(PropertyMedia::getThumbnailUrl)
                    .orElse(null);

            PropertySummaryResponse response = propertyMapper.toSummaryResponse(property, thumbnailUrl);

            userRepository.findById(property.getOwnerId()).ifPresent(owner -> {
                response.setOwnerName(owner.getFullName());
                response.setOwnerPhone(owner.getPhone());
            });

            return response;
        }).collect(Collectors.toList());
    }
}
