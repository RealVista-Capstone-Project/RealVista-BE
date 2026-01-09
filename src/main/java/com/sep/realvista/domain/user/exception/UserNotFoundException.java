package com.sep.realvista.domain.user.exception;

import com.sep.realvista.domain.common.exception.DomainException;

/**
 * Exception thrown when a user is not found.
 * Follows domain exception handling convention (Section 3.5).
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Long id) {
        super(String.format("User not found with id: %s", id), "USER_NOT_FOUND");
    }

    public UserNotFoundException(String email) {
        super(String.format("User not found with email: %s", email), "USER_NOT_FOUND");
    }

    public UserNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
