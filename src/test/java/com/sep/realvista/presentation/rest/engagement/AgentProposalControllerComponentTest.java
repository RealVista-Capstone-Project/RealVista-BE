package com.sep.realvista.presentation.rest.engagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.AgentProposalDto;
import com.sep.realvista.application.engagement.dto.ApplyAgentProposalRequest;
import com.sep.realvista.application.engagement.service.AgentProposalApplicationService;
import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AgentProposalControllerComponentTest {

    private MockMvc mockMvc;

    @Mock
    private AgentProposalApplicationService agentProposalApplicationService;

    @InjectMocks
    private AgentProposalController agentProposalController;

    private UUID userId;
    private UUID proposalId;
    private SecurityUserDetails mockUserDetails;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        userId = UUID.randomUUID();
        proposalId = UUID.randomUUID();

        mockUserDetails = new SecurityUserDetails(
                userId,
                "agent@test.com",
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
                .standaloneSetup(agentProposalController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
    }

    @Test
    public void testCreateProposal_WithValidRequest_ReturnsCreated() throws Exception {
        ApplyAgentProposalRequest request = new ApplyAgentProposalRequest();
        request.setTitle("My Proposal");
        request.setCommissionRate(BigDecimal.valueOf(2.5));
        request.setExperienceYears(3);
        request.setPitchContent("I will sell this quick!");

        AgentProposalDto responseDto = AgentProposalDto.builder()
                .agentProposalId(proposalId)
                .userId(userId)
                .status(AgentProposalStatus.ACTIVE)
                .commissionRate(BigDecimal.valueOf(2.5))
                .build();

        // Fix: Use createProposal instead of applyProposal
        when(agentProposalApplicationService.createProposal(eq(userId), any(ApplyAgentProposalRequest.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/agent-proposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Proposal template created successfully"))
                .andExpect(jsonPath("$.data.agentProposalId").value(proposalId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    public void testGetMyProposals_ReturnsPage() throws Exception {
        AgentProposalDto proposalDto = AgentProposalDto.builder()
                .agentProposalId(proposalId)
                .title("My Proposal")
                .build();

        PageResponse<AgentProposalDto> pageResponse = PageResponse.<AgentProposalDto>builder()
                .content(List.of(proposalDto))
                .totalElements(1L)
                .build();

        when(agentProposalApplicationService.getMyProposals(eq(userId), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/agent-proposals/my-proposals")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Proposal templates retrieved successfully"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].agentProposalId").value(proposalId.toString()));
    }

    @Test
    public void testArchiveProposal_ReturnsOk() throws Exception {
        // Fix: Mock archiveProposal
        doNothing().when(agentProposalApplicationService).archiveProposal(eq(proposalId), eq(userId));

        mockMvc.perform(delete("/api/v1/agent-proposals/" + proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Proposal template archived successfully"));
    }
}
