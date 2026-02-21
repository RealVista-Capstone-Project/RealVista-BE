package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.listing.dto.TenantRentalProfileDto;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfile;
import org.springframework.stereotype.Component;

@Component
public class TenantRentalProfileMapper {

    public TenantRentalProfileDto toDto(TenantRentalProfile profile) {
        if (profile == null) {
            return null;
        }

        return TenantRentalProfileDto.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUserId())
                .title(profile.getTitle())
                .monthlyIncome(profile.getMonthlyIncome())
                .moveInDate(profile.getMoveInDate())
                .leaseTermMonths(profile.getLeaseTermMonths())
                .note(profile.getNote())
                .isActive(profile.getIsActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
