package com.sep.realvista.domain.common.exception;

public class BusinessConflictException extends DomainException {
    public BusinessConflictException(String message) {
        super(message, "BUSINESS_CONFLICT");
    }

    public BusinessConflictException(String message, String errorCode) {
        super(message, errorCode);
    }
}

