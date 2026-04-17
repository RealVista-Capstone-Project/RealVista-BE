package com.sep.realvista.application.agent.service;

import com.sep.realvista.application.agent.dto.AgentListItemResponse;
import com.sep.realvista.application.agent.dto.AgentProfileResponse;
import com.sep.realvista.application.agent.dto.UpdateAgentProfileRequest;
import com.sep.realvista.application.engagement.dto.ReviewResponse;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.agent.AgentReview;
import com.sep.realvista.domain.agent.AgentReviewRepository;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AgentProfileApplicationService {

    private final AgentProfileRepository agentProfileRepository;
    private final EngagementRepository engagementRepository;
    private final AgentReviewRepository agentReviewRepository;

    @Transactional(readOnly = true)
    public AgentProfileResponse getMine(UUID userId) {
        AgentProfile profile = agentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentProfile for user", userId));
        return toResponse(profile);
    }

    public AgentProfileResponse updateMine(UUID userId, UpdateAgentProfileRequest request) {
        AgentProfile profile = agentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentProfile for user", userId));

        if (request.getBio() != null) {
            profile.updateBio(request.getBio());
        }
        if (request.getSpecialties() != null) {
            profile.updateSpecialties(request.getSpecialties());
        }
        if (request.getServiceAreas() != null) {
            profile.updateServiceAreas(request.getServiceAreas());
        }
        if (request.getYearsOfExperience() != null) {
            profile.updateYearsOfExperience(request.getYearsOfExperience());
        }

        AgentProfile saved = agentProfileRepository.save(profile);
        log.debug("Updated agent profile for user {}", userId);
        return toResponse(saved);
    }

    /**
     * Lists all agents with their engagement status for a specific property.
     * When no propertyId is given, engagement fields will be null.
     *
     * @param propertyId optional property ID to enrich with engagement status
     * @param ownerId    the requesting owner's user ID (excluded from results)
     * @return list of agent list items
     */
    @Transactional(readOnly = true)
    public List<AgentListItemResponse> listAgentsForProperty(
            UUID propertyId, UUID ownerId, String search, BigDecimal minRating) {
        List<AgentProfile> allAgents = agentProfileRepository.findAllActive();

        // Build a map of agentUserId -> latest engagement for this property
        Map<UUID, Engagement> engagementByAgent = new HashMap<>();
        if (propertyId != null) {
            List<Engagement> engagements = engagementRepository
                    .findActiveEngagementsForProperty(propertyId);
            for (Engagement e : engagements) {
                UUID agentId = resolveAgentId(e);
                engagementByAgent.putIfAbsent(agentId, e);
            }
        }

        String searchLower = (search != null && !search.isBlank())
                ? search.trim().toLowerCase() : null;

        return allAgents.stream()
                .filter(ap -> !ap.getUserId().equals(ownerId))
                .filter(ap -> {
                    if (searchLower == null) {
                        return true;
                    }
                    String name = ap.getUser() != null && ap.getUser().getFullName() != null
                            ? ap.getUser().getFullName().toLowerCase() : "";
                    return name.contains(searchLower);
                })
                .filter(ap -> {
                    if (minRating == null) {
                        return true;
                    }
                    return ap.getRating() != null
                            && ap.getRating().compareTo(minRating) >= 0;
                })
                .map(ap -> toListItem(ap, engagementByAgent.get(ap.getUserId())))
                .collect(Collectors.toList());
    }

    private UUID resolveAgentId(Engagement e) {
        if (e.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            return e.getInitiatorId(); // agent initiated
        }
        return e.getReceiverId(); // owner invited → agent is receiver
    }

    private static AgentListItemResponse toListItem(AgentProfile p, Engagement engagement) {
        String avatarUrl = null;
        String fullName = null;
        if (p.getUser() != null) {
            avatarUrl = p.getUser().getAvatarUrl();
            fullName = p.getUser().getFullName();
        }
        return AgentListItemResponse.builder()
                .userId(p.getUserId())
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .bio(p.getBio())
                .specialties(p.getSpecialties())
                .serviceAreas(p.getServiceAreas())
                .rating(p.getRating())
                .yearsOfExperience(p.getYearsOfExperience())
                .propertiesSold(p.getPropertiesSold())
                .engagementStatus(engagement != null ? engagement.getStatus().name() : null)
                .engagementId(engagement != null ? engagement.getEngagementId() : null)
                .engagementType(engagement != null ? engagement.getEngagementType().name() : null)
                .build();
    }

    private static AgentProfileResponse toResponse(AgentProfile p) {
        return AgentProfileResponse.builder()
                .agentProfileId(p.getAgentProfileId())
                .userId(p.getUserId())
                .bio(p.getBio())
                .specialties(p.getSpecialties())
                .serviceAreas(p.getServiceAreas())
                .rating(p.getRating())
                .yearsOfExperience(p.getYearsOfExperience())
                .propertiesSold(p.getPropertiesSold())
                .build();
    }

    /**
     * Returns the review list for a given agent user ID.
     *
     * @param agentUserId the agent's user ID
     * @return list of reviews ordered newest first
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForAgent(UUID agentUserId) {
        AgentProfile agentProfile = agentProfileRepository.findByUserId(agentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agent profile not found for user: " + agentUserId));
        List<AgentReview> reviews =
                agentReviewRepository.findByAgentProfileId(agentProfile.getAgentProfileId());
        return reviews.stream()
                .map(r -> ReviewResponse.builder()
                        .reviewId(r.getAgentReviewId())
                        .engagementId(r.getEngagementId())
                        .agentUserId(agentUserId)
                        .reviewerId(r.getReviewerId())
                        .rating(r.getRating())
                        .comment(r.getReview())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
