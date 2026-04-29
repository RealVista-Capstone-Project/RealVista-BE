package com.sep.realvista.domain.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;

    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public DomainException(String message, String errorCode, Object[] args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public DomainException(String message, String errorCode, Object[] args, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }
}

