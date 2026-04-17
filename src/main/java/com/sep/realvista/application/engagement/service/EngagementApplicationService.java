package com.sep.realvista.application.engagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.CancelEngagementRequest;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.dto.EngagementSummaryResponse;
import com.sep.realvista.application.engagement.dto.AgentProposalApplyStateResponse;
import com.sep.realvista.application.engagement.mapper.EngagementMapper;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.application.engagement.dto.SendOwnerInvitationRequest;
import com.sep.realvista.application.engagement.dto.SubmitAgentProposalRequest;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.EmailService;
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
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for engagement operations.
 * Orchestrates business logic for managing engagements
 * between property owners and agents.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EngagementApplicationService {
    private static final Set<EngagementStatus> PROPOSAL_BLOCKING_STATUSES = Set.of(
            EngagementStatus.SUBMITTED,
            EngagementStatus.ACCEPTED
    );

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
    private final NotificationApplicationService notificationApplicationService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${spring.application.frontend.url}")
    private String frontendUrl;

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
     * @param userId the authenticated owner's user ID
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
     * @param userId the authenticated owner's user ID
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

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("title", proposalTemplate.getTitle());
        contentMap.put("commissionRate", proposalTemplate.getCommissionRate());
        contentMap.put("experienceYears", proposalTemplate.getExperienceYears());
        contentMap.put("pitchContent", proposalTemplate.getPitchContent());
        contentMap.put("specialty", proposalTemplate.getSpecialty());
        contentMap.put("priceRange", proposalTemplate.getPriceRange());
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

        notifyOwnerOfAgentProposal(saved, property, proposalTemplate, agentUserId);

        return saved.getEngagementId();
    }

    /**
     * Sends an owner invitation to a specific agent for a property.
     * Creates an OWNER_INVITATION engagement with status SUBMITTED.
     *
     * @param ownerUserId the owner's user ID (initiator)
     * @param request     agent ID, property ID, and optional message
     * @return the created engagement ID
     */
    @Transactional
    public UUID createOwnerInvitation(UUID ownerUserId, SendOwnerInvitationRequest request) {
        log.info("Owner {} sending invitation to agent {} for property {}",
                ownerUserId, request.getAgentId(), request.getPropertyId());

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

        if (!property.getOwnerId().equals(ownerUserId)) {
            throw new BusinessConflictException(
                    "You do not own this property", "NOT_PROPERTY_OWNER");
        }

        AgentProfile agentProfile = agentProfileRepository.findByUserId(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AgentProfile for user", request.getAgentId()));

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("message", request.getMessage() != null ? request.getMessage() : "");
        if (request.getTitle() != null) {
            contentMap.put("title", request.getTitle());
        }
        if (request.getOfferedCommission() != null) {
            contentMap.put("offeredCommission", request.getOfferedCommission());
        }

        Engagement engagement = Engagement.builder()
                .initiatorId(ownerUserId)
                .receiverId(agentProfile.getUserId())
                .engagementType(EngagementType.OWNER_INVITATION)
                .propertyId(property.getPropertyId())
                .status(EngagementStatus.SUBMITTED)
                .content(objectMapper.valueToTree(contentMap))
                .build();

        Engagement saved = engagementRepository.save(engagement);
        log.info("Owner invitation created. Engagement ID: {}", saved.getEngagementId());

        return saved.getEngagementId();
    }

    private void notifyOwnerOfAgentProposal(
            Engagement saved,
            Property property,
            AgentProposal proposalTemplate,
            UUID agentUserId) {
        UUID ownerId = property.getOwnerId();
        var ownerOpt = userRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            log.warn("Owner {} not found; skipping proposal notification for engagement {}", ownerId,
                    saved.getEngagementId());
            return;
        }
        User owner = ownerOpt.get();
        if (owner.getEmail() == null || owner.getEmail().getValue() == null
                || owner.getEmail().getValue().isBlank()) {
            log.warn("Owner {} has no email; skipping proposal notification for engagement {}", ownerId,
                    saved.getEngagementId());
            return;
        }

        String agentName = userRepository.findById(agentUserId)
                .map(User::getFullName)
                .filter(n -> n != null && !n.isBlank())
                .orElse("Môi giới");

        String proposalTitle = proposalTemplate.getTitle() != null ? proposalTemplate.getTitle() : "";
        String propertyAddress = property.getStreetAddress() != null ? property.getStreetAddress() : "";

        Map<String, String> metadata = new HashMap<>();
        metadata.put("engagementId", saved.getEngagementId().toString());
        metadata.put("propertyId", property.getPropertyId().toString());
        metadata.put("agentUserId", agentUserId.toString());

        String notifyTitle = "Đề xuất môi giới mới";
        String notifyMessage = String.format(
                "%s đã gửi đề xuất \"%s\" cho bất động sản của bạn tại %s.",
                agentName,
                proposalTitle.isBlank() ? "một đề xuất" : proposalTitle,
                propertyAddress.isBlank() ? "địa chỉ đã lưu" : propertyAddress);

        try {
            notificationApplicationService.sendNotification(
                    SendNotificationRequest.builder()
                            .userId(owner.getUserId())
                            .userEmail(owner.getEmail().getValue())
                            .title(notifyTitle)
                            .message(notifyMessage)
                            .eventType(EventType.NEW_AGENT_PROPOSAL)
                            .entityType(EntityType.PROPERTY)
                            .entityId(property.getPropertyId())
                            .metadata(metadata)
                            .build());
        } catch (Exception e) {
            log.error("Failed to send in-app/push notification to owner {} for engagement {}: {}",
                    owner.getUserId(), saved.getEngagementId(), e.getMessage(), e);
        }

        String engagementsUrl = frontendUrl != null ? frontendUrl.replaceAll("/$", "") + "/vi/dashboard/my-engagements"
                : "/vi/dashboard/my-engagements";

        try {
            Map<String, Object> emailVars = new HashMap<>();
            emailVars.put("ownerName", owner.getFullName());
            emailVars.put("agentName", agentName);
            emailVars.put("proposalTitle", proposalTitle.isBlank() ? "Đề xuất môi giới" : proposalTitle);
            emailVars.put("propertyAddress", propertyAddress.isBlank() ? "—" : propertyAddress);
            emailVars.put("viewEngagementsUrl", engagementsUrl);

            emailService.sendTemplateMessageAsync(
                    owner.getEmail().getValue(),
                    "Đề xuất môi giới mới trên RealVista",
                    "agent-proposal-notification",
                    emailVars);
            log.info("Queued agent proposal notification email to owner {}", owner.getEmail().getValue());
        } catch (Exception e) {
            log.error("Failed to queue agent proposal email to owner {}: {}",
                    owner.getEmail().getValue(), e.getMessage(), e);
        }
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
                                .toList();
                        imageUrls = images.stream()
                                .map(PropertyMedia::getMediaUrl)
                                .collect(Collectors.toList());
                        // Fallback thumbnail from first image
                        if (thumbnailUrl == null && !images.isEmpty()) {
                            PropertyMedia primary = images.getFirst();
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

        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            notifyAgentOfAgentProposalDecision(engagement, true, userId);
        }
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

        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            notifyAgentOfAgentProposalDecision(engagement, false, userId);
        }
    }

    private void notifyAgentOfAgentProposalDecision(
            Engagement engagement, boolean accepted, UUID ownerUserId) {
        UUID agentUserId = engagement.getInitiatorId();
        var agentOpt = userRepository.findById(agentUserId);
        if (agentOpt.isEmpty()) {
            log.warn("Agent {} not found; skipping proposal decision notification for engagement {}",
                    agentUserId, engagement.getEngagementId());
            return;
        }
        User agent = agentOpt.get();
        if (agent.getEmail() == null || agent.getEmail().getValue() == null
                || agent.getEmail().getValue().isBlank()) {
            log.warn("Agent {} has no email; skipping proposal decision notification for engagement {}",
                    agentUserId, engagement.getEngagementId());
            return;
        }

        String ownerName = userRepository.findById(ownerUserId)
                .map(User::getFullName)
                .filter(n -> n != null && !n.isBlank())
                .orElse("Chủ nhà");

        String proposalTitle = proposalTitleFromEngagementContent(engagement);
        String displayTitle = proposalTitle.isBlank() ? "Đề xuất môi giới" : proposalTitle;

        String propertyAddress = "—";
        UUID propertyId = engagement.getPropertyId();
        if (propertyId != null) {
            propertyAddress = propertyRepository.findById(propertyId)
                    .map(p -> p.getStreetAddress() != null && !p.getStreetAddress().isBlank()
                            ? p.getStreetAddress()
                            : "—")
                    .orElse("—");
        }

        EventType eventType = accepted ? EventType.AGENT_PROPOSAL_ACCEPTED : EventType.AGENT_PROPOSAL_REJECTED;
        String notifyTitle = accepted ? "Đề xuất được chấp nhận" : "Đề xuất bị từ chối";
        String notifyMessage = accepted
                ? String.format("%s đã chấp nhận đề xuất \"%s\" cho bất động sản tại %s.",
                ownerName, displayTitle, propertyAddress)
                : String.format("%s đã từ chối đề xuất \"%s\" cho bất động sản tại %s.",
                ownerName, displayTitle, propertyAddress);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("engagementId", engagement.getEngagementId().toString());
        metadata.put("ownerUserId", ownerUserId.toString());
        metadata.put("decision", accepted ? "ACCEPTED" : "REJECTED");
        if (propertyId != null) {
            metadata.put("propertyId", propertyId.toString());
        }

        EntityType entityType;
        UUID entityId;
        if (propertyId != null) {
            entityType = EntityType.PROPERTY;
            entityId = propertyId;
        } else {
            entityType = EntityType.USER;
            entityId = agent.getUserId();
        }

        try {
            notificationApplicationService.sendNotification(
                    SendNotificationRequest.builder()
                            .userId(agent.getUserId())
                            .userEmail(agent.getEmail().getValue())
                            .title(notifyTitle)
                            .message(notifyMessage)
                            .eventType(eventType)
                            .entityType(entityType)
                            .entityId(entityId)
                            .metadata(metadata)
                            .build());
        } catch (Exception e) {
            log.error("Failed to send proposal decision notification to agent {} for engagement {}: {}",
                    agent.getUserId(), engagement.getEngagementId(), e.getMessage(), e);
        }

        String engagementsUrl = frontendUrl != null ? frontendUrl.replaceAll("/$", "") + "/vi/dashboard/my-engagements"
                : "/vi/dashboard/my-engagements";

        try {
            Map<String, Object> emailVars = new HashMap<>();
            emailVars.put("agentName", agent.getFullName());
            emailVars.put("ownerName", ownerName);
            emailVars.put("proposalTitle", displayTitle);
            emailVars.put("propertyAddress", propertyAddress);
            emailVars.put("accepted", accepted);
            emailVars.put("viewEngagementsUrl", engagementsUrl);

            String emailSubject = accepted
                    ? "Đề xuất của bạn đã được chấp nhận - RealVista"
                    : "Đề xuất của bạn đã bị từ chối - RealVista";

            emailService.sendTemplateMessageAsync(
                    agent.getEmail().getValue(),
                    emailSubject,
                    "agent-proposal-decision-notification",
                    emailVars);
            log.info("Queued agent proposal decision email to agent {}", agent.getEmail().getValue());
        } catch (Exception e) {
            log.error("Failed to queue proposal decision email to agent {}: {}",
                    agent.getEmail().getValue(), e.getMessage(), e);
        }
    }

    private static String proposalTitleFromEngagementContent(Engagement engagement) {
        JsonNode content = engagement.getContent();
        if (content == null || !content.hasNonNull("title")) {
            return "";
        }
        String t = content.get("title").asText();
        return t != null ? t.trim() : "";
    }

    /**
     * Checks whether an agent can apply proposal for a property based on
     * the latest AGENT_PROPOSAL engagement status for that initiator, receiver, and property.
     *
     * @param initiatorId the agent user ID
     * @param receiverId the owner user ID
     * @param propertyId the property ID
     * @return proposal apply state (allowed or blocked) and latest status
     */
    @Transactional(readOnly = true)
    public AgentProposalApplyStateResponse getAgentProposalApplyState(
            UUID initiatorId, UUID receiverId, UUID propertyId) {
        log.info(
                "Checking proposal apply state for initiator: {}, receiver: {}, property: {}",
                initiatorId, receiverId, propertyId);

        Engagement latestEngagement = engagementRepository
                .findLatestAgentProposalEngagement(initiatorId, receiverId, propertyId)
                .orElse(null);

        EngagementStatus latestStatus = latestEngagement != null ? latestEngagement.getStatus() : null;
        boolean canApplyProposal = latestStatus == null || !PROPOSAL_BLOCKING_STATUSES.contains(latestStatus);

        return AgentProposalApplyStateResponse.builder()
                .canApplyProposal(canApplyProposal)
                .engagementStatus(latestStatus != null ? latestStatus.name() : null)
                .build();
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
