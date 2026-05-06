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

    /** Renter's phone number for DocuSign SMS authentication. */
    private String renterPhone;

    /** Landlord's full name (tab label: landlordName). */
    private String landlordName;

    /** Landlord's email address. */
    private String landlordEmail;

    /** Landlord's client-side user ID for embedded signing. */
    private String landlordClientUserId;

    /** Landlord's phone number for DocuSign SMS authentication. */
    private String landlordPhone;

    /**
     * Handover/move-in date formatted as string (tab label: handoverDate).
     * Mapped from leaseStartDate.
     */
    private String handoverDate;

    /** Lease duration in months (tab label: leaseDurationMonths). */
    private String leaseDurationMonths;

    /** Property land/available area as a number only (tab label: propertyAvailable). */
    private String propertyAvailable;

    /** Property usable area as a number only (tab label: propertyUsed). */
    private String propertyUsed;

    /**
     * Monthly rent amount formatted with dot thousands separator (tab label: monthlyRent).
     * Example: "1.500.000"
     */
    private String monthlyRent;

    /**
     * Monthly rent amount in Vietnamese words (tab label: monthlyRentByText).
     * Example: "một triệu năm trăm nghìn đồng"
     */
    private String monthlyRentByText;

    /**
     * Security deposit formatted with dot thousands separator (tab label: securityDeposit).
     * Example: "3.000.000"
     */
    private String securityDeposit;

    /**
     * Security deposit in Vietnamese words (tab label: securityDepositByText).
     * Example: "ba triệu đồng"
     */
    private String securityDepositByText;

    /**
     * Vietnamese day-of-week for contract creation date (tab label: contractDayOfWeek).
     * Example: "Thứ Hai"
     */
    private String contractDayOfWeek;

    /**
     * Two-digit day of the contract creation date (tab label: contractDay).
     * Example: "07"
     */
    private String contractDay;

    /**
     * Two-digit month of the contract creation date (tab label: contractMonth).
     * Example: "02"
     */
    private String contractMonth;

    /**
     * Four-digit year of the contract creation date (tab label: contractYear).
     * Example: "2026"
     */
    private String contractYear;

    /** Current weekday in Vietnamese numeric style (tab label: currentWeekday). */
    private String currentWeekday;

    /** Current day number (tab label: currentDay). */
    private String currentDay;

    /** Current month number (tab label: currentMonth). */
    private String currentMonth;

    /** Current year number (tab label: currentYear). */
    private String currentYear;
}
