package com.sep.realvista.domain.common.exception;

public class ServiceUnavailableException extends DomainException {
    public ServiceUnavailableException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ServiceUnavailableException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
