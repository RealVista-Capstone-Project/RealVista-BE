package com.sep.realvista.application.engagement.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.mapper.EngagementMapper;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final EngagementMapper engagementMapper;

    /**
     * Gets all hired agents for a property owner with pagination.
     *
     * Retrieves accepted engagements (AGENT_PROPOSAL where owner is receiver,
     * or OWNER_INVITATION where owner is initiator) and enriches them
     * with agent profile information.
     *
     * @param ownerId the authenticated owner's user ID
     * @param page    page number (0-indexed)
     * @param size    page size
     * @return paginated response of hired agent details
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "hiredAgents", key = "#ownerId + '_' + #page + '_' + #size")
    public PageResponse<HiredAgentResponse> getHiredAgents(UUID ownerId, int page, int size) {
        log.info("Getting hired agents for owner: {}, page: {}, size: {}", ownerId, page, size);

        PageRequest pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Page<Engagement> engagementPage = engagementRepository.findHiredAgentEngagements(ownerId, pageable);

        // Collect all agent user IDs from the engagements
        List<UUID> agentUserIds = engagementPage.getContent().stream()
                .map(this::resolveAgentUserId)
                .distinct()
                .collect(Collectors.toList());

        // Batch fetch all agent profiles
        Map<UUID, AgentProfile> agentProfileMap = agentProfileRepository.findByUserIds(agentUserIds)
                .stream()
                .collect(Collectors.toMap(AgentProfile::getUserId, Function.identity()));

        // Map engagements to response DTOs
        List<HiredAgentResponse> content = engagementPage.getContent().stream()
                .map(engagement -> {
                    UUID agentUserId = resolveAgentUserId(engagement);
                    User agentUser = resolveAgentUser(engagement);
                    AgentProfile agentProfile = agentProfileMap.get(agentUserId);
                    return engagementMapper.toHiredAgentResponse(engagement, agentUser, agentProfile);
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
