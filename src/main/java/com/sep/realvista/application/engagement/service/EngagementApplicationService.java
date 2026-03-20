package com.sep.realvista.application.engagement.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.CancelEngagementRequest;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.mapper.EngagementMapper;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
                            engagement, agentUser, agentProfile, hasReview);
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
     * Finishes an engagement (marks contract as completed).
     * Only ACCEPTED engagements can be finished.
     *
     * @param engagementId the engagement ID
     * @param ownerId the authenticated owner's user ID
     */
    @CacheEvict(value = "hiredAgents", allEntries = true)
    public void finishEngagement(UUID engagementId, UUID ownerId) {
        log.info("Finishing engagement: {} by owner: {}", engagementId, ownerId);

        Engagement engagement = findEngagementOrThrow(engagementId);
        validateOwnership(engagement, ownerId);

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
    @CacheEvict(value = "hiredAgents", allEntries = true)
    public void cancelEngagement(UUID engagementId, UUID ownerId, CancelEngagementRequest request) {
        log.info("Cancelling engagement: {} by owner: {}", engagementId, ownerId);

        Engagement engagement = findEngagementOrThrow(engagementId);
        validateOwnership(engagement, ownerId);

        engagement.cancel(request != null ? request.getReason() : null);
        engagementRepository.save(engagement);

        log.info("Engagement {} cancelled successfully", engagementId);
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
