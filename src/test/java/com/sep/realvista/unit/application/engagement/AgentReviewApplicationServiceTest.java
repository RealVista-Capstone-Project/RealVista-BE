package com.sep.realvista.unit.application.engagement;

import com.sep.realvista.application.engagement.dto.CreateReviewRequest;
import com.sep.realvista.application.engagement.dto.ReviewResponse;
import com.sep.realvista.application.engagement.service.AgentReviewApplicationService;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.agent.AgentReview;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentReviewApplicationService Unit Tests")
class AgentReviewApplicationServiceTest {

    @Mock
    private EngagementRepository engagementRepository;

    @Mock
    private AgentReviewRepository agentReviewRepository;

    @Mock
    private AgentProfileRepository agentProfileRepository;

    @InjectMocks
    private AgentReviewApplicationService agentReviewApplicationService;

    private UUID engagementId;
    private UUID reviewerId;
    private UUID agentUserId;
    private UUID agentProfileId;
    private UUID listingId;
    private UUID reviewId;
    private CreateReviewRequest createReviewRequest;
    private Engagement agentProposalEngagement;
    private Engagement ownerInvitationEngagement;
    private AgentProfile agentProfile;

    @BeforeEach
    void setUp() {
        engagementId = UUID.randomUUID();
        reviewerId = UUID.randomUUID();
        agentUserId = UUID.randomUUID();
        agentProfileId = UUID.randomUUID();
        listingId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        createReviewRequest = CreateReviewRequest.builder()
                .rating(new BigDecimal("4.5"))
                .comment("Great agent, very professional")
                .build();

        // AGENT_PROPOSAL: agent is initiator, owner is receiver
        agentProposalEngagement = Engagement.builder()
                .engagementId(engagementId)
                .initiatorId(agentUserId)
                .receiverId(reviewerId)
                .engagementType(EngagementType.AGENT_PROPOSAL)
                .status(EngagementStatus.FINISHED)
                .listingId(listingId)
                .build();

        // OWNER_INVITATION: owner is initiator, agent is receiver
        ownerInvitationEngagement = Engagement.builder()
                .engagementId(engagementId)
                .initiatorId(reviewerId)
                .receiverId(agentUserId)
                .engagementType(EngagementType.OWNER_INVITATION)
                .status(EngagementStatus.FINISHED)
                .listingId(listingId)
                .build();

        agentProfile = AgentProfile.builder()
                .agentProfileId(agentProfileId)
                .userId(agentUserId)
                .rating(new BigDecimal("4.0"))
                .build();
    }

    @Nested
    @DisplayName("submitReview - Success Scenarios")
    class SubmitReviewSuccess {

