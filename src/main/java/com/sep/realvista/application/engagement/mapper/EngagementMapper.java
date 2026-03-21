package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Engagement-related DTOs.
 */
@Mapper(componentModel = "spring")
public interface EngagementMapper {

    /**
     * Maps an Engagement with its associated agent User and AgentProfile
     * to a HiredAgentResponse DTO.
     *
     * @param engagement the engagement entity
     * @param agentUser the agent's User entity
     * @param agentProfile the agent's profile (may be null)
     * @return the hired agent response DTO
     */
    /**
     * Maps an Engagement with its associated agent User, AgentProfile,
     * and review status to a HiredAgentResponse DTO.
     *
     * @param engagement the engagement entity
     * @param agentUser the agent's User entity
     * @param agentProfile the agent's profile (may be null)
     * @param hasReview whether this engagement has been reviewed
     * @return the hired agent response DTO
     */
    default HiredAgentResponse toHiredAgentResponse(
            Engagement engagement,
            User agentUser,
            AgentProfile agentProfile,
            boolean hasReview) {

        if (engagement == null) {
            return null;
        }
        if (agentUser == null) {
            throw new ResourceNotFoundException("User", resolveAgentUserId(engagement));
        }

        HiredAgentResponse.HiredAgentResponseBuilder builder = HiredAgentResponse.builder()
                .agentUserId(agentUser.getUserId())
                .agentFullName(agentUser.getFullName())
                .agentAvatarUrl(agentUser.getAvatarUrl())
                .agentPhone(agentUser.getPhone())
                .agentEmail(agentUser.getEmail() != null ? agentUser.getEmail().getValue() : null)
                .engagementId(engagement.getEngagementId())
                .engagementType(engagement.getEngagementType().name())
                .status(engagement.getStatus().name())
                .hiredAt(engagement.getUpdatedAt())
                .hasReview(hasReview)
                .cancellationReason(engagement.getCancellationReason())
                .content(engagement.getContent());

        // Agent profile info (may be null if profile not yet created)
        if (agentProfile != null) {
            builder.agentBio(agentProfile.getBio())
                   .agentSpecialties(agentProfile.getSpecialties())
                   .agentServiceAreas(agentProfile.getServiceAreas())
                   .agentRating(agentProfile.getRating())
                   .agentYearsOfExperience(agentProfile.getYearsOfExperience())
                   .agentPropertiesSold(agentProfile.getPropertiesSold());
        }

        // Property info
        if (engagement.getProperty() != null) {
            var property = engagement.getProperty();
            builder.propertyId(property.getPropertyId())
                   .propertyAddress(property.getStreetAddress());

            if (property.getPropertyType() != null) {
                builder.propertyTypeName(property.getPropertyType().getName());
            }
            if (property.getLocation() != null) {
                builder.propertyLocationName(property.getLocation().getName());
            }
        }

        return builder.build();
    }

    /**
     * Resolves the agent's user ID from an engagement based on engagement type.
     * For AGENT_PROPOSAL: agent is the initiator.
     * For OWNER_INVITATION: agent is the receiver.
     *
     * @param engagement the engagement
     * @return the agent's user ID
     */
    @Named("resolveAgentUserId")
    default java.util.UUID resolveAgentUserId(Engagement engagement) {
        if (engagement.getEngagementType() == EngagementType.AGENT_PROPOSAL) {
            return engagement.getInitiatorId();
        } else {
            return engagement.getReceiverId();
        }
    }
}
