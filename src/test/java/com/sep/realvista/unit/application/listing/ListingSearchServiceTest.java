package com.sep.realvista.unit.application.listing;

import com.sep.realvista.application.listing.ListingSearchService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.presentation.rest.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.presentation.rest.listing.dto.ListingSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @InjectMocks
    private ListingSearchService listingSearchService;

    private Listing sampleListing;
    private Property sampleProperty;
    private PropertyType samplePropertyType;
    private Location sampleLocation;

    @BeforeEach
    void setUp() {
        sampleLocation = mock(Location.class);
        when(sampleLocation.getName()).thenReturn("Quận 7");

        samplePropertyType = mock(PropertyType.class);
        when(samplePropertyType.getName()).thenReturn("Apartment");

        sampleProperty = mock(Property.class);
        when(sampleProperty.getStreetAddress()).thenReturn("123 Street");
        when(sampleProperty.getPropertyType()).thenReturn(samplePropertyType);
        when(sampleProperty.getLocation()).thenReturn(sampleLocation);
        when(sampleProperty.getUsableSizeM2()).thenReturn(new BigDecimal("100.0"));

        sampleListing = mock(Listing.class);
        when(sampleListing.getListingId()).thenReturn(UUID.randomUUID());
        when(sampleListing.getProperty()).thenReturn(sampleProperty);
        when(sampleListing.getPrice()).thenReturn(new BigDecimal("1000000000"));
    }

    @Test
    @DisplayName("Should return list of search results successfully")
    void shouldReturnSearchResultsSuccessfully() {
        // Given
        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .propertyType("apartment")
                .location("Quận 7")
                .price(List.of(new BigDecimal("500000000"), new BigDecimal("2000000000")))
                .area(List.of(new BigDecimal("50"), new BigDecimal("150")))
                .build();

        when(listingRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(sampleListing));

        // When
        List<ListingSearchResponse> results = listingSearchService.search(request);

        // Then
        assertThat(results).hasSize(1);
        ListingSearchResponse response = results.get(0);
        assertThat(response.getLocation()).isEqualTo("Quận 7");
        assertThat(response.getPropertyType()).isEqualTo("Apartment");
        assertThat(response.getPrice()).isEqualByComparingTo("1000000000");
        
        verify(listingRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should handle search with dynamic criteria")
    void shouldHandleSearchWithDynamicCriteria() {
        // Given
        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .dynamic(Collections.singletonMap("bedrooms", 2))
                .build();

        when(listingRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(sampleListing));

        // When
        List<ListingSearchResponse> results = listingSearchService.search(request);

        // Then
        assertThat(results).hasSize(1);
        verify(listingRepository, times(1)).findAll(any(Specification.class));
    }
}
