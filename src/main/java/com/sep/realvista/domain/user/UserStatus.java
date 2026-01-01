package com.sep.realvista.domain.user;

/**
 * User status enumeration.
 */
public enum UserStatus {
    PENDING,    // Email verification pending
    ACTIVE,     // Active user
    SUSPENDED,  // Suspended by admin
    INACTIVE    // Deactivated by user
}

