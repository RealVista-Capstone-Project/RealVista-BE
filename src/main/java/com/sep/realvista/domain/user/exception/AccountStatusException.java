package com.sep.realvista.domain.user.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when a user attempts to login while their account is suspended or banned.
 */
@Getter
public class AccountStatusException extends AuthenticationException {
    private final String errorCode;

    public AccountStatusException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
