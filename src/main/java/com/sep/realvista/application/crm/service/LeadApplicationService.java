package com.sep.realvista.application.crm.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.crm.dto.AddLeadNoteRequest;
import com.sep.realvista.application.crm.dto.CreateLeadRequest;
import com.sep.realvista.application.crm.dto.LeadNoteResponse;
import com.sep.realvista.application.crm.dto.LeadResponse;
import com.sep.realvista.application.crm.dto.LeadSourceSummaryResponse;
import com.sep.realvista.application.crm.dto.LeadSummaryResponse;
import com.sep.realvista.application.crm.dto.UpdateLeadRequest;
import com.sep.realvista.application.crm.dto.UpdateLeadStatusRequest;
import com.sep.realvista.application.crm.mapper.LeadMapper;
import com.sep.realvista.domain.agent.lead.LeadSource;
import com.sep.realvista.domain.agent.lead.LeadNote;
import com.sep.realvista.domain.agent.lead.LeadNoteRepository;
import com.sep.realvista.domain.agent.lead.LeadPriority;
import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLead;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import com.sep.realvista.domain.agent.lead.NoteType;
import com.sep.realvista.domain.agent.lead.exception.ListingLeadNotFoundException;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadApplicationService {

    private static final LocalDateTime MIN_FILTER_DATE = LocalDate.of(1970, 1, 1).atStartOfDay();
    private static final LocalDateTime MAX_FILTER_DATE = LocalDate.of(9999, 12, 31).atStartOfDay();

    private final ListingLeadRepository leadRepository;
    private final LeadNoteRepository noteRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final LeadMapper leadMapper;

    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> getLeads(UUID agentId, LeadStatus status, LocalDate from, LocalDate to,
                                               String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        DateBounds bounds = toBounds(from, to);
        String normalizedQuery = normalizeQuery(query);

        Page<ListingLead> leadPage = leadRepository.findAllByAgentIdWithFilters(
                agentId,
                status,
                bounds.from(),
                bounds.toExclusive(),
                normalizedQuery,
                pageable
        );

        List<LeadResponse> content = leadPage.getContent().stream()
                .map(lead -> {
                    LeadResponse resp = toEnrichedResponse(lead);
                    List<LeadNoteResponse> notes = noteRepository
                            .findAllByListingLeadId(lead.getListingLeadId())
                            .stream().map(leadMapper::toNoteResponse).toList();
                    resp.setNotes(notes);
                    return resp;
                })
                .toList();

        return PageResponse.<LeadResponse>builder()
                .content(content)
                .page(leadPage.getNumber())
                .size(leadPage.getSize())
                .totalElements(leadPage.getTotalElements())
                .totalPages(leadPage.getTotalPages())
                .first(leadPage.isFirst())
                .last(leadPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public LeadSummaryResponse getSummary(UUID agentId, LocalDate from, LocalDate to, String query) {
        DateBounds current = toBounds(from, to);
        DateBounds previous = toPreviousBounds(current);
        String normalizedQuery = normalizeQuery(query);

        long totalLeads = leadRepository.countByAgentIdWithFilters(
                agentId, null, current.from(), current.toExclusive(), normalizedQuery);
        long closedLeads = leadRepository.countByAgentIdWithFilters(
                agentId, LeadStatus.CLOSED, current.from(), current.toExclusive(), normalizedQuery);
        long previousTotalLeads = leadRepository.countByAgentIdWithFilters(
                agentId, null, previous.from(), previous.toExclusive(), normalizedQuery);
        long previousClosedLeads = leadRepository.countByAgentIdWithFilters(
                agentId, LeadStatus.CLOSED, previous.from(), previous.toExclusive(), normalizedQuery);

        List<Object[]> sourceCounts = leadRepository.countBySourceWithFilters(
                agentId, current.from(), current.toExclusive(), normalizedQuery);
        List<LeadSourceSummaryResponse> bySource = Arrays.stream(LeadSource.values())
                .map(source -> LeadSourceSummaryResponse.builder()
                        .source(source)
                        .count(countForSource(sourceCounts, source))
                        .build())
                .toList();

        return LeadSummaryResponse.builder()
                .totalLeads(totalLeads)
                .closedLeads(closedLeads)
                .previousTotalLeads(previousTotalLeads)
                .previousClosedLeads(previousClosedLeads)
                .bySource(bySource)
                .build();
    }

    @Transactional
    public LeadResponse createLead(UUID agentId, CreateLeadRequest req) {
        ListingLead lead = ListingLead.builder()
                .agentId(agentId)
                .listingId(req.getListingId())
                .buyerId(req.getBuyerId())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .source(req.getSource())
                .budget(req.getBudget())
                .priority(req.getPriority() != null ? req.getPriority() : LeadPriority.MEDIUM)
                .build();

        lead = leadRepository.save(lead);

        if (req.getNote() != null && !req.getNote().isBlank()) {
            LeadNote firstNote = LeadNote.builder()
                    .listingLeadId(lead.getListingLeadId())
                    .agentId(agentId)
                    .content(req.getNote())
                    .noteType(NoteType.NOTE)
                    .statusAtTime(lead.getStatus())
                    .build();
            noteRepository.save(firstNote);
        }

        log.info("Created lead {} for agent {}", lead.getListingLeadId(), agentId);
        return toEnrichedResponse(lead);
    }

    @Transactional
    public LeadResponse updateLead(UUID agentId, UUID leadId, UpdateLeadRequest req) {
        ListingLead lead = leadRepository.findByIdAndAgentId(leadId, agentId)
                .orElseThrow(() -> new ListingLeadNotFoundException(leadId));

        lead.update(req.getFullName(), req.getEmail(), req.getPhone(),
                req.getSource(), req.getListingId(), req.getBudget());
        if (req.getPriority() != null) {
            lead.updatePriority(req.getPriority());
        }

        lead = leadRepository.save(lead);
        log.info("Updated lead {} for agent {}", leadId, agentId);
        return toEnrichedResponse(lead);
    }

    @Transactional
    public LeadResponse updateLeadStatus(UUID agentId, UUID leadId, UpdateLeadStatusRequest req) {
        ListingLead lead = leadRepository.findByIdAndAgentId(leadId, agentId)
                .orElseThrow(() -> new ListingLeadNotFoundException(leadId));

        lead.updateStatus(req.getStatus());
        lead = leadRepository.save(lead);

        log.info("Updated lead {} status to {} for agent {}", leadId, req.getStatus(), agentId);
        return toEnrichedResponse(lead);
    }

    @Transactional
    public void deleteLead(UUID agentId, UUID leadId) {
        ListingLead lead = leadRepository.findByIdAndAgentId(leadId, agentId)
                .orElseThrow(() -> new ListingLeadNotFoundException(leadId));

        lead.markAsDeleted();
        leadRepository.save(lead);
        log.info("Deleted lead {} for agent {}", leadId, agentId);
    }

    @Transactional
    public LeadNoteResponse addNote(UUID agentId, UUID leadId, AddLeadNoteRequest req) {
        ListingLead lead = leadRepository.findByIdAndAgentId(leadId, agentId)
                .orElseThrow(() -> new ListingLeadNotFoundException(leadId));

        LeadNote note = LeadNote.builder()
                .listingLeadId(leadId)
                .agentId(agentId)
                .content(req.getContent())
                .noteType(NoteType.NOTE)
                .statusAtTime(lead.getStatus())
                .build();

        note = noteRepository.save(note);
        log.info("Added note {} to lead {} for agent {}", note.getLeadNoteId(), leadId, agentId);
        return leadMapper.toNoteResponse(note);
    }

    private LeadResponse toEnrichedResponse(ListingLead lead) {
        LeadResponse resp = leadMapper.toResponse(lead);
        if (lead.getListingId() != null) {
            listingRepository.findById(lead.getListingId())
                    .ifPresent(listing -> resp.setListingName(listing.getName()));
        }
        if (lead.getBuyerId() != null) {
            userRepository.findById(lead.getBuyerId())
                    .ifPresent(user -> resp.setBuyerAvatarUrl(user.getAvatarUrl()));
        }
        return resp;
    }

    private DateBounds toBounds(LocalDate from, LocalDate to) {
        LocalDateTime fromDate = from != null ? from.atStartOfDay() : MIN_FILTER_DATE;
        LocalDateTime toDate = to != null ? to.plusDays(1).atStartOfDay() : MAX_FILTER_DATE;
        return new DateBounds(fromDate, toDate, from != null, to != null);
    }

    private DateBounds toPreviousBounds(DateBounds current) {
        if (!current.hasFrom() || !current.hasTo()) {
            return new DateBounds(MIN_FILTER_DATE, MAX_FILTER_DATE, false, false);
        }
        long days = Math.max(1, ChronoUnit.DAYS.between(current.from(), current.toExclusive()));
        LocalDateTime previousTo = current.from();
        return new DateBounds(previousTo.minusDays(days), previousTo, true, true);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return "%" + query.trim().toLowerCase() + "%";
    }

    private long countForSource(List<Object[]> rows, LeadSource source) {
        return rows.stream()
                .filter(row -> row[0] == source)
                .map(row -> (Long) row[1])
                .findFirst()
                .orElse(0L);
    }

    private record DateBounds(LocalDateTime from, LocalDateTime toExclusive, boolean hasFrom, boolean hasTo) {
    }
}
