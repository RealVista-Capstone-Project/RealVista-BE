package com.sep.realvista.unit.application.property;

import com.sep.realvista.application.engagement.service.EngagementApplicationService;
import com.sep.realvista.application.property.dto.CreatePropertyRequest;
import com.sep.realvista.application.property.dto.PropertyDetailResponse;
import com.sep.realvista.application.property.mapper.PropertyMapper;
import com.sep.realvista.application.property.service.PropertyApplicationService;
import com.sep.realvista.domain.agent.PropertyAgentRepository;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRangeRepository;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRepository;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.location.LocationRepository;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import com.sep.realvista.domain.property.repository.PropertyTypeAttributeRepository;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.infrastructure.persistence.property.amenity.AmenityJpaRepository;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyApplicationService Unit Tests")
class PropertyApplicationServiceTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private PropertyMediaRepository propertyMediaRepository;
    @Mock private PropertyAmenityRepository propertyAmenityRepository;
    @Mock private AmenityJpaRepository amenityJpaRepository;
    @Mock private PropertyAttributeValueRepository propertyAttributeValueRepository;
    @Mock private PropertyTypeRepository propertyTypeRepository;
    @Mock private PropertyAttributeRepository propertyAttributeRepository;
    @Mock private PropertyAgentRepository propertyAgentRepository;
    @Mock private UserRepository userRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private PropertyAttributeRangeRepository propertyAttributeRangeRepository;
    @Mock private PropertyTypeAttributeRepository propertyTypeAttributeRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private PropertyMapper propertyMapper;
    @Mock private EntityManager entityManager;
    @Mock private AgentProposalRepository agentProposalRepository;
    @Mock private SettingPreferenceRepository settingPreferenceRepository;
    @Mock private EngagementApplicationService engagementApplicationService;

    @InjectMocks
    private PropertyApplicationService propertyApplicationService;

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("createProperty should auto-create engagement when agent creates for owner")
    void createPropertyShouldAutoCreateEngagementWhenAgentCreatesForOwner() {
        UUID agentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID propertyTypeId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        setupCurrentUser(agentId);

        CreatePropertyRequest request = CreatePropertyRequest.builder()
                .ownerId(ownerId)
                .locationId(locationId)
                .propertyTypeCode("APARTMENT")
                .streetAddress("123 Test Street")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.ONE)
                .build();

        Property savedProperty = Property.builder()
                .propertyId(propertyId)
                .ownerId(ownerId)
                .locationId(locationId)
                .propertyTypeId(propertyTypeId)
                .streetAddress("123 Test Street")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.ONE)
                .status(PropertyStatus.PENDING)
                .slug("slug")
                .build();

        PropertyDetailResponse detailResponse = PropertyDetailResponse.builder()
                .propertyId(propertyId)
                .ownerId(ownerId)
                .build();

        when(propertyTypeRepository.findByCode("APARTMENT"))
                .thenReturn(Optional.of(com.sep.realvista.domain.property.PropertyType.builder()
                        .propertyTypeId(propertyTypeId)
                        .code("APARTMENT")
                        .build()));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(savedProperty));
        when(propertyMediaRepository.findByPropertyId(propertyId)).thenReturn(List.of());
        when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId)).thenReturn(List.of());
        when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId)).thenReturn(List.of());
        when(listingRepository.findByPropertyId(propertyId)).thenReturn(List.of());
        when(propertyMapper.toDetailResponse(any(), any(), any(), any())).thenReturn(detailResponse);

        PropertyDetailResponse result = propertyApplicationService.createProperty(request);

        assertThat(result.getPropertyId()).isEqualTo(propertyId);
        verify(engagementApplicationService).createAgentCreatedPropertyLink(propertyId, ownerId, agentId);
    }

    @Test
    @DisplayName("createProperty should not auto-create engagement for owner self-create flow")
    void createPropertyShouldNotAutoCreateEngagementForOwnerSelfCreateFlow() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID propertyTypeId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        setupCurrentUser(ownerId);

        CreatePropertyRequest request = CreatePropertyRequest.builder()
                .locationId(locationId)
                .propertyTypeCode("APARTMENT")
                .streetAddress("123 Test Street")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.ONE)
                .build();

        Property savedProperty = Property.builder()
                .propertyId(propertyId)
                .ownerId(ownerId)
                .locationId(locationId)
                .propertyTypeId(propertyTypeId)
                .streetAddress("123 Test Street")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.ONE)
                .status(PropertyStatus.DRAFT)
                .slug("slug")
                .build();

        when(propertyTypeRepository.findByCode("APARTMENT"))
                .thenReturn(Optional.of(com.sep.realvista.domain.property.PropertyType.builder()
                        .propertyTypeId(propertyTypeId)
                        .code("APARTMENT")
                        .build()));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(savedProperty));
        when(propertyMediaRepository.findByPropertyId(propertyId)).thenReturn(List.of());
        when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId)).thenReturn(List.of());
        when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId)).thenReturn(List.of());
        when(listingRepository.findByPropertyId(propertyId)).thenReturn(List.of());
        when(propertyMapper.toDetailResponse(any(), any(), any(), any()))
                .thenReturn(PropertyDetailResponse.builder().propertyId(propertyId).ownerId(ownerId).build());

        propertyApplicationService.createProperty(request);

        verify(engagementApplicationService, never()).createAgentCreatedPropertyLink(any(), any(), any());
    }

    private static void setupCurrentUser(UUID userId) {
        SecurityUserDetails principal = SecurityUserDetails.builder()
                .userId(userId)
                .username("test@example.com")
                .password("encoded")
                .authorities(List.of())
                .active(true)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
