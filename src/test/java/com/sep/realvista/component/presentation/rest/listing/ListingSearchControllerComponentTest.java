package com.sep.realvista.component.presentation.rest.listing;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.service.ListingSearchService;
import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.sep.realvista.presentation.rest.listing.ListingSearchController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ListingSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ListingSearchController Component Tests")
class ListingSearchControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListingSearchService listingSearchService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should call service with correct parameters when searching")
    void search_withParams_shouldCallService() throws Exception {
        // Arrange
        Page<ListingSearchResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(listingSearchService.search(any(ListingSearchCriteria.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        mockMvc.perform(get("/api/v1/listings/search")
                .param("listingType", "RENT")
                .param("minPrice", "1000")
                .param("attr_hasPool", "true")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        // Assert
        verify(listingSearchService).search(
                argThat(criteria -> 
                    "RENT".equals(criteria.getListingType()) &&
                    BigDecimal.valueOf(1000).equals(criteria.getMinPrice()) &&
                    criteria.getDynamicAttributes() != null &&
                    "true".equals(criteria.getDynamicAttributes().get("hasPool"))
                ),
                any(Pageable.class)
        );
    }
}
