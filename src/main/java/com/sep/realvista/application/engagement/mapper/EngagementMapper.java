package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.engagement.dto.EngagementDto;
import com.sep.realvista.application.engagement.dto.EngagementSummaryResponse;
import com.sep.realvista.application.engagement.dto.HiredAgentResponse;
import com.sep.realvista.application.engagement.dto.SoldListingInfo;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EngagementMapper {

    private final ListingRepository listingRepository;

    public EngagementDto toDto(Engagement engagement) {
        if (engagement == null) {
            return null;
        }

        String listingTitle = "";
        String propertyAddress = "";
        String propertyImageUrl = "";

        if (engagement.getListingId() != null) {
            try {
                var listing = listingRepository.findById(engagement.getListingId()).orElse(null);
                if (listing != null) {
                    listingTitle = Optional.ofNullable(listing.getName()).orElse("");
                    if (listing.getProperty() != null) {
                        propertyAddress = Optional.ofNullable(listing.getProperty().getStreetAddress())
                                .orElse("");
                    }
                    propertyImageUrl = listingRepository
                            .findThumbnailByListingId(listing.getListingId())
                            .orElse("");
                }
            } catch (Exception ex) {
                log.warn(
                        "Failed to enrich engagement {} with listing {}: {}",
                        engagement.getEngagementId(),
                        engagement.getListingId(),
                        ex.toString());
            }
        }

        return EngagementDto.builder()
                .engagementId(engagement.getEngagementId())
                .initiatorId(engagement.getInitiatorId())
                .receiverId(engagement.getReceiverId())
                .engagementType(engagement.getEngagementType())
                .content(engagement.getContent() != null ? engagement.getContent().toString() : null)
                .listingId(engagement.getListingId())
                .propertyId(engagement.getPropertyId())
                .status(engagement.getStatus())
                .listingTitle(listingTitle)
                .propertyAddress(propertyAddress)
                .propertyImageUrl(propertyImageUrl)
                .createdAt(engagement.getCreatedAt())
                .updatedAt(engagement.getUpdatedAt())
                .build();
    }

    public HiredAgentResponse toHiredAgentResponse(
            Engagement engagement,
            User agentUser,
            AgentProfile agentProfile,
            boolean hasReview,
            String listingThumbnailUrl,
            List<PropertyAttributeValue> attributeValues) {

        if (engagement == null) {
            return null;
        }

        HiredAgentResponse.HiredAgentResponseBuilder builder = HiredAgentResponse.builder()
                .engagementId(engagement.getEngagementId())
                .engagementType(engagement.getEngagementType() != null
                        ? engagement.getEngagementType().name() : null)
                .status(engagement.getStatus() != null ? engagement.getStatus().name() : null)
                .hiredAt(engagement.getCreatedAt())
                .hasReview(hasReview)
                .cancellationReason(engagement.getCancellationReason())
                .content(engagement.getContent())
                .propertyId(engagement.getPropertyId());

        // Initiator / Receiver for list display
        builder.initiatorId(engagement.getInitiatorId())
                .receiverId(engagement.getReceiverId());
        var initiator = engagement.getInitiator();
        if (initiator != null) {
            builder.initiatorName(initiator.getFirstName() + " " + initiator.getLastName());
        }
        var receiver = engagement.getReceiver();
        if (receiver != null) {
            builder.receiverName(receiver.getFirstName() + " " + receiver.getLastName())
                    .receiverAvatarUrl(receiver.getAvatarUrl());
        }

        if (agentUser != null) {
            builder.agentUserId(agentUser.getUserId())
                    .agentFullName(agentUser.getFirstName() + " " + agentUser.getLastName())
                    .agentAvatarUrl(agentUser.getAvatarUrl())
                    .agentPhone(agentUser.getPhone())
                    .agentEmail(agentUser.getEmail() != null ? agentUser.getEmail().toString() : null);
        }

        if (agentProfile != null) {
            builder.agentBio(agentProfile.getBio())
                    .agentSpecialties(agentProfile.getSpecialties())
                    .agentServiceAreas(agentProfile.getServiceAreas())
                    .agentRating(agentProfile.getRating())
                    .agentYearsOfExperience(agentProfile.getYearsOfExperience())
                    .agentPropertiesSold(agentProfile.getPropertiesSold());
        }

        var property = engagement.getProperty();
        if (property != null) {
            builder.propertyAddress(property.getStreetAddress());
            if (property.getPropertyType() != null) {
                builder.propertyTypeName(property.getPropertyType().getName());
            }
            if (property.getLocation() != null) {
                builder.propertyLocationName(property.getLocation().getName());
            }
        }

        var listing = engagement.getListing();
        if (listing != null) {
            List<PropertyAttributeDTO> attrDtos = attributeValues.stream()
                    .map(av -> {
                        var attr = av.getPropertyAttribute();
                        return PropertyAttributeDTO.builder()
                                .attributeId(av.getPropertyAttributeId())
                                .attributeCode(attr != null ? attr.getCode() : null)
                                .attributeName(attr != null ? attr.getName() : null)
                                .dataType(attr != null && attr.getDataType() != null
                                        ? attr.getDataType().name() : null)
                                .icon(attr != null ? attr.getIcon() : null)
                                .unit(attr != null ? attr.getUnit() : null)
                                .valueNumber(av.getValueNumber())
                                .valueText(av.getValueText())
                                .valueBoolean(av.getValueBoolean())
                                .build();
                    })
                    .collect(Collectors.toList());

            builder.soldListing(SoldListingInfo.builder()
                    .listingId(listing.getListingId())
                    .title(listing.getName())
                    .price(listing.getPrice())
                    .imageUrl(listingThumbnailUrl)
                    .status(listing.getStatus() != null ? listing.getStatus().name() : null)
                    .listingType(listing.getListingType() != null
                            ? listing.getListingType().name() : null)
                    .address(property != null ? property.getStreetAddress() : null)
                    .attributes(attrDtos)
                    .build());
        }

        return builder.build();
    }

    public EngagementSummaryResponse toSummaryResponse(
            Engagement engagement,
            User agentUser,
            String listingThumbnailUrl,
            List<String> propertyMediaUrls) {

        if (engagement == null) {
            return null;
        }

        EngagementSummaryResponse.EngagementSummaryResponseBuilder builder =
                EngagementSummaryResponse.builder()
                        .engagementId(engagement.getEngagementId())
                        .engagementType(engagement.getEngagementType() != null
                                ? engagement.getEngagementType().name() : null)
                        .status(engagement.getStatus() != null ? engagement.getStatus().name() : null)
                        .content(engagement.getContent())
                        .propertyId(engagement.getPropertyId())
                        .createdAt(engagement.getCreatedAt())
                        .updatedAt(engagement.getUpdatedAt());

        // Initiator / Receiver
        builder.initiatorId(engagement.getInitiatorId())
                .receiverId(engagement.getReceiverId());
        var initiator = engagement.getInitiator();
        if (initiator != null) {
            builder.initiatorName(initiator.getFirstName() + " " + initiator.getLastName());
        }
        var receiver = engagement.getReceiver();
        if (receiver != null) {
            builder.receiverName(receiver.getFirstName() + " " + receiver.getLastName())
                    .receiverAvatarUrl(receiver.getAvatarUrl());
        }

        // Agent summary
        if (agentUser != null) {
            builder.agentUserId(agentUser.getUserId())
                    .agentFullName(agentUser.getFirstName() + " " + agentUser.getLastName())
                    .agentAvatarUrl(agentUser.getAvatarUrl());
        }

        // Property
        var property = engagement.getProperty();
        if (property != null) {
            builder.propertyAddress(property.getStreetAddress());
            if (property.getPropertyType() != null) {
                builder.propertyTypeName(property.getPropertyType().getName());
            }
            if (property.getLocation() != null) {
                builder.propertyLocationName(property.getLocation().getName());
            }
        }

        // Listing title + thumbnail + media
        var listing = engagement.getListing();
        if (listing != null) {
            builder.listingTitle(listing.getName());
        }
        builder.propertyImageUrl(listingThumbnailUrl);
        builder.propertyMediaUrls(propertyMediaUrls);

        return builder.build();
    }
}
