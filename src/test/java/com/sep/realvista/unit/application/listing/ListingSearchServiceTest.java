package com.sep.realvista.unit.application.listing;

import com.sep.realvista.application.listing.ListingSearchService;
import com.sep.realvista.application.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListingSearchService Unit Test")
class ListingSearchServiceTest {

    @Mock
    private ListingRepository listingRepository;
    
    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private ListingSearchService listingSearchService;

    private Listing sampleListing;

    @BeforeEach
    void setUp() {
        sampleListing = mock(Listing.class);
    }

    @Test
    @DisplayName("Should return page of search results successfully")
    void shouldReturnSearchResultsSuccessfully() {
        // Given
        UUID propertyTypeId = UUID.randomUUID();
        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .propertyType(propertyTypeId)
                .location("Quận 7")
                .price(List.of(new BigDecimal("500000000"), new BigDecimal("2000000000")))
                .area(List.of(new BigDecimal("50"), new BigDecimal("150")))
                .build();

        ListingSearchResponse responseDTO = ListingSearchResponse.builder()
                .location("Quận 7")
                .propertyType("Apartment")
                .price(new BigDecimal("1000000000"))
                .build();

        Page<Listing> listingPage = new PageImpl<>(Collections.singletonList(sampleListing));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(listingPage);
        when(listingMapper.toSearchResponse(any(Listing.class))).thenReturn(responseDTO);

        // When
        Page<ListingSearchResponse> results = listingSearchService.search(request, Pageable.unpaged());

        // Then
        assertThat(results.getContent()).hasSize(1);
        ListingSearchResponse response = results.getContent().get(0);
        assertThat(response.getLocation()).isEqualTo("Quận 7");
        assertThat(response.getPropertyType()).isEqualTo("Apartment");
        assertThat(response.getPrice()).isEqualByComparingTo("1000000000");
        
        verify(listingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(listingMapper, times(1)).toSearchResponse(any(Listing.class));
    }

    @Test
    @DisplayName("Should handle search with dynamic criteria")
    void shouldHandleSearchWithDynamicCriteria() {
        // Given
        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .dynamic(Collections.singletonMap("bedrooms", 2))
                .build();

        Page<Listing> listingPage = new PageImpl<>(Collections.singletonList(sampleListing));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(listingPage);
        when(listingMapper.toSearchResponse(any(Listing.class))).thenReturn(new ListingSearchResponse());

        // When
        Page<ListingSearchResponse> results = listingSearchService.search(request, Pageable.unpaged());

        // Then
        assertThat(results.getContent()).hasSize(1);
        verify(listingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
    @Test
    @DisplayName("Should handle search with listing type, bedrooms and bathrooms")
    void shouldHandleSearchWithListingTypeAndRoomCount() {
        // Given
        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .listingType("SALE")
                .bedrooms(3)
                .bathrooms(2)
                .build();

        Page<Listing> listingPage = new PageImpl<>(Collections.singletonList(sampleListing));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(listingPage);
        when(listingMapper.toSearchResponse(any(Listing.class))).thenReturn(new ListingSearchResponse());

        // When
        Page<ListingSearchResponse> results = listingSearchService.search(request, Pageable.unpaged());

        // Then
        assertThat(results.getContent()).hasSize(1);
        verify(listingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
