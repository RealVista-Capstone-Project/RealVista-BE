package com.sep.realvista.unit.application.engagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.CancelEngagementRequest;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.mapper.EngagementMapper;
import com.sep.realvista.application.engagement.service.EngagementApplicationService;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.application.engagement.dto.SubmitAgentProposalRequest;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EngagementApplicationService Unit Tests")
class EngagementApplicationServiceTest {

    @Mock
    private EngagementRepository engagementRepository;

    @Mock
    private AgentProfileRepository agentProfileRepository;

    @Mock
    private AgentReviewRepository agentReviewRepository;

    @Mock
    private EngagementMapper engagementMapper;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private PropertyAttributeValueRepository propertyAttributeValueRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private AgentProposalRepository agentProposalRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationApplicationService notificationApplicationService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EngagementApplicationService engagementApplicationService;

    private UUID ownerId;
    private UUID agentUserId;
    private UUID engagementId;
    private UUID listingId;
    private UUID propertyId;
    private UUID agentProfileId;
    private Engagement agentProposalEngagement;
    private Engagement ownerInvitationEngagement;
    private AgentProfile agentProfile;
    private User agentUser;
    private HiredAgentResponse hiredAgentResponse;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        agentUserId = UUID.randomUUID();
        engagementId = UUID.randomUUID();
        listingId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        agentProfileId = UUID.randomUUID();

        agentUser = User.builder()
                .userId(agentUserId)
                .firstName("John")
                .lastName("Agent")
                .build();

        // AGENT_PROPOSAL: agent is initiator, owner is receiver
        agentProposalEngagement = Engagement.builder()
                .engagementId(engagementId)
                .initiatorId(agentUserId)
                .receiverId(ownerId)
                .engagementType(EngagementType.AGENT_PROPOSAL)
                .status(EngagementStatus.ACCEPTED)
                .listingId(listingId)
                .propertyId(propertyId)
                .build();

        // OWNER_INVITATION: owner is initiator, agent is receiver
        ownerInvitationEngagement = Engagement.builder()
                .engagementId(engagementId)
                .initiatorId(ownerId)
                .receiverId(agentUserId)
                .engagementType(EngagementType.OWNER_INVITATION)
                .status(EngagementStatus.ACCEPTED)
                .listingId(listingId)
                .propertyId(propertyId)
                .build();

        agentProfile = AgentProfile.builder()
                .agentProfileId(agentProfileId)
                .userId(agentUserId)
                .rating(new BigDecimal("4.0"))
                .build();

