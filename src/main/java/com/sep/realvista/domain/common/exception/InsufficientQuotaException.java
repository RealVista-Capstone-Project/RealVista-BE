package com.sep.realvista.domain.common.exception;

public class InsufficientQuotaException extends RuntimeException {

    private static final String ERROR_CODE = "QUOTA_EXHAUSTED";

    public InsufficientQuotaException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}
