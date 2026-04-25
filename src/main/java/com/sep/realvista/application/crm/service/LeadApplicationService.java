package com.sep.realvista.application.crm.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.crm.dto.AddLeadNoteRequest;
import com.sep.realvista.application.crm.dto.CreateLeadRequest;
import com.sep.realvista.application.crm.dto.LeadNoteResponse;
import com.sep.realvista.application.crm.dto.LeadResponse;
import com.sep.realvista.application.crm.dto.UpdateLeadRequest;
import com.sep.realvista.application.crm.dto.UpdateLeadStatusRequest;
import com.sep.realvista.application.crm.mapper.LeadMapper;
import com.sep.realvista.domain.agent.lead.LeadNote;
import com.sep.realvista.domain.agent.lead.LeadNoteRepository;
import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLead;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import com.sep.realvista.domain.agent.lead.NoteType;
import com.sep.realvista.domain.agent.lead.exception.ListingLeadNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadApplicationService {

    private final ListingLeadRepository leadRepository;
    private final LeadNoteRepository noteRepository;
    private final LeadMapper leadMapper;

    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> getLeads(UUID agentId, LeadStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ListingLead> leadPage = (status != null)
                ? leadRepository.findAllByAgentIdAndStatus(agentId, status, pageable)
                : leadRepository.findAllByAgentId(agentId, pageable);

        List<LeadResponse> content = leadPage.getContent().stream()
                .map(lead -> {
                    LeadResponse resp = leadMapper.toResponse(lead);
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

    @Transactional
    public LeadResponse createLead(UUID agentId, CreateLeadRequest req) {
        ListingLead lead = ListingLead.builder()
                .agentId(agentId)
                .listingId(req.getListingId())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .source(req.getSource())
                .budget(req.getBudget())
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
        return leadMapper.toResponse(lead);
    }

    @Transactional
    public LeadResponse updateLead(UUID agentId, UUID leadId, UpdateLeadRequest req) {
        ListingLead lead = leadRepository.findByIdAndAgentId(leadId, agentId)
                .orElseThrow(() -> new ListingLeadNotFoundException(leadId));

        lead.update(req.getFullName(), req.getEmail(), req.getPhone(),
                req.getSource(), req.getListingId(), req.getBudget());

        lead = leadRepository.save(lead);
        log.info("Updated lead {} for agent {}", leadId, agentId);
        return leadMapper.toResponse(lead);
    }

    @Transactional
    public LeadResponse updateLeadStatus(UUID agentId, UUID leadId, UpdateLeadStatusRequest req) {
        ListingLead lead = leadRepository.findByIdAndAgentId(leadId, agentId)
                .orElseThrow(() -> new ListingLeadNotFoundException(leadId));

        lead.updateStatus(req.getStatus());
        lead = leadRepository.save(lead);

        log.info("Updated lead {} status to {} for agent {}", leadId, req.getStatus(), agentId);
        return leadMapper.toResponse(lead);
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
}
