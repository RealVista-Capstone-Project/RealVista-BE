package com.sep.realvista.unit.application.property;

import com.sep.realvista.application.engagement.service.EngagementApplicationService;
import com.sep.realvista.application.property.dto.CreatePropertyRequest;
import com.sep.realvista.application.property.dto.PropertyDetailResponse;
import com.sep.realvista.application.property.dto.UpdatePropertyRequest;
import com.sep.realvista.application.property.mapper.PropertyMapper;
import com.sep.realvista.application.property.service.PropertyApplicationService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.agent.PropertyAgentRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
    @Mock private PropertyAttributeValueRepository propertyAttributeValueRepository;
    @Mock private PropertyTypeRepository propertyTypeRepository;
    @Mock private PropertyAgentRepository propertyAgentRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private PropertyMapper propertyMapper;
    @Mock private EngagementApplicationService engagementApplicationService;
    @Mock private EntityManager entityManager;
    @Mock private CacheManager cacheManager;
    @Mock private Cache listingCache;

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

    @Test
    @DisplayName("createProperty should store rented rent listing toggle")
    void createPropertyShouldStoreAllowRentListingWhenRentedToggle() {
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
                .allowRentListingWhenRented(true)
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
                .allowRentListingWhenRented(true)
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

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository, org.mockito.Mockito.atLeastOnce()).save(propertyCaptor.capture());
        assertThat(propertyCaptor.getAllValues().get(0).getAllowRentListingWhenRented()).isTrue();
    }

    @Test
    @DisplayName("updateProperty should draft active rent listings when rented toggle turns off")
    void updatePropertyWhenRentedToggleTurnsOffShouldDraftActiveRentListings() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID rentListingId = UUID.randomUUID();
        UUID pendingRentListingId = UUID.randomUUID();
        UUID saleListingId = UUID.randomUUID();
        setupCurrentUser(ownerId);

        Property rentedProperty = Property.builder()
                .propertyId(propertyId)
                .ownerId(ownerId)
                .status(PropertyStatus.RENTED)
                .allowRentListingWhenRented(true)
                .streetAddress("123 Test Street")
                .build();
        Listing publishedRentListing = Listing.builder()
                .listingId(rentListingId)
                .propertyId(propertyId)
                .userId(ownerId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .name("Published rent")
                .slug("published-rent")
                .price(BigDecimal.valueOf(1000))
                .build();
        Listing pendingRentListing = Listing.builder()
                .listingId(pendingRentListingId)
                .propertyId(propertyId)
                .userId(ownerId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PENDING)
                .name("Pending rent")
                .slug("pending-rent")
                .price(BigDecimal.valueOf(1100))
                .build();
        Listing saleListing = Listing.builder()
                .listingId(saleListingId)
                .propertyId(propertyId)
                .userId(ownerId)
                .listingType(ListingType.SALE)
                .status(ListingStatus.PUBLISHED)
                .name("Sale listing")
                .slug("sale-listing")
                .price(BigDecimal.valueOf(200000))
                .build();
        UpdatePropertyRequest request = UpdatePropertyRequest.builder()
                .allowRentListingWhenRented(false)
                .build();

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(rentedProperty));
        when(listingRepository.findByPropertyId(propertyId))
                .thenReturn(List.of(publishedRentListing, pendingRentListing, saleListing));
        when(cacheManager.getCache("listings")).thenReturn(listingCache);
        when(propertyMediaRepository.findByPropertyId(propertyId)).thenReturn(List.of());
        when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId)).thenReturn(List.of());
        when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId)).thenReturn(List.of());
        when(listingRepository.findThumbnailByListingId(saleListingId)).thenReturn(Optional.empty());
        when(propertyMapper.toDetailResponse(any(), any(), any(), any()))
                .thenReturn(PropertyDetailResponse.builder().propertyId(propertyId).ownerId(ownerId).build());

        propertyApplicationService.updateProperty(propertyId, request);

        assertThat(rentedProperty.getAllowRentListingWhenRented()).isFalse();
        assertThat(publishedRentListing.getStatus()).isEqualTo(ListingStatus.DRAFT);
        assertThat(pendingRentListing.getStatus()).isEqualTo(ListingStatus.DRAFT);
        assertThat(saleListing.getStatus()).isEqualTo(ListingStatus.PUBLISHED);
        verify(listingRepository).saveAll(any());
        verify(listingCache).evict(rentListingId);
        verify(listingCache).evict(pendingRentListingId);
        verify(listingCache, never()).evict(saleListingId);
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
