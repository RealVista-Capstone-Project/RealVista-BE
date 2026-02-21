package com.sep.realvista.application.service.engagement;

import com.sep.realvista.application.engagement.mapper.TenantRentalProfileMapper;
import com.sep.realvista.application.listing.dto.TenantRentalProfileDto;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfile;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantRentalProfileService {

    private final TenantRentalProfileRepository tenantRentalProfileRepository;
    private final TenantRentalProfileMapper tenantRentalProfileMapper;

    @Transactional(readOnly = true)
    public List<TenantRentalProfileDto> getMyProfiles(UUID userId) {
        return tenantRentalProfileRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(tenantRentalProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TenantRentalProfileDto createProfile(TenantRentalProfileDto dto, UUID userId) {
        TenantRentalProfile profile = TenantRentalProfile.builder()
                .userId(userId)
                .title(dto.getTitle())
                .monthlyIncome(dto.getMonthlyIncome())
                .moveInDate(dto.getMoveInDate())
                .leaseTermMonths(dto.getLeaseTermMonths())
                .note(dto.getNote())
                .isActive(true)
                .build();
        
        return tenantRentalProfileMapper.toDto(tenantRentalProfileRepository.save(profile));
    }
}
