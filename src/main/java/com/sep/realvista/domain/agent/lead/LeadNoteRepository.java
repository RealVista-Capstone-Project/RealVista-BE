package com.sep.realvista.domain.agent.lead;

import java.util.List;
import java.util.UUID;

public interface LeadNoteRepository {

    LeadNote save(LeadNote note);

    List<LeadNote> findAllByListingLeadId(UUID listingLeadId);
}
