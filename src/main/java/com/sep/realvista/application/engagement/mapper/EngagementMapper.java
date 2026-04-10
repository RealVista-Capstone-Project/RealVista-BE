package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.dto.SoldListingInfo;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Engagement-related DTOs.
 */
@Mapper(componentModel = "spring")
public interface EngagementMapper {

    /**
     * Simple mapping from Engagement entity to EngagementDto.
     */
    EngagementDto toDto(Engagement engagement);
     * review status, listing thumbnail, and property attributes
     * to a HiredAgentResponse DTO.
     *
     * @param engagement           the engagement entity
     * @param agentUser            the agent's User entity
     * @param agentProfile         the agent's profile (may be null)
     * @param hasReview            whether this engagement has been reviewed
     * @param listingThumbnailUrl  thumbnail URL for the linked listing (may be null)
     * @param attributeValues      property attribute values (may be empty)
     * @return the hired agent response DTO
     */
    default HiredAgentResponse toHiredAgentResponse(
            Engagement engagement,
            User agentUser,
            AgentProfile agentProfile,
            boolean hasReview,
            String listingThumbnailUrl,
            List<PropertyAttributeValue> attributeValues) {

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

        // Listing info — build nested SoldListingInfo (nullable when no listing linked)
        Listing listing = engagement.getListing();
        if (listing != null) {
            String address = engagement.getProperty() != null
                    ? engagement.getProperty().getStreetAddress()
                    : null;

            List<PropertyAttributeDTO> attributes = attributeValues != null
                    ? attributeValues.stream()
                            .map(this::toAttributeDTO)
                            .collect(Collectors.toList())
                    : List.of();

            SoldListingInfo soldListing = SoldListingInfo.builder()
                    .listingId(listing.getListingId())
                    .title(listing.getName())
                    .price(listing.getPrice())
                    .imageUrl(listingThumbnailUrl)
                    .status(listing.getStatus() != null ? listing.getStatus().name() : null)
                    .listingType(listing.getListingType() != null ? listing.getListingType().name() : null)
                    .address(address)
                    .attributes(attributes)
                    .build();

            builder.soldListing(soldListing);
        }

        return builder.build();
    }

    /**
     * Maps a single PropertyAttributeValue to a PropertyAttributeDTO.
     * Reuses the same structure as GET /listings/:id attributes.
     */
    default PropertyAttributeDTO toAttributeDTO(PropertyAttributeValue attributeValue) {
        PropertyAttribute attribute = attributeValue.getPropertyAttribute();
        return PropertyAttributeDTO.builder()
                .attributeId(attribute.getPropertyAttributeId())
                .attributeCode(attribute.getCode())
                .attributeName(attribute.getName())
                .dataType(attribute.getDataType() != null ? attribute.getDataType().name() : null)
                .icon(attribute.getIcon())
                .unit(attribute.getUnit())
                .valueNumber(attributeValue.getValueNumber())
                .valueText(attributeValue.getValueText())
                .valueBoolean(attributeValue.getValueBoolean())
                .build();
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
