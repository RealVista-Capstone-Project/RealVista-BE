package com.sep.realvista.component.presentation.rest.engagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.CancelEngagementRequest;
import com.sep.realvista.application.engagement.dto.SubmitAgentProposalRequest;
import com.sep.realvista.application.engagement.dto.CreateReviewRequest;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.dto.ReviewResponse;
import com.sep.realvista.application.engagement.service.AgentReviewApplicationService;
import com.sep.realvista.application.engagement.service.EngagementApplicationService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.engagement.EngagementController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for EngagementController.
 *
 * Tests the web layer including request mapping, validation,
 * serialization, and exception handling.
 */
@WebMvcTest(EngagementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("EngagementController Component Tests (Web Layer)")
class EngagementControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EngagementApplicationService engagementApplicationService;

    @MockitoBean
    private AgentReviewApplicationService agentReviewApplicationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // Common test data
    private UUID ownerId;
    private UUID engagementId;
    private UUID agentUserId;
    private SecurityUserDetails mockSecurityUser;
    private HiredAgentResponse hiredAgentResponse;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        engagementId = UUID.randomUUID();
        agentUserId = UUID.randomUUID();

        mockSecurityUser = new SecurityUserDetails(
                ownerId,
                "owner@test.com",
                "hashedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_OWNER")),
                true
        );

        // Set SecurityContextHolder directly so @AuthenticationPrincipal resolves
        // when addFilters = false (security filter chain is bypassed)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        mockSecurityUser,
                        null,
                        mockSecurityUser.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        hiredAgentResponse = HiredAgentResponse.builder()
                .engagementId(engagementId)
                .agentUserId(agentUserId)
                .agentFullName("John Agent")
                .agentEmail("john@agent.com")
                .agentPhone("1234567890")
                .agentRating(new BigDecimal("4.5"))
                .engagementType("AGENT_PROPOSAL")
                .status("ACCEPTED")
                .hiredAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .hasReview(false)
                .build();

        reviewResponse = ReviewResponse.builder()
                .reviewId(UUID.randomUUID())
                .engagementId(engagementId)
                .agentUserId(agentUserId)
                .reviewerId(ownerId)
                .rating(new BigDecimal("4.5"))
                .comment("Great agent")
                .createdAt(LocalDateTime.of(2026, 3, 20, 14, 30))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/v1/engagements/{id}")
    class GetEngagementById {

        @Test
        @DisplayName("Should return 200 with engagement detail")
        void shouldReturnEngagementDetail() throws Exception {
            // Arrange
            when(engagementApplicationService.getEngagementById(eq(engagementId), any()))
                    .thenReturn(hiredAgentResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/engagements/{id}", engagementId)

                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.engagement_id").value(engagementId.toString()))
                    .andExpect(jsonPath("$.data.agent_user_id").value(agentUserId.toString()))
                    .andExpect(jsonPath("$.data.agent_full_name").value("John Agent"))
                    .andExpect(jsonPath("$.data.agent_email").value("john@agent.com"))
                    .andExpect(jsonPath("$.data.agent_rating").value(4.5))
                    .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.data.has_review").value(false));
        }

        @Test
        @DisplayName("Should return 404 when engagement not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Arrange
            when(engagementApplicationService.getEngagementById(eq(engagementId), any()))
                    .thenThrow(new ResourceNotFoundException("Engagement", engagementId));

            // Act & Assert
            mockMvc.perform(get("/api/v1/engagements/{id}", engagementId)

                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 409 when user is not the owner")
        void shouldReturn409WhenNotOwner() throws Exception {
            // Arrange
            when(engagementApplicationService.getEngagementById(eq(engagementId), any()))
                    .thenThrow(new BusinessConflictException(
                            "You are not authorized to manage this engagement",
                            "ENGAGEMENT_NOT_OWNED"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/engagements/{id}", engagementId)

                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("ENGAGEMENT_NOT_OWNED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/engagements/hired-agents")
    class GetHiredAgents {

        @Test
        @DisplayName("Should return 200 with paginated hired agents")
        void shouldReturnPaginatedHiredAgents() throws Exception {
            // Arrange
            PageResponse<HiredAgentResponse> pageResponse = PageResponse.<HiredAgentResponse>builder()
                    .content(List.of(hiredAgentResponse))
                    .page(0)
                    .size(10)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(engagementApplicationService.getHiredAgents(any(), any(), any(), eq(0), eq(10)))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/engagements/hired-agents")

                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].engagement_id").value(engagementId.toString()))
                    .andExpect(jsonPath("$.data.content[0].agent_full_name").value("John Agent"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.total_elements").value(1))
                    .andExpect(jsonPath("$.data.total_pages").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        @DisplayName("Should pass status filter to service")
        void shouldPassStatusFilter() throws Exception {
            // Arrange
            PageResponse<HiredAgentResponse> pageResponse = PageResponse.<HiredAgentResponse>builder()
                    .content(List.of())
                    .page(0)
                    .size(10)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(engagementApplicationService.getHiredAgents(any(), eq("FINISHED"), any(), eq(0), eq(10)))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/engagements/hired-agents")

                            .param("status", "FINISHED")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty());

            verify(engagementApplicationService).getHiredAgents(any(), eq("FINISHED"), any(), eq(0), eq(10));
        }

        @Test
        @DisplayName("Should pass search query to service")
        void shouldPassSearchQuery() throws Exception {
            // Arrange
            PageResponse<HiredAgentResponse> pageResponse = PageResponse.<HiredAgentResponse>builder()
                    .content(List.of())
                    .page(0)
                    .size(10)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(engagementApplicationService.getHiredAgents(any(), any(), eq("John"), eq(0), eq(10)))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/engagements/hired-agents")

                            .param("search", "John")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(engagementApplicationService).getHiredAgents(any(), any(), eq("John"), eq(0), eq(10));
        }

        @Test
        @DisplayName("Should use default page and size when not specified")
        void shouldUseDefaultPagination() throws Exception {
            // Arrange
            PageResponse<HiredAgentResponse> pageResponse = PageResponse.<HiredAgentResponse>builder()
                    .content(List.of())
                    .page(0)
                    .size(10)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(engagementApplicationService.getHiredAgents(any(), any(), any(), eq(0), eq(10)))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/engagements/hired-agents")

                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(engagementApplicationService).getHiredAgents(any(), any(), any(), eq(0), eq(10));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/engagements/{id}/finish")
    class FinishEngagement {

        @Test
        @DisplayName("Should return 200 on successful finish")
        void shouldFinishSuccessfully() throws Exception {
            // Arrange
            doNothing().when(engagementApplicationService)
                    .finishEngagement(eq(engagementId), any());

            // Act & Assert
            mockMvc.perform(put("/api/v1/engagements/{id}/finish", engagementId)

                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Engagement finished successfully"));
        }

        @Test
        @DisplayName("Should return 404 when engagement not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Engagement", engagementId))
                    .when(engagementApplicationService)
                    .finishEngagement(eq(engagementId), any());

            // Act & Assert
            mockMvc.perform(put("/api/v1/engagements/{id}/finish", engagementId)

                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 409 when engagement is not ACCEPTED")
        void shouldReturn409WhenNotAccepted() throws Exception {
            // Arrange
            doThrow(new BusinessConflictException(
                    "Only ACCEPTED engagements can be finished",
                    "INVALID_ENGAGEMENT_STATUS"))
                    .when(engagementApplicationService)
                    .finishEngagement(eq(engagementId), any());

            // Act & Assert
            mockMvc.perform(put("/api/v1/engagements/{id}/finish", engagementId)

                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_ENGAGEMENT_STATUS"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/engagements/{id}/cancel")
    class CancelEngagement {

        @Test
        @DisplayName("Should return 200 on successful cancel with reason")
        void shouldCancelSuccessfully() throws Exception {
            // Arrange
            CancelEngagementRequest request = CancelEngagementRequest.builder()
                    .reason("No longer need the service")
                    .build();

            doNothing().when(engagementApplicationService)
                    .cancelEngagement(eq(engagementId), any(), any());

            // Act & Assert
            mockMvc.perform(put("/api/v1/engagements/{id}/cancel", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Engagement cancelled successfully"));
        }

        @Test
        @DisplayName("Should return 404 when engagement not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Engagement", engagementId))
                    .when(engagementApplicationService)
                    .cancelEngagement(eq(engagementId), any(), any());

            // Act & Assert
            mockMvc.perform(put("/api/v1/engagements/{id}/cancel", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when cancellation reason missing for ACCEPTED")
        void shouldReturn409WhenReasonMissing() throws Exception {
            // Arrange
            doThrow(new BusinessConflictException(
                    "Cancellation reason is required for accepted engagements",
                    "CANCELLATION_REASON_REQUIRED"))
                    .when(engagementApplicationService)
                    .cancelEngagement(eq(engagementId), any(), any());

            // Act & Assert
            mockMvc.perform(put("/api/v1/engagements/{id}/cancel", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("CANCELLATION_REASON_REQUIRED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/engagements/{id}/reviews")
    class SubmitReview {

        @Test
        @DisplayName("Should return 201 with review response on success")
        void shouldSubmitReviewSuccessfully() throws Exception {
            // Arrange
            CreateReviewRequest request = CreateReviewRequest.builder()
                    .rating(new BigDecimal("4.5"))
                    .comment("Great agent")
                    .build();

            when(agentReviewApplicationService.submitReview(eq(engagementId), any(), any()))
                    .thenReturn(reviewResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Review submitted successfully"))
                    .andExpect(jsonPath("$.data.review_id").value(reviewResponse.getReviewId().toString()))
                    .andExpect(jsonPath("$.data.engagement_id").value(engagementId.toString()))
                    .andExpect(jsonPath("$.data.agent_user_id").value(agentUserId.toString()))
                    .andExpect(jsonPath("$.data.reviewer_id").value(ownerId.toString()))
                    .andExpect(jsonPath("$.data.rating").value(4.5))
                    .andExpect(jsonPath("$.data.comment").value("Great agent"));
        }

        @Test
        @DisplayName("Should return 400 when rating is missing")
        void shouldReturn400WhenRatingMissing() throws Exception {
            // Arrange
            String requestBody = "{\"comment\": \"Great agent\"}";

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when rating is below 1.0")
        void shouldReturn400WhenRatingTooLow() throws Exception {
            // Arrange
            CreateReviewRequest request = CreateReviewRequest.builder()
                    .rating(new BigDecimal("0.5"))
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when rating exceeds 5.0")
        void shouldReturn400WhenRatingTooHigh() throws Exception {
            // Arrange
            CreateReviewRequest request = CreateReviewRequest.builder()
                    .rating(new BigDecimal("5.5"))
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 404 when engagement not found")
        void shouldReturn404WhenEngagementNotFound() throws Exception {
            // Arrange
            CreateReviewRequest request = CreateReviewRequest.builder()
                    .rating(new BigDecimal("4.0"))
                    .build();

            when(agentReviewApplicationService.submitReview(eq(engagementId), any(), any()))
                    .thenThrow(new ResourceNotFoundException("Engagement", engagementId));

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 409 when review already exists")
        void shouldReturn409WhenReviewAlreadyExists() throws Exception {
            // Arrange
            CreateReviewRequest request = CreateReviewRequest.builder()
                    .rating(new BigDecimal("4.0"))
                    .build();

            when(agentReviewApplicationService.submitReview(eq(engagementId), any(), any()))
                    .thenThrow(new BusinessConflictException(
                            "A review has already been submitted for this engagement",
                            "REVIEW_ALREADY_EXISTS"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("REVIEW_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("Should return 409 when engagement status invalid for review")
        void shouldReturn409WhenInvalidStatus() throws Exception {
            // Arrange
            CreateReviewRequest request = CreateReviewRequest.builder()
                    .rating(new BigDecimal("4.0"))
                    .build();

            when(agentReviewApplicationService.submitReview(eq(engagementId), any(), any()))
                    .thenThrow(new BusinessConflictException(
                            "Reviews can only be submitted for FINISHED or CANCELLED engagements",
                            "INVALID_ENGAGEMENT_STATUS"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_ENGAGEMENT_STATUS"));
        }

        @Test
        @DisplayName("Should accept review with rating only (no comment)")
        void shouldAcceptReviewWithRatingOnly() throws Exception {
            // Arrange
            CreateReviewRequest request = CreateReviewRequest.builder()
                    .rating(new BigDecimal("3.0"))
                    .build();

            ReviewResponse noCommentResponse = ReviewResponse.builder()
                    .reviewId(UUID.randomUUID())
                    .engagementId(engagementId)
                    .agentUserId(agentUserId)
                    .reviewerId(ownerId)
                    .rating(new BigDecimal("3.0"))
                    .comment(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(agentReviewApplicationService.submitReview(eq(engagementId), any(), any()))
                    .thenReturn(noCommentResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/{id}/reviews", engagementId)

                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.rating").value(3.0))
                    .andExpect(jsonPath("$.data.comment").doesNotExist());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/engagements/agent-proposal")
    class SubmitAgentProposal {

        @Test
        @DisplayName("Should return 201 when proposal submitted successfully")
        void shouldReturn201OnSuccess() throws Exception {
            // Arrange
            SubmitAgentProposalRequest request = SubmitAgentProposalRequest.builder()
                    .agentProposalId(UUID.randomUUID())
                    .propertyId(UUID.randomUUID())
                    .build();

            when(engagementApplicationService.submitAgentProposal(any(), any()))
                    .thenReturn(engagementId);

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/agent-proposal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.engagement_id").value(engagementId.toString()));
        }

        @Test
        @DisplayName("Should return 400 when property_id is missing")
        void shouldReturn400WhenPropertyIdMissing() throws Exception {
            // Arrange
            String requestBody = "{\"agent_proposal_id\": \"" + UUID.randomUUID() + "\"}";

            // Act & Assert
            mockMvc.perform(post("/api/v1/engagements/agent-proposal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));
        }
    }
}
