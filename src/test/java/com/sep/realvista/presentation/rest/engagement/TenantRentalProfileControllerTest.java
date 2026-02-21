package com.sep.realvista.presentation.rest.engagement;

import com.sep.realvista.application.listing.dto.TenantRentalProfileDto;
import com.sep.realvista.application.service.engagement.TenantRentalProfileService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenantRentalProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TenantRentalProfileService tenantRentalProfileService;

    @InjectMocks
    private TenantRentalProfileController tenantRentalProfileController;

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
                .standaloneSetup(tenantRentalProfileController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
    }

    @Test
    void getMyProfiles_Success() throws Exception {
        UUID profileId = UUID.randomUUID();
        TenantRentalProfileDto profile = TenantRentalProfileDto.builder()
                .profileId(profileId)
                .title("Default Profile")
                .build();

        when(tenantRentalProfileService.getMyProfiles(userId)).thenReturn(List.of(profile));

        mockMvc.perform(get("/api/v1/tenant-rental-profiles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].profileId").value(profileId.toString()))
                .andExpect(jsonPath("$.data[0].title").value("Default Profile"));
    }
}
