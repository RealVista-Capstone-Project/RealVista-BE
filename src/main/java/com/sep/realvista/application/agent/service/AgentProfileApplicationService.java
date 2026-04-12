package com.sep.realvista.application.agent.service;

import com.sep.realvista.application.agent.dto.AgentProfileResponse;
import com.sep.realvista.application.agent.dto.UpdateAgentProfileRequest;
import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AgentProfileApplicationService {

    private final AgentProfileRepository agentProfileRepository;

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
}
