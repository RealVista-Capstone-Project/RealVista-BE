package com.sep.realvista.application.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerHeroInsightsResponse {

    /** Sum of listing_views.view_count for all owner's listings (all time). */
    private Long listingViewsTotal;

    /** Chat messages (non-system) in conversations tied to owner's listings via listing_leads. */
    private Long chatMessagesOnListings;

    /** Appointments linked to a listing on owner's properties. */
    private Long appointmentsOnOwnerListings;

    /** Listings closed as SOLD or RENTED (all time). */
    private Long completedContracts;
}
