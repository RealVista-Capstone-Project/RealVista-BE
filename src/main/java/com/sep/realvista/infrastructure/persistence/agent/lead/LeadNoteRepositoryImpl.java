package com.sep.realvista.infrastructure.persistence.agent.lead;

import com.sep.realvista.domain.agent.lead.LeadNote;
import com.sep.realvista.domain.agent.lead.LeadNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LeadNoteRepositoryImpl implements LeadNoteRepository {

    private final LeadNoteJpaRepository jpaRepository;

    @Override
    public LeadNote save(LeadNote note) {
        return jpaRepository.save(note);
    }

    @Override
    public List<LeadNote> findAllByListingLeadId(UUID listingLeadId) {
        return jpaRepository.findAllByListingLeadId(listingLeadId);
    }
}
