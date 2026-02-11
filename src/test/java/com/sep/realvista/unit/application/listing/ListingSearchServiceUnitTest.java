package com.sep.realvista.unit.application.listing;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.application.listing.service.ListingSearchService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListingSearchService Unit Tests")
class ListingSearchServiceUnitTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private ListingSearchService listingSearchService;

    @Test
    @DisplayName("Should invoke repository findAll with specification")
    void search_shouldInvokeRepositoryFindAll() {
        // Arrange
        UUID listingId = UUID.randomUUID();
        Listing listing = Listing.builder()
                .listingId(listingId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .price(BigDecimal.valueOf(1000))
                .build();
        
        Page<Listing> listingPage = new PageImpl<>(List.of(listing));
        
        ListingSearchResponse response = ListingSearchResponse.builder()
                .listingId(listingId)
                .price(BigDecimal.valueOf(1000))
                .build();

        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(listingPage);
        when(listingMapper.toSearchResponse(any(Listing.class))).thenReturn(response);
        when(listingRepository.findThumbnailByListingId(any(UUID.class))).thenReturn(Optional.empty());

        // Act
        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .minPrice(BigDecimal.valueOf(500))
                .maxPrice(BigDecimal.valueOf(1500))
                .dynamicAttributes(Collections.emptyMap())
                .build();

        // Act
        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(listingRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