        @Test
        @DisplayName("Should submit review for FINISHED AGENT_PROPOSAL engagement and return response with reviewerId")
        void shouldSubmitReviewForFinishedAgentProposal() {
            // Arrange
            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent, very professional")
                    .rating(new BigDecimal("4.5"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("4.25"));

            // Act
            ReviewResponse response = agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getReviewId()).isEqualTo(reviewId);
            assertThat(response.getEngagementId()).isEqualTo(engagementId);
            assertThat(response.getAgentUserId()).isEqualTo(agentUserId);
            assertThat(response.getReviewerId()).isEqualTo(reviewerId);
            assertThat(response.getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
            assertThat(response.getComment()).isEqualTo("Great agent, very professional");
        }

        @Test
        @DisplayName("Should submit review for FINISHED OWNER_INVITATION engagement")
        void shouldSubmitReviewForFinishedOwnerInvitation() {
            // Arrange
            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent, very professional")
                    .rating(new BigDecimal("4.5"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(ownerInvitationEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("4.25"));

            // Act
            ReviewResponse response = agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getReviewId()).isEqualTo(reviewId);
            assertThat(response.getAgentUserId()).isEqualTo(agentUserId);
            assertThat(response.getReviewerId()).isEqualTo(reviewerId);
        }

        @Test
        @DisplayName("Should submit review for CANCELLED engagement")
        void shouldSubmitReviewForCancelledEngagement() {
            // Arrange
            Engagement cancelledEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(reviewerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.CANCELLED)
                    .listingId(listingId)
                    .cancellationReason("Project cancelled")
                    .build();

            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent, very professional")
                    .rating(new BigDecimal("4.5"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(cancelledEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("4.5"));

            // Act
            ReviewResponse response = agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getReviewId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("Should correctly build AgentReview entity with all fields")
        void shouldBuildAgentReviewEntityCorrectly() {
            // Arrange
            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent, very professional")
                    .rating(new BigDecimal("4.5"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("4.5"));

            // Act
            agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert - verify the review entity was built correctly
            ArgumentCaptor<AgentReview> reviewCaptor = ArgumentCaptor.forClass(AgentReview.class);
            verify(agentReviewRepository).save(reviewCaptor.capture());

            AgentReview capturedReview = reviewCaptor.getValue();
            assertThat(capturedReview.getAgentProfileId()).isEqualTo(agentProfileId);
            assertThat(capturedReview.getReviewerId()).isEqualTo(reviewerId);
            assertThat(capturedReview.getListingId()).isEqualTo(listingId);
            assertThat(capturedReview.getEngagementId()).isEqualTo(engagementId);
            assertThat(capturedReview.getReview()).isEqualTo("Great agent, very professional");
            assertThat(capturedReview.getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        }

        @Test
        @DisplayName("Should update agent average rating after review submission")
        void shouldUpdateAgentAverageRating() {
            // Arrange
            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent")
                    .rating(new BigDecimal("5.0"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("4.333"));

            // Act
            agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert - rating should be rounded to 1 decimal place using HALF_UP
            ArgumentCaptor<AgentProfile> profileCaptor = ArgumentCaptor.forClass(AgentProfile.class);
            verify(agentProfileRepository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getRating()).isEqualByComparingTo(new BigDecimal("4.3"));
        }

        @Test
        @DisplayName("Should not update agent rating when average is null")
        void shouldNotUpdateRatingWhenAverageIsNull() {
            // Arrange
            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent")
                    .rating(new BigDecimal("4.5"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(null);

            // Act
            agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert - should not attempt to save updated profile
            verify(agentProfileRepository, never()).save(any(AgentProfile.class));
        }

        @Test
        @DisplayName("Should submit review with null comment")
        void shouldSubmitReviewWithNullComment() {
            // Arrange
            CreateReviewRequest requestNoComment = CreateReviewRequest.builder()
                    .rating(new BigDecimal("3.0"))
                    .comment(null)
                    .build();

            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review(null)
                    .rating(new BigDecimal("3.0"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("3.0"));

            // Act
            ReviewResponse response = agentReviewApplicationService.submitReview(engagementId, reviewerId, requestNoComment);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getComment()).isNull();
            assertThat(response.getRating()).isEqualByComparingTo(new BigDecimal("3.0"));
        }
    }

    @Nested
    @DisplayName("submitReview - Failure Scenarios")
    class SubmitReviewFailure {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when engagement does not exist")
        void shouldThrowWhenEngagementNotFound() {
            // Arrange
            when(engagementRepository.findById(engagementId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Engagement")
                    .hasMessageContaining(engagementId.toString());

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when reviewer is not the owner (AGENT_PROPOSAL)")
        void shouldThrowWhenReviewerIsNotOwnerAgentProposal() {
            // Arrange - for AGENT_PROPOSAL, owner is the receiver
            UUID nonOwnerId = UUID.randomUUID();
            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, nonOwnerId, createReviewRequest))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("not authorized to review");

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when reviewer is not the owner (OWNER_INVITATION)")
        void shouldThrowWhenReviewerIsNotOwnerOwnerInvitation() {
            // Arrange - for OWNER_INVITATION, owner is the initiator
            UUID nonOwnerId = UUID.randomUUID();
            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(ownerInvitationEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, nonOwnerId, createReviewRequest))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("not authorized to review");

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when engagement status is SUBMITTED")
        void shouldThrowWhenEngagementStatusIsSubmitted() {
            // Arrange
            Engagement submittedEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(reviewerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.SUBMITTED)
                    .listingId(listingId)
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(submittedEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("FINISHED or CANCELLED");

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when engagement status is ACCEPTED")
        void shouldThrowWhenEngagementStatusIsAccepted() {
            // Arrange
            Engagement acceptedEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(reviewerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.ACCEPTED)
                    .listingId(listingId)
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(acceptedEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("FINISHED or CANCELLED");

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when engagement status is REJECTED")
        void shouldThrowWhenEngagementStatusIsRejected() {
            // Arrange
            Engagement rejectedEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(reviewerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.REJECTED)
                    .listingId(listingId)
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(rejectedEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("FINISHED or CANCELLED");

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when engagement has no listing")
        void shouldThrowWhenEngagementHasNoListing() {
            // Arrange
            Engagement noListingEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(reviewerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.FINISHED)
                    .listingId(null)
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(noListingEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("no associated listing");

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when review already exists for engagement")
        void shouldThrowWhenReviewAlreadyExists() {
            // Arrange
            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("already been submitted");

            verify(agentReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when agent profile not found")
        void shouldThrowWhenAgentProfileNotFound() {
            // Arrange
            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Agent profile not found");

            verify(agentReviewRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("submitReview - Agent Resolution")
    class AgentResolution {

        @Test
        @DisplayName("Should resolve agent as initiator for AGENT_PROPOSAL engagement")
        void shouldResolveAgentAsInitiatorForAgentProposal() {
            // Arrange
            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent")
                    .rating(new BigDecimal("4.5"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(agentProposalEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("4.5"));

            // Act
            ReviewResponse response = agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert - agent is the initiator for AGENT_PROPOSAL
            verify(agentProfileRepository).findByUserId(agentProposalEngagement.getInitiatorId());
            assertThat(response.getAgentUserId()).isEqualTo(agentUserId);
        }

        @Test
        @DisplayName("Should resolve agent as receiver for OWNER_INVITATION engagement")
        void shouldResolveAgentAsReceiverForOwnerInvitation() {
            // Arrange
            AgentReview savedReview = AgentReview.builder()
                    .agentReviewId(reviewId)
                    .agentProfileId(agentProfileId)
                    .reviewerId(reviewerId)
                    .listingId(listingId)
                    .engagementId(engagementId)
                    .review("Great agent")
                    .rating(new BigDecimal("4.5"))
                    .build();

            when(engagementRepository.findById(engagementId)).thenReturn(Optional.of(ownerInvitationEngagement));
            when(agentReviewRepository.existsByEngagementId(engagementId)).thenReturn(false);
            when(agentProfileRepository.findByUserId(agentUserId)).thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.save(any(AgentReview.class))).thenReturn(savedReview);
            when(agentReviewRepository.calculateAverageRating(agentProfileId)).thenReturn(new BigDecimal("4.5"));

            // Act
            ReviewResponse response = agentReviewApplicationService.submitReview(engagementId, reviewerId, createReviewRequest);

            // Assert - agent is the receiver for OWNER_INVITATION
            verify(agentProfileRepository).findByUserId(ownerInvitationEngagement.getReceiverId());
            assertThat(response.getAgentUserId()).isEqualTo(agentUserId);
        }
    }
}
