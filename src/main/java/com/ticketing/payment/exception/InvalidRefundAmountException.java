package com.ticketing.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRefundAmountException extends RuntimeException {
    public InvalidRefundAmountException(String message) {
        super(message);
    }
}
