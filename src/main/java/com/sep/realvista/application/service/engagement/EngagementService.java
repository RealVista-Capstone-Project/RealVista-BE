package com.sep.realvista.application.service.engagement;

import com.sep.realvista.application.engagement.dto.EngagementDto;
import com.sep.realvista.application.engagement.mapper.EngagementMapper;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.repository.EngagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EngagementService {

    private final EngagementRepository engagementRepository;
    private final EngagementMapper engagementMapper;

    @Transactional(readOnly = true)
    public List<EngagementDto> getMyEngagements(UUID userId) {
        List<Engagement> engagements = engagementRepository.findByInitiatorId(userId);
        return engagements.stream()
                .map(engagementMapper::toDto)
                .toList();
    }

    @Transactional
    public EngagementDto cancelEngagement(UUID engagementId, UUID userId) {
        Engagement engagement = engagementRepository.findById(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("Engagement", engagementId));

        if (!engagement.getInitiatorId().equals(userId)) {
            throw new BusinessConflictException("You are not authorized to cancel this engagement");
        }

        if (engagement.getStatus() != EngagementStatus.SUBMITTED) {
            throw new BusinessConflictException(
                    "Only SUBMITTED engagements can be cancelled. Current status: " + engagement.getStatus()
            );
        }

        engagement.cancel();
        Engagement saved = engagementRepository.save(engagement);
        return engagementMapper.toDto(saved);
    }
}
