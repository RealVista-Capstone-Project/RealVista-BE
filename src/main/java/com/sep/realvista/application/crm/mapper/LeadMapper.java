package com.sep.realvista.application.crm.mapper;

import com.sep.realvista.application.crm.dto.LeadNoteResponse;
import com.sep.realvista.application.crm.dto.LeadResponse;
import com.sep.realvista.domain.agent.lead.LeadNote;
import com.sep.realvista.domain.agent.lead.ListingLead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeadMapper {

    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "listingName", ignore = true)
    @Mapping(target = "buyerAvatarUrl", ignore = true)
    LeadResponse toResponse(ListingLead lead);

    LeadNoteResponse toNoteResponse(LeadNote note);
}
