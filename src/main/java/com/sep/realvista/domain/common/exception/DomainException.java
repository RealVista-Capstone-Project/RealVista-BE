package com.sep.realvista.domain.common.exception;

import lombok.Getter;

/**
 * Base exception for all domain-level business logic errors.
 */
@Getter
public class DomainException extends RuntimeException {
    private final String errorCode;

    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

