package com.ticketing.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildResponse(HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<Object> handleIdempotencyConflict(IdempotencyConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<Object> handleInvalidPaymentStatus(InvalidPaymentStatusException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS", ex.getMessage());
    }

    @ExceptionHandler(InvalidRefundAmountException.class)
    public ResponseEntity<Object> handleInvalidRefundAmount(InvalidRefundAmountException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REFUND_AMOUNT", ex.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Object> handlePaymentNotFound(PaymentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(AlreadyRefundedException.class)
    public ResponseEntity<Object> handleAlreadyRefunded(AlreadyRefundedException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "ALREADY_REFUNDED", ex.getMessage());
    }

    // fallback for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
    }
}
