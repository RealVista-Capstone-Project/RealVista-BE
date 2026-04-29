package com.sep.realvista.domain.agent.lead.exception;

import com.sep.realvista.domain.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ListingLeadNotFoundException extends ResourceNotFoundException {

    public ListingLeadNotFoundException(UUID listingLeadId) {
        super("Lead", listingLeadId);
    }
}