        hiredAgentResponse = HiredAgentResponse.builder()
                .agentUserId(agentUserId)
                .agentFullName("John Agent")
                .engagementId(engagementId)
                .engagementType("AGENT_PROPOSAL")
                .status("ACCEPTED")
                .hasReview(false)
                .build();
    }

    // ========================================================================
    // getHiredAgents
    // ========================================================================

    @Nested
    @DisplayName("getHiredAgents")
    class GetHiredAgents {

        @Test
        @DisplayName("Should return paginated hired agents without status filter")
        void shouldReturnPaginatedHiredAgentsWithoutStatusFilter() {
            // Arrange
            Page<Engagement> engagementPage = new PageImpl<>(
                    List.of(agentProposalEngagement),
                    PageRequest.of(0, 10),
                    1
            );

            when(engagementRepository.findAllHiredAgentEngagements(
                    eq(ownerId), isNull(), any(Pageable.class)))
                    .thenReturn(engagementPage);
            when(agentProfileRepository.findByUserIds(anyList()))
                    .thenReturn(List.of(agentProfile));
            when(agentReviewRepository.findReviewedEngagementIds(anyList()))
                    .thenReturn(List.of());
            when(engagementMapper.toHiredAgentResponse(
                    any(Engagement.class), any(), any(), eq(false), isNull(), eq(List.of())))
                    .thenReturn(hiredAgentResponse);

            // Act
            PageResponse<HiredAgentResponse> result =
                    engagementApplicationService.getHiredAgents(ownerId, null, null, 0, 10);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAgentUserId()).isEqualTo(agentUserId);
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should return paginated hired agents with status filter")
        void shouldReturnPaginatedHiredAgentsWithStatusFilter() {
            // Arrange
            Page<Engagement> engagementPage = new PageImpl<>(
                    List.of(agentProposalEngagement),
                    PageRequest.of(0, 10),
                    1
            );

            when(engagementRepository.findHiredAgentEngagements(
                    eq(ownerId), eq(EngagementStatus.ACCEPTED), isNull(), any(Pageable.class)))
                    .thenReturn(engagementPage);
            when(agentProfileRepository.findByUserIds(anyList()))
                    .thenReturn(List.of(agentProfile));
            when(agentReviewRepository.findReviewedEngagementIds(anyList()))
                    .thenReturn(List.of());
            when(engagementMapper.toHiredAgentResponse(
                    any(Engagement.class), any(), any(), eq(false), isNull(), eq(List.of())))
                    .thenReturn(hiredAgentResponse);

            // Act
            PageResponse<HiredAgentResponse> result =
                    engagementApplicationService.getHiredAgents(ownerId, "ACCEPTED", null, 0, 10);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(engagementRepository).findHiredAgentEngagements(
                    eq(ownerId), eq(EngagementStatus.ACCEPTED), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw BusinessConflictException for invalid status")
        void shouldThrowForInvalidStatus() {
            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.getHiredAgents(ownerId, "INVALID_STATUS", null, 0, 10))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Invalid engagement status");
        }

        @Test
        @DisplayName("Should return empty page when no engagements found")
        void shouldReturnEmptyPage() {
            // Arrange
            Page<Engagement> emptyPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0
            );

            when(engagementRepository.findAllHiredAgentEngagements(
                    eq(ownerId), isNull(), any(Pageable.class)))
                    .thenReturn(emptyPage);
            when(agentProfileRepository.findByUserIds(anyList()))
                    .thenReturn(List.of());
            when(agentReviewRepository.findReviewedEngagementIds(anyList()))
                    .thenReturn(List.of());

            // Act
            PageResponse<HiredAgentResponse> result =
                    engagementApplicationService.getHiredAgents(ownerId, null, null, 0, 10);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should normalize search query by trimming whitespace")
        void shouldNormalizeSearchQuery() {
            // Arrange
            Page<Engagement> engagementPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0
            );

            when(engagementRepository.findAllHiredAgentEngagements(
                    eq(ownerId), eq("John"), any(Pageable.class)))
                    .thenReturn(engagementPage);
            when(agentProfileRepository.findByUserIds(anyList()))
                    .thenReturn(List.of());
            when(agentReviewRepository.findReviewedEngagementIds(anyList()))
                    .thenReturn(List.of());

            // Act
            engagementApplicationService.getHiredAgents(ownerId, null, "  John  ", 0, 10);

            // Assert
            verify(engagementRepository).findAllHiredAgentEngagements(
                    eq(ownerId), eq("John"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should treat blank search as null")
        void shouldTreatBlankSearchAsNull() {
            // Arrange
            Page<Engagement> engagementPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0
            );

            when(engagementRepository.findAllHiredAgentEngagements(
                    eq(ownerId), isNull(), any(Pageable.class)))
                    .thenReturn(engagementPage);
            when(agentProfileRepository.findByUserIds(anyList()))
                    .thenReturn(List.of());
            when(agentReviewRepository.findReviewedEngagementIds(anyList()))
                    .thenReturn(List.of());

            // Act
            engagementApplicationService.getHiredAgents(ownerId, null, "   ", 0, 10);

            // Assert
            verify(engagementRepository).findAllHiredAgentEngagements(
                    eq(ownerId), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should correctly mark engagements that have reviews")
        void shouldMarkEngagementsWithReviews() {
            // Arrange
            Page<Engagement> engagementPage = new PageImpl<>(
                    List.of(agentProposalEngagement),
                    PageRequest.of(0, 10),
                    1
            );

            HiredAgentResponse reviewedResponse = HiredAgentResponse.builder()
                    .agentUserId(agentUserId)
                    .engagementId(engagementId)
                    .hasReview(true)
                    .build();

            when(engagementRepository.findAllHiredAgentEngagements(
                    eq(ownerId), isNull(), any(Pageable.class)))
                    .thenReturn(engagementPage);
            when(agentProfileRepository.findByUserIds(anyList()))
                    .thenReturn(List.of(agentProfile));
            when(agentReviewRepository.findReviewedEngagementIds(anyList()))
                    .thenReturn(List.of(engagementId));
            when(engagementMapper.toHiredAgentResponse(
                    any(Engagement.class), any(), any(), eq(true), isNull(), eq(List.of())))
                    .thenReturn(reviewedResponse);

            // Act
            PageResponse<HiredAgentResponse> result =
                    engagementApplicationService.getHiredAgents(ownerId, null, null, 0, 10);

            // Assert
            assertThat(result.getContent().get(0).isHasReview()).isTrue();
            verify(engagementMapper).toHiredAgentResponse(
                    any(Engagement.class), any(), any(), eq(true), isNull(), eq(List.of()));
        }
    }

    // ========================================================================
    // getEngagementById
    // ========================================================================

    @Nested
    @DisplayName("getEngagementById")
    class GetEngagementById {

        @Test
        @DisplayName("Should return engagement detail for owner of AGENT_PROPOSAL")
        void shouldReturnEngagementDetailForAgentProposal() {
            // Arrange
            when(engagementRepository.findByIdWithFetches(engagementId))
                    .thenReturn(Optional.of(agentProposalEngagement));
            when(agentProfileRepository.findByUserId(agentUserId))
                    .thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.existsByEngagementId(engagementId))
                    .thenReturn(false);
            when(listingRepository.findThumbnailByListingId(listingId))
                    .thenReturn(Optional.of("https://example.com/thumb.jpg"));
            when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                    .thenReturn(List.of());
            when(engagementMapper.toHiredAgentResponse(
                    any(Engagement.class), any(), any(), eq(false),
                    eq("https://example.com/thumb.jpg"), eq(List.of())))
                    .thenReturn(hiredAgentResponse);

            // Act
            HiredAgentResponse result = engagementApplicationService.getEngagementById(engagementId, ownerId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAgentUserId()).isEqualTo(agentUserId);
            assertThat(result.getEngagementId()).isEqualTo(engagementId);
        }

        @Test
        @DisplayName("Should return engagement detail for owner of OWNER_INVITATION")
        void shouldReturnEngagementDetailForOwnerInvitation() {
            // Arrange
            when(engagementRepository.findByIdWithFetches(engagementId))
                    .thenReturn(Optional.of(ownerInvitationEngagement));
            when(agentProfileRepository.findByUserId(agentUserId))
                    .thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.existsByEngagementId(engagementId))
                    .thenReturn(false);
            when(listingRepository.findThumbnailByListingId(listingId))
                    .thenReturn(Optional.empty());
            when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                    .thenReturn(List.of());
            when(engagementMapper.toHiredAgentResponse(
                    any(Engagement.class), any(), any(), eq(false),
                    isNull(), eq(List.of())))
                    .thenReturn(hiredAgentResponse);

            // Act
            HiredAgentResponse result = engagementApplicationService.getEngagementById(engagementId, ownerId);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when engagement not found")
        void shouldThrowWhenEngagementNotFound() {
            // Arrange
            when(engagementRepository.findByIdWithFetches(engagementId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.getEngagementById(engagementId, ownerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Engagement");
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when user is not the owner")
        void shouldThrowWhenUserIsNotOwner() {
            // Arrange
            UUID nonOwnerId = UUID.randomUUID();
            when(engagementRepository.findByIdWithFetches(engagementId))
                    .thenReturn(Optional.of(agentProposalEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.getEngagementById(engagementId, nonOwnerId))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("Should skip listing queries when engagement has no listing")
        void shouldSkipListingQueriesWhenNoListing() {
            // Arrange
            Engagement noListingEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(ownerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.ACCEPTED)
                    .listingId(null)
                    .propertyId(null)
                    .build();

            when(engagementRepository.findByIdWithFetches(engagementId))
                    .thenReturn(Optional.of(noListingEngagement));
            when(agentProfileRepository.findByUserId(agentUserId))
                    .thenReturn(Optional.of(agentProfile));
            when(agentReviewRepository.existsByEngagementId(engagementId))
                    .thenReturn(false);
            when(engagementMapper.toHiredAgentResponse(
                    any(Engagement.class), any(), any(), eq(false),
                    isNull(), eq(List.of())))
                    .thenReturn(hiredAgentResponse);

            // Act
            engagementApplicationService.getEngagementById(engagementId, ownerId);

            // Assert - should not query listing or attribute repos
            verify(listingRepository, never()).findThumbnailByListingId(any());
            verify(propertyAttributeValueRepository, never()).findByPropertyIdWithAttribute(any());
        }
    }

    // ========================================================================
    // finishEngagement
    // ========================================================================

    @Nested
    @DisplayName("finishEngagement")
    class FinishEngagement {

        @Test
        @DisplayName("Should finish ACCEPTED AGENT_PROPOSAL engagement")
        void shouldFinishAcceptedAgentProposal() {
            // Arrange
            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(agentProposalEngagement));
            when(engagementRepository.save(any(Engagement.class)))
                    .thenReturn(agentProposalEngagement);

            // Act
            engagementApplicationService.finishEngagement(engagementId, ownerId);

            // Assert
            assertThat(agentProposalEngagement.getStatus()).isEqualTo(EngagementStatus.FINISHED);
            verify(engagementRepository).save(agentProposalEngagement);
        }

        @Test
        @DisplayName("Should finish ACCEPTED OWNER_INVITATION engagement")
        void shouldFinishAcceptedOwnerInvitation() {
            // Arrange
            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(ownerInvitationEngagement));
            when(engagementRepository.save(any(Engagement.class)))
                    .thenReturn(ownerInvitationEngagement);

            // Act
            engagementApplicationService.finishEngagement(engagementId, ownerId);

            // Assert
            assertThat(ownerInvitationEngagement.getStatus()).isEqualTo(EngagementStatus.FINISHED);
            verify(engagementRepository).save(ownerInvitationEngagement);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when engagement not found")
        void shouldThrowWhenEngagementNotFound() {
            // Arrange
            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.finishEngagement(engagementId, ownerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Engagement");

            verify(engagementRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when user is not the owner")
        void shouldThrowWhenUserIsNotOwner() {
            // Arrange
            UUID nonOwnerId = UUID.randomUUID();
            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(agentProposalEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.finishEngagement(engagementId, nonOwnerId))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("not authorized");

            verify(engagementRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when engagement is not ACCEPTED")
        void shouldThrowWhenEngagementIsNotAccepted() {
            // Arrange
            Engagement submittedEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(ownerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.SUBMITTED)
                    .build();

            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(submittedEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.finishEngagement(engagementId, ownerId))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Only ACCEPTED engagements can be finished");

            verify(engagementRepository, never()).save(any());
        }
    }

    // ========================================================================
    // cancelEngagement
    // ========================================================================

    @Nested
    @DisplayName("cancelEngagement")
    class CancelEngagement {

        @Test
        @DisplayName("Should cancel ACCEPTED engagement with reason")
        void shouldCancelAcceptedEngagementWithReason() {
            // Arrange
            CancelEngagementRequest request = CancelEngagementRequest.builder()
                    .reason("Found a better agent")
                    .build();

            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(agentProposalEngagement));
            when(engagementRepository.save(any(Engagement.class)))
                    .thenReturn(agentProposalEngagement);

            // Act
            engagementApplicationService.cancelEngagement(engagementId, ownerId, request);

            // Assert
            assertThat(agentProposalEngagement.getStatus()).isEqualTo(EngagementStatus.CANCELLED);
            assertThat(agentProposalEngagement.getCancellationReason()).isEqualTo("Found a better agent");
            verify(engagementRepository).save(agentProposalEngagement);
        }

        @Test
        @DisplayName("Should cancel SUBMITTED engagement without reason")
        void shouldCancelSubmittedEngagementWithoutReason() {
            // Arrange
            Engagement submittedEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(ownerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.SUBMITTED)
                    .build();

            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(submittedEngagement));
            when(engagementRepository.save(any(Engagement.class)))
                    .thenReturn(submittedEngagement);

            // Act
            engagementApplicationService.cancelEngagement(engagementId, ownerId, null);

            // Assert
            assertThat(submittedEngagement.getStatus()).isEqualTo(EngagementStatus.CANCELLED);
            verify(engagementRepository).save(submittedEngagement);
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when cancelling ACCEPTED without reason")
        void shouldThrowWhenCancellingAcceptedWithoutReason() {
            // Arrange
            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(agentProposalEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.cancelEngagement(engagementId, ownerId, null))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Cancellation reason is required");

            verify(engagementRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when engagement not found")
        void shouldThrowWhenEngagementNotFound() {
            // Arrange
            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.empty());

            CancelEngagementRequest request = CancelEngagementRequest.builder()
                    .reason("test")
                    .build();

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.cancelEngagement(engagementId, ownerId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Engagement");

            verify(engagementRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when user is not the owner")
        void shouldThrowWhenUserIsNotOwner() {
            // Arrange
            UUID nonOwnerId = UUID.randomUUID();
            CancelEngagementRequest request = CancelEngagementRequest.builder()
                    .reason("test")
                    .build();

            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(agentProposalEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.cancelEngagement(engagementId, nonOwnerId, request))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("not authorized");

            verify(engagementRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when engagement is FINISHED")
        void shouldThrowWhenEngagementIsFinished() {
            // Arrange
            Engagement finishedEngagement = Engagement.builder()
                    .engagementId(engagementId)
                    .initiatorId(agentUserId)
                    .receiverId(ownerId)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .status(EngagementStatus.FINISHED)
                    .build();

            CancelEngagementRequest request = CancelEngagementRequest.builder()
                    .reason("test")
                    .build();

            when(engagementRepository.findById(engagementId))
                    .thenReturn(Optional.of(finishedEngagement));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.cancelEngagement(engagementId, ownerId, request))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Only ACCEPTED or SUBMITTED engagements can be cancelled");

            verify(engagementRepository, never()).save(any());
        }
    }

    // ========================================================================
    // submitAgentProposal
    // ========================================================================

    @Nested
    @DisplayName("submitAgentProposal")
    class SubmitAgentProposal {

        private SubmitAgentProposalRequest request;
        private AgentProposal agentProposal;
        private Property property;

        @BeforeEach
        void setUp() {
            request = SubmitAgentProposalRequest.builder()
                    .agentProposalId(UUID.randomUUID())
                    .propertyId(UUID.randomUUID())
                    .build();

            agentProposal = AgentProposal.builder()
                    .agentProposalId(request.getAgentProposalId())
                    .userId(agentUserId)
                    .title("Expert listing package")
                    .pitchContent("My expert pitch")
                    .build();

            property = Property.builder()
                    .propertyId(request.getPropertyId())
                    .ownerId(ownerId)
                    .streetAddress("10 Nguyen Hue, Q1")
                    .build();
        }

        @Test
        @DisplayName("Should successfully submit agent proposal")
        void shouldSubmitSuccessfully() {
            // Arrange
            UUID expectedEngagementId = UUID.randomUUID();
            User ownerUser = User.builder()
                    .userId(ownerId)
                    .email(Email.of("owner@test.com"))
                    .businessName("Owner Business")
                    .passwordHash("hash")
                    .build();
            User notifyingAgent = User.builder()
                    .userId(agentUserId)
                    .firstName("Jane")
                    .lastName("Agent")
                    .businessName("Jane Agent")
                    .passwordHash("hash")
                    .build();
            when(agentProposalRepository.findById(request.getAgentProposalId()))
                    .thenReturn(Optional.of(agentProposal));
            when(propertyRepository.findById(request.getPropertyId()))
                    .thenReturn(Optional.of(property));
            when(objectMapper.valueToTree(any()))
                    .thenReturn(NullNode.getInstance());
            when(engagementRepository.save(any(Engagement.class)))
                    .thenAnswer(invocation -> {
                        Engagement e = (Engagement) invocation.getArgument(0);
                        return Engagement.builder()
                                .engagementId(expectedEngagementId)
                                .initiatorId(e.getInitiatorId())
                                .receiverId(e.getReceiverId())
                                .engagementType(e.getEngagementType())
                                .propertyId(e.getPropertyId())
                                .content(e.getContent())
                                .build();
                    });
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(ownerUser));
            when(userRepository.findById(agentUserId)).thenReturn(Optional.of(notifyingAgent));

            // Act
            UUID result = engagementApplicationService.submitAgentProposal(agentUserId, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedEngagementId);
            verify(engagementRepository).save(any(Engagement.class));

            ArgumentCaptor<SendNotificationRequest> notifyCaptor =
                    ArgumentCaptor.forClass(SendNotificationRequest.class);
            verify(notificationApplicationService).sendNotification(notifyCaptor.capture());
            SendNotificationRequest sent = notifyCaptor.getValue();
            assertThat(sent.getUserId()).isEqualTo(ownerId);
            assertThat(sent.getEventType()).isEqualTo(EventType.NEW_AGENT_PROPOSAL);
            assertThat(sent.getEntityType()).isEqualTo(EntityType.PROPERTY);
            assertThat(sent.getEntityId()).isEqualTo(property.getPropertyId());
            assertThat(sent.getMetadata().get("engagementId")).isEqualTo(expectedEngagementId.toString());
            assertThat(sent.getMetadata().get("propertyId")).isEqualTo(property.getPropertyId().toString());
            assertThat(sent.getMetadata().get("agentUserId")).isEqualTo(agentUserId.toString());

            verify(emailService).sendTemplateMessageAsync(
                    eq("owner@test.com"),
                    anyString(),
                    eq("agent-proposal-notification"),
                    anyMap());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when proposal not found")
        void shouldThrowWhenProposalNotFound() {
            // Arrange
            when(agentProposalRepository.findById(request.getAgentProposalId()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.submitAgentProposal(agentUserId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("AgentProposal");

            verify(notificationApplicationService, never()).sendNotification(any());
            verify(emailService, never()).sendTemplateMessageAsync(anyString(), anyString(), anyString(), anyMap());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when agent does not own proposal template")
        void shouldThrowWhenAgentDoesNotOwnProposal() {
            // Arrange
            UUID otherAgentId = UUID.randomUUID();
            when(agentProposalRepository.findById(request.getAgentProposalId()))
                    .thenReturn(Optional.of(agentProposal));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.submitAgentProposal(otherAgentId, request))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("You do not own this proposal template");

            verify(notificationApplicationService, never()).sendNotification(any());
            verify(emailService, never()).sendTemplateMessageAsync(anyString(), anyString(), anyString(), anyMap());
        }

        @Test
        @DisplayName("Should throw BusinessConflictException when proposing to own property")
        void shouldThrowWhenProposingToOwnProperty() {
            // Arrange
            // Rebuild property with the agent as owner for this test case
            property = Property.builder()
                    .propertyId(request.getPropertyId())
                    .ownerId(agentUserId) // Agent is the owner
                    .build();
            when(agentProposalRepository.findById(request.getAgentProposalId()))
                    .thenReturn(Optional.of(agentProposal));
            when(propertyRepository.findById(request.getPropertyId()))
                    .thenReturn(Optional.of(property));

            // Act & Assert
            assertThatThrownBy(() ->
                    engagementApplicationService.submitAgentProposal(agentUserId, request))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("cannot submit a proposal to your own property");

            verify(notificationApplicationService, never()).sendNotification(any());
            verify(emailService, never()).sendTemplateMessageAsync(anyString(), anyString(), anyString(), anyMap());
        }
    }

    @Nested
    @DisplayName("Agent proposal accept/reject notifications")
    class AgentProposalDecisionNotifications {

        private final ObjectMapper jsonMapper = new ObjectMapper();

        @Test
        @DisplayName("acceptEngagement on AGENT_PROPOSAL notifies agent")
        void acceptAgentProposalNotifiesAgent() throws Exception {
            UUID eid = UUID.randomUUID();
            UUID owner = UUID.randomUUID();
            UUID agentId = UUID.randomUUID();
            UUID propId = UUID.randomUUID();

            Engagement eng = Engagement.builder()
                    .engagementId(eid)
                    .initiatorId(agentId)
                    .receiverId(owner)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .propertyId(propId)
                    .status(EngagementStatus.SUBMITTED)
                    .content(jsonMapper.readTree("{\"title\":\"Pitch X\"}"))
                    .build();

            when(engagementRepository.findById(eid)).thenReturn(Optional.of(eng));
            when(engagementRepository.save(any(Engagement.class))).thenAnswer(inv -> inv.getArgument(0));

            User agentUser = User.builder()
                    .userId(agentId)
                    .email(Email.of("agent@test.com"))
                    .businessName("Agent Co")
                    .passwordHash("x")
                    .build();
            User ownerUser = User.builder()
                    .userId(owner)
                    .firstName("O")
                    .lastName("Owner")
                    .businessName("Owner Biz")
                    .passwordHash("x")
                    .build();
            Property prop = Property.builder()
                    .propertyId(propId)
                    .ownerId(owner)
                    .streetAddress("1 Road")
                    .latitude(BigDecimal.ONE)
                    .longitude(BigDecimal.ONE)
                    .build();

            when(userRepository.findById(agentId)).thenReturn(Optional.of(agentUser));
            when(userRepository.findById(owner)).thenReturn(Optional.of(ownerUser));
            when(propertyRepository.findById(propId)).thenReturn(Optional.of(prop));

            engagementApplicationService.acceptEngagement(eid, owner);

            ArgumentCaptor<SendNotificationRequest> cap = ArgumentCaptor.forClass(SendNotificationRequest.class);
            verify(notificationApplicationService).sendNotification(cap.capture());
            assertThat(cap.getValue().getUserId()).isEqualTo(agentId);
            assertThat(cap.getValue().getEventType()).isEqualTo(EventType.AGENT_PROPOSAL_ACCEPTED);
            assertThat(cap.getValue().getEntityType()).isEqualTo(EntityType.PROPERTY);
            assertThat(cap.getValue().getEntityId()).isEqualTo(propId);
            assertThat(cap.getValue().getMetadata().get("decision")).isEqualTo("ACCEPTED");
            assertThat(cap.getValue().getMetadata().get("engagementId")).isEqualTo(eid.toString());

            verify(emailService).sendTemplateMessageAsync(
                    eq("agent@test.com"),
                    anyString(),
                    eq("agent-proposal-decision-notification"),
                    anyMap());
        }

        @Test
        @DisplayName("rejectEngagement on AGENT_PROPOSAL notifies agent")
        void rejectAgentProposalNotifiesAgent() throws Exception {
            UUID eid = UUID.randomUUID();
            UUID owner = UUID.randomUUID();
            UUID agentId = UUID.randomUUID();
            UUID propId = UUID.randomUUID();

            Engagement eng = Engagement.builder()
                    .engagementId(eid)
                    .initiatorId(agentId)
                    .receiverId(owner)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .propertyId(propId)
                    .status(EngagementStatus.SUBMITTED)
                    .content(jsonMapper.readTree("{\"title\":\"Pitch Y\"}"))
                    .build();

            when(engagementRepository.findById(eid)).thenReturn(Optional.of(eng));
            when(engagementRepository.save(any(Engagement.class))).thenAnswer(inv -> inv.getArgument(0));

            User agentUser = User.builder()
                    .userId(agentId)
                    .email(Email.of("agent2@test.com"))
                    .businessName("Agent Two")
                    .passwordHash("x")
                    .build();
            User ownerUser = User.builder()
                    .userId(owner)
                    .businessName("Owner Two")
                    .passwordHash("x")
                    .build();
            Property prop = Property.builder()
                    .propertyId(propId)
                    .ownerId(owner)
                    .streetAddress("2 Road")
                    .latitude(BigDecimal.ONE)
                    .longitude(BigDecimal.ONE)
                    .build();

            when(userRepository.findById(agentId)).thenReturn(Optional.of(agentUser));
            when(userRepository.findById(owner)).thenReturn(Optional.of(ownerUser));
            when(propertyRepository.findById(propId)).thenReturn(Optional.of(prop));

            engagementApplicationService.rejectEngagement(eid, owner);

            ArgumentCaptor<SendNotificationRequest> cap = ArgumentCaptor.forClass(SendNotificationRequest.class);
            verify(notificationApplicationService).sendNotification(cap.capture());
            assertThat(cap.getValue().getEventType()).isEqualTo(EventType.AGENT_PROPOSAL_REJECTED);
            assertThat(cap.getValue().getMetadata().get("decision")).isEqualTo("REJECTED");

            verify(emailService).sendTemplateMessageAsync(
                    eq("agent2@test.com"),
                    anyString(),
                    eq("agent-proposal-decision-notification"),
                    anyMap());
        }

        @Test
        @DisplayName("acceptEngagement on non-AGENT_PROPOSAL does not notify")
        void acceptNonAgentProposalDoesNotNotify() throws Exception {
            UUID eid = UUID.randomUUID();
            UUID owner = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            Engagement eng = Engagement.builder()
                    .engagementId(eid)
                    .initiatorId(tenantId)
                    .receiverId(owner)
                    .engagementType(EngagementType.TENANT_APPLICATION)
                    .status(EngagementStatus.SUBMITTED)
                    .content(jsonMapper.readTree("{}"))
                    .build();

            when(engagementRepository.findById(eid)).thenReturn(Optional.of(eng));
            when(engagementRepository.save(any(Engagement.class))).thenAnswer(inv -> inv.getArgument(0));

            engagementApplicationService.acceptEngagement(eid, owner);

            verify(notificationApplicationService, never()).sendNotification(any());
            verify(emailService, never()).sendTemplateMessageAsync(anyString(), anyString(), anyString(), anyMap());
        }

        @Test
        @DisplayName("acceptEngagement when not receiver does not notify")
        void acceptWhenNotReceiverDoesNotNotify() throws Exception {
            UUID eid = UUID.randomUUID();
            UUID owner = UUID.randomUUID();
            UUID agentId = UUID.randomUUID();
            UUID wrongUser = UUID.randomUUID();

            Engagement eng = Engagement.builder()
                    .engagementId(eid)
                    .initiatorId(agentId)
                    .receiverId(owner)
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .propertyId(UUID.randomUUID())
                    .status(EngagementStatus.SUBMITTED)
                    .content(jsonMapper.readTree("{}"))
                    .build();

            when(engagementRepository.findById(eid)).thenReturn(Optional.of(eng));

            assertThatThrownBy(() -> engagementApplicationService.acceptEngagement(eid, wrongUser))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("not authorized to accept");

            verify(engagementRepository, never()).save(any(Engagement.class));
            verify(notificationApplicationService, never()).sendNotification(any());
            verify(emailService, never()).sendTemplateMessageAsync(anyString(), anyString(), anyString(), anyMap());
        }
    }

    @Nested
    @DisplayName("createAgentCreatedPropertyLink")
    class CreateAgentCreatedPropertyLink {

        @Test
        @DisplayName("Should create owner-to-agent engagement with dedicated type")
        void shouldCreateOwnerToAgentEngagementWithDedicatedType() {
            UUID createdEngagementId = UUID.randomUUID();
            UUID testPropertyId = UUID.randomUUID();
            UUID testOwnerId = UUID.randomUUID();
            UUID testAgentId = UUID.randomUUID();

            when(objectMapper.valueToTree(any()))
                    .thenReturn(NullNode.getInstance());
            when(engagementRepository.save(any(Engagement.class)))
                    .thenAnswer(invocation -> {
                        Engagement input = invocation.getArgument(0);
                        return Engagement.builder()
                                .engagementId(createdEngagementId)
                                .initiatorId(input.getInitiatorId())
                                .receiverId(input.getReceiverId())
                                .engagementType(input.getEngagementType())
                                .propertyId(input.getPropertyId())
                                .status(input.getStatus())
                                .content(input.getContent())
                                .build();
                    });

            UUID result = engagementApplicationService
                    .createAgentCreatedPropertyLink(testPropertyId, testOwnerId, testAgentId);

            assertThat(result).isEqualTo(createdEngagementId);

            ArgumentCaptor<Engagement> engagementCaptor = ArgumentCaptor.forClass(Engagement.class);
            verify(engagementRepository).save(engagementCaptor.capture());
            Engagement saved = engagementCaptor.getValue();
            assertThat(saved.getInitiatorId()).isEqualTo(testOwnerId);
            assertThat(saved.getReceiverId()).isEqualTo(testAgentId);
            assertThat(saved.getPropertyId()).isEqualTo(testPropertyId);
            assertThat(saved.getEngagementType()).isEqualTo(EngagementType.AGENT_CREATED_PROPERTY_LINK);
            assertThat(saved.getStatus()).isEqualTo(EngagementStatus.ACCEPTED);
        }
    }
}
