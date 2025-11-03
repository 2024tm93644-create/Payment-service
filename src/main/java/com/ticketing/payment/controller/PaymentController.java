package com.ticketing.payment.controller;

import com.ticketing.payment.dto.PaymentRequest;
import com.ticketing.payment.dto.RefundRequest;
import com.ticketing.payment.model.Payment;
import com.ticketing.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
                                    @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                    @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.charge(idempotencyKey, correlationId, request));
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable("id") Integer id) {
        Payment p = paymentService.getPayment(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }
}
