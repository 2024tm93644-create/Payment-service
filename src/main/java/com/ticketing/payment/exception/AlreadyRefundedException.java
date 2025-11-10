package com.ticketing.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AlreadyRefundedException extends RuntimeException {
    public AlreadyRefundedException(String message) {
        super(message);
    }
}
