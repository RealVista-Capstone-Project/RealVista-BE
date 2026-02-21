package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.listing.dto.TenantApplicationDto;
import com.sep.realvista.application.service.engagement.TenantApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenantApplicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TenantApplicationService tenantApplicationService;

    @InjectMocks
    private TenantApplicationController tenantApplicationController;

    private SecurityUserDetails mockUserDetails;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockUserDetails = new SecurityUserDetails(
                userId,
                "user@test.com",
                "password123",
                Collections.emptyList(),
                true
        );

        HandlerMethodArgumentResolver authenticationPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(SecurityUserDetails.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return mockUserDetails;
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(tenantApplicationController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
    }

    @Test
    void getMyApplications_Success() throws Exception {
        UUID applicationId1 = UUID.randomUUID();
        UUID applicationId2 = UUID.randomUUID();

        TenantApplicationDto app1 = TenantApplicationDto.builder()
                .tenantApplicationId(applicationId1)
                .title("Cozy Apartment")
                .propertyAddress("123 Main St")
                .build();

        TenantApplicationDto app2 = TenantApplicationDto.builder()
                .tenantApplicationId(applicationId2)
                .title("Luxury Villa")
                .propertyAddress("456 Oak St")
                .build();

        List<TenantApplicationDto> expectedApplications = List.of(app1, app2);

        when(tenantApplicationService.getMyApplications(userId))
                .thenReturn(expectedApplications);

        mockMvc.perform(get("/api/v1/tenant-applications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].tenantApplicationId").value(applicationId1.toString()))
                .andExpect(jsonPath("$.data[0].title").value("Cozy Apartment"))
                .andExpect(jsonPath("$.data[0].propertyAddress").value("123 Main St"))
                .andExpect(jsonPath("$.data[1].tenantApplicationId").value(applicationId2.toString()))
                .andExpect(jsonPath("$.data[1].title").value("Luxury Villa"))
                .andExpect(jsonPath("$.data[1].propertyAddress").value("456 Oak St"));

        verify(tenantApplicationService, times(1)).getMyApplications(userId);
    }

    @Test
    void deleteApplication_Success() throws Exception {
        UUID applicationId = UUID.randomUUID();

        doNothing().when(tenantApplicationService).softDeleteApplication(applicationId, userId);

        mockMvc.perform(delete("/api/v1/tenant-applications/{id}", applicationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(tenantApplicationService, times(1)).softDeleteApplication(applicationId, userId);
    }

    @Test
    void submitApplication_Success() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        TenantApplicationDto expectedDto = TenantApplicationDto.builder()
                .title("Luxury Apartment - My Profile")
                .build();

        when(tenantApplicationService.submitApplication(listingId, profileId, userId))
                .thenReturn(expectedDto);

        mockMvc.perform(post("/api/v1/tenant-applications/listings/{listingId}/profiles/{profileId}", listingId, profileId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Luxury Apartment - My Profile"));

        verify(tenantApplicationService, times(1)).submitApplication(listingId, profileId, userId);
    }
}
