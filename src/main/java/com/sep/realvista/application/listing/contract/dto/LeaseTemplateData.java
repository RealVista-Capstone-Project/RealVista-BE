package com.sep.realvista.application.listing.contract.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO carrying dynamic field values to populate a DocuSign lease template.
 * <p>
 * Field names correspond to the tab labels configured in the DocuSign template.
 * All values are pre-formatted strings ready for insertion into template tabs.
 */
@Data
@Builder
public class LeaseTemplateData {

    /** Renter's full name (tab label: renterName). */
    private String renterName;

    /** Renter's email address. */
    private String renterEmail;

    /** Renter's client-side user ID for embedded signing. */
    private String renterClientUserId;

    /** Landlord's full name (tab label: landlordName). */
    private String landlordName;

    /** Landlord's email address. */
    private String landlordEmail;

    /** Landlord's client-side user ID for embedded signing. */
    private String landlordClientUserId;

    /** Lease start date formatted as string (tab label: leaseStartDate). */
    private String leaseStartDate;

    /** Lease end date formatted as string (tab label: leaseEndDate). */
    private String leaseEndDate;

    /** Lease duration in months (tab label: leaseDurationMonths). */
    private String leaseDurationMonths;

    /** Monthly rent amount (tab label: monthlyRent). */
    private String monthlyRent;

    /** Security deposit amount (tab label: securityDeposit). */
    private String securityDeposit;
}
