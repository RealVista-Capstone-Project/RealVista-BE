package com.sep.realvista.domain.user;

public enum UserStatus {
    ACTIVE,     // Active user
    VERIFIED,   // Email/phone verified user
    SUSPENDED,  // Suspended by admin
    BANNED      // Permanently banned
}

