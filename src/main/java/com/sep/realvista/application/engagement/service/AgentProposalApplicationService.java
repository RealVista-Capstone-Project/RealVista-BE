package com.sep.realvista.application.engagement.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.engagement.dto.AgentProposalDto;
import com.sep.realvista.application.engagement.dto.ApplyAgentProposalRequest;
import com.sep.realvista.application.engagement.mapper.AgentProposalMapper;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.DomainException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentProposalApplicationService {

    private final AgentProposalRepository agentProposalRepository;
    private final AgentProposalMapper agentProposalMapper;
    private final PropertyTypeRepository propertyTypeRepository;
    private final AgentProfileRepository agentProfileRepository;

    @Transactional
    public AgentProposalDto createProposal(UUID userId, ApplyAgentProposalRequest request) {
        AgentProposalStatus targetStatus = request.getStatus() != null
                ? request.getStatus()
                : AgentProposalStatus.ACTIVE;
        if (targetStatus == AgentProposalStatus.ARCHIVED) {
            throw new DomainException(
                    "Cannot create a proposal in ARCHIVED status",
                    "ERROR_PROPOSAL_CANNOT_CREATE_ARCHIVED");
        }
        boolean draft = targetStatus == AgentProposalStatus.DRAFT;
        validateApplyRequest(request, draft);

        String title = request.getTitle().trim();
        log.info("Agent {} is creating a new proposal template: {} (status={})", userId, title, targetStatus);

        if (agentProposalRepository.existsByUserIdAndTitle(userId, title)) {
            throw new BusinessConflictException(
                    "You already have a proposal with this title",
                    "ERROR_DUPLICATE_PROPOSAL_TITLE");
        }

        String pitch = request.getPitchContent() != null ? request.getPitchContent() : "";

        ResolvedSpecialty resolvedSpecialty = resolveSpecialty(request.getSpecialty());

        AgentProposal proposal = AgentProposal.builder()
                .userId(userId)
                .title(title)
                .commissionRate(request.getCommissionRate())
                .experienceYears(request.getExperienceYears())
                .pitchContent(pitch)
                .specialty(resolvedSpecialty.id())
                .priceRange(request.getPriceRange())
                .status(targetStatus)
                .build();

        AgentProposal savedProposal = agentProposalRepository.save(proposal);
        syncAgentProfileSpecialties(userId, resolvedSpecialty.name());
        return agentProposalMapper.toDto(savedProposal);
    }

    @Transactional
    public AgentProposalDto updateProposal(UUID proposalId, UUID userId, ApplyAgentProposalRequest request) {
        log.info("Agent {} is updating proposal template: {}", userId, proposalId);

        AgentProposal proposal = agentProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentProposal", proposalId));

        boolean draft = isDraft(userId, request, proposal);
        validateApplyRequest(request, draft);

        String title = request.getTitle().trim();

        if (agentProposalRepository.existsByUserIdAndTitleExcludingId(userId, title, proposalId)) {
            throw new BusinessConflictException(
                    "You already have a proposal with this title",
                    "ERROR_DUPLICATE_PROPOSAL_TITLE");
        }

        String pitch = request.getPitchContent() != null ? request.getPitchContent() : "";

        ResolvedSpecialty resolvedSpecialty = resolveSpecialty(request.getSpecialty());

        if (draft) {
            proposal.replaceContent(
                    title,
                    request.getCommissionRate(),
                    request.getExperienceYears(),
                    pitch,
                    resolvedSpecialty.id(),
                    request.getPriceRange());
        } else {
            proposal.update(
                    title,
                    request.getCommissionRate(),
                    request.getExperienceYears(),
                    pitch,
                    resolvedSpecialty.id(),
                    request.getPriceRange());
        }
        if (request.getStatus() != null) {
            proposal.setProposalStatus(request.getStatus());
        }

        AgentProposal updatedProposal = agentProposalRepository.save(proposal);
        syncAgentProfileSpecialties(userId, resolvedSpecialty.name());
        return agentProposalMapper.toDto(updatedProposal);
    }

    private static boolean isDraft(UUID userId, ApplyAgentProposalRequest request, AgentProposal proposal) {
        if (!proposal.getUserId().equals(userId)) {
            throw new DomainException(
                    "You do not have permission to modify this proposal",
                    "ERROR_PROPOSAL_MODIFY_FORBIDDEN");
        }

        if (request.getStatus() == AgentProposalStatus.ARCHIVED) {
            throw new DomainException(
                    "Use archive endpoint to archive a proposal",
                    "ERROR_PROPOSAL_USE_ARCHIVE_ENDPOINT");
        }

        AgentProposalStatus effectiveStatus = request.getStatus() != null
                ? request.getStatus()
                : proposal.getStatus();
        boolean draft = effectiveStatus == AgentProposalStatus.DRAFT;
        return draft;
    }

    @Transactional(readOnly = true)
    public PageResponse<AgentProposalDto> getMyProposals(UUID userId, int page, int size) {
        log.info("Getting proposal templates for agent {}", userId);

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<AgentProposal> proposals = agentProposalRepository.findByUserId(userId, pageable);

        List<AgentProposal> pageEntities = proposals.getContent();
        List<AgentProposalDto> content = pageEntities.stream()
                .map(agentProposalMapper::toDto)
                .collect(Collectors.toList());
        attachSpecialtyCodes(content, pageEntities);

        return PageResponse.<AgentProposalDto>builder()
                .content(content)
                .page(proposals.getNumber())
                .size(proposals.getSize())
                .totalElements(proposals.getTotalElements())
                .totalPages(proposals.getTotalPages())
                .last(proposals.isLast())
                .first(proposals.isFirst())
                .build();
    }

    @Transactional
    public void archiveProposal(UUID proposalId, UUID userId) {
        log.info("Agent {} is archiving proposal template {}", userId, proposalId);

        AgentProposal proposal = agentProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentProposal", proposalId));

        if (!proposal.getUserId().equals(userId)) {
            throw new DomainException(
                    "You do not have permission to modify this proposal",
                    "ERROR_PROPOSAL_MODIFY_FORBIDDEN");
        }
        proposal.archive();
        agentProposalRepository.save(proposal);
    }

    private void validateApplyRequest(ApplyAgentProposalRequest request, boolean draft) {
        if (request.getTitle() == null) {
            throw new DomainException("Title is required", "ERROR_PROPOSAL_TITLE_REQUIRED");
        }
        String title = request.getTitle().trim();
        if (title.isEmpty()) {
            throw new DomainException("Title is required", "ERROR_PROPOSAL_TITLE_REQUIRED");
        }
        if (title.length() > 100) {
            throw new DomainException("Title must not exceed 100 characters", "ERROR_PROPOSAL_TITLE_TOO_LONG");
        }
        if (!draft && title.length() < 5) {
            throw new DomainException("Title must be at least 5 characters", "ERROR_PROPOSAL_TITLE_TOO_SHORT");
        }
        if (draft) {
            validateOptionalCommission(request.getCommissionRate());
            validateOptionalExperience(request.getExperienceYears());
        } else {
            BigDecimal cr = request.getCommissionRate();
            if (cr == null) {
                throw new DomainException("Commission rate is required", "ERROR_PROPOSAL_COMMISSION_REQUIRED");
            }
            if (cr.compareTo(BigDecimal.ZERO) <= 0 || cr.compareTo(new BigDecimal("100")) > 0) {
                throw new DomainException(
                        "Commission rate must be greater than 0 and at most 100",
                        "ERROR_PROPOSAL_COMMISSION_RANGE");
            }
            Integer ey = request.getExperienceYears();
            if (ey == null) {
                throw new DomainException("Experience years is required", "ERROR_PROPOSAL_EXPERIENCE_REQUIRED");
            }
            if (ey < 0 || ey > 60) {
                throw new DomainException(
                        "Experience years must be between 0 and 60",
                        "ERROR_PROPOSAL_EXPERIENCE_RANGE");
            }
            String pitch = request.getPitchContent();
            if (pitch == null || pitch.trim().isEmpty()) {
                throw new DomainException("Pitch content is required", "ERROR_PROPOSAL_PITCH_REQUIRED");
            }
            if (pitch.trim().length() < 50) {
                throw new DomainException(
                        "Pitch content must be at least 50 characters",
                        "ERROR_PROPOSAL_PITCH_TOO_SHORT");
            }
            if (pitch.length() > 2000) {
                throw new DomainException(
                        "Pitch content must not exceed 2000 characters",
                        "ERROR_PROPOSAL_PITCH_TOO_LONG");
            }
            if (request.getSpecialty() == null || request.getSpecialty().isBlank()) {
                throw new DomainException("Specialty is required", "ERROR_PROPOSAL_SPECIALTY_REQUIRED");
            }
        }
        validatePriceRangeMap(request.getPriceRange());
    }

    private static void validateOptionalCommission(BigDecimal cr) {
        if (cr == null) {
            return;
        }
        if (cr.compareTo(BigDecimal.ZERO) < 0 || cr.compareTo(new BigDecimal("100")) > 0) {
            throw new DomainException("Commission rate must be between 0 and 100", "ERROR_PROPOSAL_COMMISSION_RANGE");
        }
    }

    private static void validateOptionalExperience(Integer ey) {
        if (ey == null) {
            return;
        }
        if (ey < 0 || ey > 60) {
            throw new DomainException("Experience years must be between 0 and 60", "ERROR_PROPOSAL_EXPERIENCE_RANGE");
        }
    }

    @SuppressWarnings("unchecked")
    private static void validatePriceRangeMap(Map<String, Object> priceRange) {
        if (priceRange == null) {
            return;
        }
        Object rent = priceRange.get("rent");
        if (rent instanceof Map<?, ?> m) {
            validatePriceBand((Map<String, Object>) m, "rent");
        }
        Object sale = priceRange.get("sale");
        if (sale instanceof Map<?, ?> m) {
            validatePriceBand((Map<String, Object>) m, "sale");
        }
    }

    private static void validatePriceBand(Map<String, Object> band, String label) {
        if (band == null) {
            return;
        }
        int min = toInt(band.get("min"), 0);
        int max = toInt(band.get("max"), 0);
        if (min > 0 && max > 0 && min > max) {
            throw new DomainException(
                    "Minimum price cannot be greater than maximum price (" + label + ")",
                    "ERROR_PROPOSAL_PRICE_RANGE_INVALID");
        }
    }

    private static int toInt(Object o, int defaultVal) {
        if (o == null) {
            return defaultVal;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        return defaultVal;
    }

    private ResolvedSpecialty resolveSpecialty(String specialty) {
        if (specialty == null || specialty.isBlank()) {
            return new ResolvedSpecialty(null, null);
        }

        return propertyTypeRepository.findByCode(specialty)
                .map(pt -> new ResolvedSpecialty(pt.getPropertyTypeId(), pt.getName()))
                .orElse(new ResolvedSpecialty(null, null));
    }

    private void syncAgentProfileSpecialties(UUID userId, String propertyTypeName) {
        if (propertyTypeName == null || propertyTypeName.isBlank()) {
            return;
        }
        agentProfileRepository.findByUserId(userId).ifPresent(profile -> {
            String merged = mergeSpecialties(profile.getSpecialties(), propertyTypeName);
            if (!Objects.equals(profile.getSpecialties(), merged)) {
                profile.updateSpecialties(merged);
                agentProfileRepository.save(profile);
            }
        });
    }

    private static String mergeSpecialties(String existing, String toAppend) {
        String incoming = toAppend.trim();
        if (incoming.isEmpty()) {
            return existing;
        }
        if (existing == null || existing.isBlank()) {
            return incoming;
        }
        List<String> parts = Arrays.stream(existing.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        boolean present = parts.stream().anyMatch(s -> s.equalsIgnoreCase(incoming));
        if (!present) {
            parts.add(incoming);
        }
        return String.join(", ", parts);
    }

    /**
     * Resolves {@link PropertyType#getCode()} for each non-null specialty id (batch).
     */
    private Map<UUID, String> loadSpecialtyCodesById(Set<UUID> specialtyIds) {
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            return Map.of();
        }
        return propertyTypeRepository.findAllByIdIn(specialtyIds).stream()
                .filter(pt -> pt.getCode() != null && !pt.getCode().isBlank())
                .collect(Collectors.toMap(PropertyType::getPropertyTypeId, PropertyType::getCode, (a, b) -> a));
    }

    private void attachSpecialtyCodes(List<AgentProposalDto> dtos, List<AgentProposal> entities) {
        Set<UUID> ids = entities.stream()
                .map(AgentProposal::getSpecialty)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> codeById = loadSpecialtyCodesById(ids);
        for (int i = 0; i < dtos.size(); i++) {
            UUID sid = entities.get(i).getSpecialty();
            if (sid != null) {
                dtos.get(i).setSpecialtyCode(codeById.get(sid));
            }
        }
    }

    private record ResolvedSpecialty(UUID id, String name) {
    }
}
