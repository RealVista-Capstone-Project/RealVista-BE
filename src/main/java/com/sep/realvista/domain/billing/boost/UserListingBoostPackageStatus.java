package com.sep.realvista.domain.billing.boost;

public enum UserListingBoostPackageStatus {
    ACTIVE,      // Currently active and usable
    EXPIRED,     // End date has passed
    CANCELLED,   // User cancelled
    EXHAUSTED    // All quotas used (if applicable)
}
