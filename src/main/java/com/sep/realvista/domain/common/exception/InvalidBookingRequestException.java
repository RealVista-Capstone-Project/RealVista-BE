package com.sep.realvista.domain.common.exception;

public class InvalidBookingRequestException extends DomainException {
    public InvalidBookingRequestException(String message) {
        super(message, "INVALID_BOOKING_REQUEST");
    }
}
