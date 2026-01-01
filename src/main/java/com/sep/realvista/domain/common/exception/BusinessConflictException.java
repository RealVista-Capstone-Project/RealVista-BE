package com.sep.realvista.domain.common.exception;

/**
 * Exception thrown when there's a conflict in the business logic.
 */
public class BusinessConflictException extends DomainException {
    public BusinessConflictException(String message) {
        super(message, "BUSINESS_CONFLICT");
    }

    public BusinessConflictException(String message, String errorCode) {
        super(message, errorCode);
    }
}

