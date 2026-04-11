package com.sep.realvista.application.engagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.CancelEngagementRequest;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.dto.EngagementSummaryResponse;
import com.sep.realvista.application.engagement.mapper.EngagementMapper;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.application.engagement.dto.SubmitAgentProposalRequest;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for engagement operations.
 *
 * Orchestrates business logic for managing engagements
 * between property owners and agents.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EngagementApplicationService {

    private final EngagementRepository engagementRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final AgentReviewRepository agentReviewRepository;
    private final EngagementMapper engagementMapper;
    private final ListingRepository listingRepository;
    private final PropertyAttributeValueRepository propertyAttributeValueRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyMediaRepository propertyMediaRepository;
    private final AgentProposalRepository agentProposalRepository;
    private final ObjectMapper objectMapper;

    /**
     * Gets hired agents for a property owner with pagination, optional status filter, and search.
     *
     * @param ownerId the authenticated owner's user ID
     * @param status  optional status filter (ACCEPTED, FINISHED, CANCELLED)
     * @param search  optional search query for agent name
     * @param page    page number (0-indexed)
     * @param size    page size
     * @return paginated response of hired agent details
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "hiredAgents", key = "#ownerId + '_' + #status + '_' + #search + '_' + #page + '_' + #size")
    public PageResponse<HiredAgentResponse> getHiredAgents(
            UUID ownerId, String status, String search, int page, int size) {
        log.info("Getting hired agents for owner: {}, status: {}, search: {}, page: {}, size: {}",
                ownerId, status, search, page, size);

        PageRequest pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        // Normalize search
        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        Page<Engagement> engagementPage;
        if (status != null && !status.isBlank()) {
            EngagementStatus engagementStatus;
            try {
                engagementStatus = EngagementStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessConflictException(
                        "Invalid engagement status: " + status, "INVALID_STATUS");
            }
            engagementPage = engagementRepository.findHiredAgentEngagements(
                    ownerId, engagementStatus, normalizedSearch, pageable);
        } else {
            engagementPage = engagementRepository.findAllHiredAgentEngagements(
                    ownerId, normalizedSearch, pageable);
        }

        // Collect all agent user IDs from the engagements
        List<UUID> agentUserIds = engagementPage.getContent().stream()
                .map(this::resolveAgentUserId)
                .distinct()
                .collect(Collectors.toList());

        // Batch fetch all agent profiles
        Map<UUID, AgentProfile> agentProfileMap = agentProfileRepository.findByUserIds(agentUserIds)
                .stream()
                .collect(Collectors.toMap(AgentProfile::getUserId, Function.identity()));

        // Batch check which engagements have reviews
        List<UUID> engagementIds = engagementPage.getContent().stream()
                .map(Engagement::getEngagementId)
                .collect(Collectors.toList());
        Set<UUID> reviewedEngagementIds = Set.copyOf(
                agentReviewRepository.findReviewedEngagementIds(engagementIds));

        // Map engagements to response DTOs
        List<HiredAgentResponse> content = engagementPage.getContent().stream()
                .map(engagement -> {
                    UUID agentUserId = resolveAgentUserId(engagement);
                    User agentUser = resolveAgentUser(engagement);
                    AgentProfile agentProfile = agentProfileMap.get(agentUserId);
                    boolean hasReview = reviewedEngagementIds.contains(engagement.getEngagementId());
                    return engagementMapper.toHiredAgentResponse(
                            engagement, agentUser, agentProfile, hasReview,
                            null, List.of());
                })
                .collect(Collectors.toList());

        log.info("Retrieved {} hired agents for owner: {}", content.size(), ownerId);

        return PageResponse.<HiredAgentResponse>builder()
                .content(content)
                .page(engagementPage.getNumber())
                .size(engagementPage.getSize())
                .totalElements(engagementPage.getTotalElements())
                .totalPages(engagementPage.getTotalPages())
                .first(engagementPage.isFirst())
                .last(engagementPage.isLast())
                .build();
    }

    /**
     * Gets a single engagement by ID, scoped to the authenticated owner.
     *
     * <p>Uses a single optimized JPQL query with JOIN FETCH to load all associated
     * entities (initiator, receiver, property, property type, location) in one round-trip,
     * avoiding the N+1 lazy-loading pattern. Only two additional queries are needed:
     * one for the agent profile and one to check if a review already exists.
     *
     * @param engagementId the engagement ID
     * @param ownerId      the authenticated owner's user ID
     * @return the full engagement detail response
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "engagement", key = "#engagementId + '_' + #ownerId")
    public HiredAgentResponse getEngagementById(UUID engagementId, UUID ownerId) {
        log.info("Getting engagement detail: {} for owner: {}", engagementId, ownerId);

        Engagement engagement = engagementRepository.findByIdWithFetches(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("Engagement", engagementId));

        validateOwnership(engagement, ownerId);

        User agentUser = resolveAgentUser(engagement);
        UUID agentUserId = resolveAgentUserId(engagement);
        AgentProfile agentProfile = agentProfileRepository.findByUserId(agentUserId).orElse(null);
        boolean hasReview = agentReviewRepository.existsByEngagementId(engagementId);

        String listingThumbnailUrl = null;
        List<PropertyAttributeValue> attributeValues = List.of();
        if (engagement.getListingId() != null) {
            listingThumbnailUrl = listingRepository
                    .findThumbnailByListingId(engagement.getListingId())
                    .orElse(null);
            if (engagement.getPropertyId() != null) {
                attributeValues = propertyAttributeValueRepository
                        .findByPropertyIdWithAttribute(engagement.getPropertyId());
            }
        }

        log.info("Retrieved engagement detail: {} for owner: {}", engagementId, ownerId);

        return engagementMapper.toHiredAgentResponse(
                engagement, agentUser, agentProfile, hasReview,
                listingThumbnailUrl, attributeValues);
    }

    /**
     * Finishes an engagement (marks contract as completed).
     * Only ACCEPTED engagements can be finished.
     *
     * @param engagementId the engagement ID
     * @param ownerId the authenticated owner's user ID
     */
    @Caching(evict = {
            @CacheEvict(value = "hiredAgents", allEntries = true),
            @CacheEvict(value = "engagement", allEntries = true)
    })
    public void finishEngagement(UUID engagementId, UUID userId) {
        log.info("Finishing engagement: {} by user: {}", engagementId, userId);

        Engagement engagement = findEngagementOrThrow(engagementId);
        validateParticipant(engagement, userId);

        engagement.finish();
        engagementRepository.save(engagement);

        log.info("Engagement {} finished successfully", engagementId);
    }

    /**
     * Cancels an engagement.
     * ACCEPTED engagements require a cancellation reason.
     * SUBMITTED engagements can be cancelled without a reason.
     *
     * @param engagementId the engagement ID
     * @param ownerId the authenticated owner's user ID
     * @param request the cancellation request containing the reason
     */
    @Caching(evict = {
            @CacheEvict(value = "hiredAgents", allEntries = true),
            @CacheEvict(value = "engagement", allEntries = true)
    })
    public void cancelEngagement(UUID engagementId, UUID userId, CancelEngagementRequest request) {
        log.info("Cancelling engagement: {} by user: {}", engagementId, userId);

        Engagement engagement = findEngagementOrThrow(engagementId);
        validateParticipant(engagement, userId);

        engagement.cancel(request != null ? request.getReason() : null);
        engagementRepository.save(engagement);

        log.info("Engagement {} cancelled successfully", engagementId);
    }

    /**
     * Submits an agent proposal for a specific property.
     *
     * <p>Initiates a new engagement between the logged-in agent and the property owner.
     * The proposal content (template) is cloned into the engagement blob.
     *
     * @param agentUserId   the ID of the agent submitting the proposal
     * @param request       the submission payload (proposal template ID and property ID)
     * @return the newly created engagement ID
     */
    @Caching(evict = {
            @CacheEvict(value = "hiredAgents", allEntries = true)
    })
    public UUID submitAgentProposal(UUID agentUserId, SubmitAgentProposalRequest request) {
        log.info("Submitting agent proposal: {} for property: {} by agent: {}",
                request.getAgentProposalId(), request.getPropertyId(), agentUserId);

        // 1. Fetch and validate agent proposal template
        AgentProposal proposalTemplate = agentProposalRepository.findById(request.getAgentProposalId())
                .orElseThrow(() -> new ResourceNotFoundException("AgentProposal", request.getAgentProposalId()));

        if (!proposalTemplate.getUserId().equals(agentUserId)) {
            throw new BusinessConflictException(
                    "You do not own this proposal template",
                    "PROPOSAL_TEMPLATE_NOT_OWNED");
        }

        // 2. Fetch and validate target property
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

        // Cannot propose to self-owned property (edge case)
        if (property.getOwnerId().equals(agentUserId)) {
            throw new BusinessConflictException(
                    "You cannot submit a proposal to your own property",
                    "CANNOT_PROPOSE_TO_SELF");
        }

        Map<String, Object> contentMap = new java.util.HashMap<>();
        contentMap.put("title", proposalTemplate.getTitle());
        contentMap.put("commissionRate", proposalTemplate.getCommissionRate());
        contentMap.put("experienceYears", proposalTemplate.getExperienceYears());
        contentMap.put("pitchContent", proposalTemplate.getPitchContent());
        contentMap.put("message", request.getMessage() != null ? request.getMessage() : "");

        // 4. Create the engagement
        Engagement engagement = Engagement.builder()
                .initiatorId(agentUserId)
                .receiverId(property.getOwnerId())
                .engagementType(EngagementType.AGENT_PROPOSAL)
                .propertyId(property.getPropertyId())
                .status(EngagementStatus.SUBMITTED)
                .content(objectMapper.valueToTree(contentMap))
                .build();

        Engagement saved = engagementRepository.save(engagement);

        log.info("Agent proposal submitted successfully. New engagement ID: {}", saved.getEngagementId());

        return saved.getEngagementId();
    }

    /**
     * Gets all engagements for the authenticated user (as initiator or receiver),
     * with optional search.
     *
     * @param userId the authenticated user's ID
     * @param search optional search query for agent name
     * @return list of engagements
     */
    @Transactional(readOnly = true)
    public List<EngagementSummaryResponse> getMyEngagements(UUID userId, String search) {
        log.info("Getting engagements for user: {}, search: {}", userId, search);

        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        List<Engagement> engagements = engagementRepository.findByParticipantWithFetches(userId, normalizedSearch);

        return engagements.stream()
                .map(engagement -> {
                    User agentUser = resolveAgentUser(engagement);
                    String thumbnailUrl = null;
                    List<String> imageUrls = List.of();

                    if (engagement.getListingId() != null) {
                        thumbnailUrl = listingRepository
                                .findThumbnailByListingId(engagement.getListingId())
                                .orElse(null);
                    }

                    // Get property image media (filter IMAGE only, skip video/3D)
                    if (engagement.getPropertyId() != null) {
                        List<PropertyMedia> propertyMediaList =
                                propertyMediaRepository.findByPropertyId(engagement.getPropertyId());
                        List<PropertyMedia> images = propertyMediaList.stream()
                                .filter(pm -> pm.getMediaType() == MediaType.IMAGE)
                                .collect(Collectors.toList());
                        imageUrls = images.stream()
                                .map(PropertyMedia::getMediaUrl)
                                .collect(Collectors.toList());
                        // Fallback thumbnail from first image
                        if (thumbnailUrl == null && !images.isEmpty()) {
                            PropertyMedia primary = images.get(0);
                            thumbnailUrl = primary.getThumbnailUrl() != null
                                    ? primary.getThumbnailUrl()
                                    : primary.getMediaUrl();
                        }
                    }

                    return engagementMapper.toSummaryResponse(
                            engagement, agentUser, thumbnailUrl, imageUrls);
                })
                .collect(Collectors.toList());
    }

    /**
     * Accepts a SUBMITTED engagement.
     * Only the receiving party can accept.
     *
     * @param engagementId the engagement ID
     * @param userId the authenticated user's ID
     */
    @Caching(evict = {
            @CacheEvict(value = "hiredAgents", allEntries = true),
            @CacheEvict(value = "engagement", allEntries = true)
    })
    public void acceptEngagement(UUID engagementId, UUID userId) {
        log.info("Accepting engagement: {} by user: {}", engagementId, userId);

        Engagement engagement = findEngagementOrThrow(engagementId);

        if (!engagement.getReceiverId().equals(userId)) {
            throw new BusinessConflictException(
                    "You are not authorized to accept this engagement",
                    "ENGAGEMENT_NOT_AUTHORIZED");
        }

        engagement.accept();
        engagementRepository.save(engagement);

        log.info("Engagement {} accepted successfully", engagementId);
    }

    /**
     * Rejects a SUBMITTED engagement.
     * Only the receiving party can reject.
     *
     * @param engagementId the engagement ID
     * @param userId the authenticated user's ID
     */
    @Caching(evict = {
            @CacheEvict(value = "hiredAgents", allEntries = true),
            @CacheEvict(value = "engagement", allEntries = true)
    })
    public void rejectEngagement(UUID engagementId, UUID userId) {
        log.info("Rejecting engagement: {} by user: {}", engagementId, userId);

        Engagement engagement = findEngagementOrThrow(engagementId);

        if (!engagement.getReceiverId().equals(userId)) {
            throw new BusinessConflictException(
                    "You are not authorized to reject this engagement",
                    "ENGAGEMENT_NOT_AUTHORIZED");
        }

        engagement.reject();
        engagementRepository.save(engagement);

        log.info("Engagement {} rejected successfully", engagementId);
    }

    /**
     * Finds an engagement by ID or throws ResourceNotFoundException.
     */
    private Engagement findEngagementOrThrow(UUID engagementId) {
        return engagementRepository.findById(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("Engagement", engagementId));
    }

    /**
     * Validates that the given owner has ownership of the engagement.
     * For AGENT_PROPOSAL: owner is the receiver.
     * For OWNER_INVITATION: owner is the initiator.
     */
    private void validateOwnership(Engagement engagement, UUID ownerId) {
        boolean isOwner;
        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            isOwner = engagement.getReceiverId().equals(ownerId);
        } else {
            isOwner = engagement.getInitiatorId().equals(ownerId);
        }

        if (!isOwner) {
            throw new BusinessConflictException(
                    "You are not authorized to manage this engagement",
                    "ENGAGEMENT_NOT_OWNED");
        }
    }

    /**
     * Validates that the given user is a participant (initiator or receiver) of the engagement.
     */
    private void validateParticipant(Engagement engagement, UUID userId) {
        boolean isParticipant = engagement.getInitiatorId().equals(userId)
                || engagement.getReceiverId().equals(userId);
        if (!isParticipant) {
            throw new BusinessConflictException(
                    "You are not authorized to manage this engagement",
                    "ENGAGEMENT_NOT_AUTHORIZED");
        }
    }

    /**
     * Resolves the agent user ID from an engagement based on engagement type.
     */
    private UUID resolveAgentUserId(Engagement engagement) {
        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            return engagement.getInitiatorId();
        } else {
            return engagement.getReceiverId();
        }
    }

    /**
     * Resolves the agent User entity from an engagement based on engagement type.
     * Relies on the Engagement entity having eagerly fetched initiator and receiver.
     */
    private User resolveAgentUser(Engagement engagement) {
        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            return engagement.getInitiator();
        } else {
            return engagement.getReceiver();
        }
    }
}
