package com.ticketing.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.payment.dto.PaymentRequest;
import com.ticketing.payment.dto.RefundRequest;
import com.ticketing.payment.model.IdempotencyKey;
import com.ticketing.payment.model.Payment;
import com.ticketing.payment.model.Refund;
import com.ticketing.payment.repository.IdempotencyKeyRepository;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.payment.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final RefundRepository refundRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentService(PaymentRepository paymentRepository, IdempotencyKeyRepository idempotencyKeyRepository, RefundRepository refundRepository) {
        this.paymentRepository = paymentRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.refundRepository = refundRepository;
    }

    private String fingerprint(String idempotencyKey, PaymentRequest req) {
        try {
            String s = req.getOrderId() + "|" + req.getAmount() + "|" + req.getMethod();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(req.hashCode());
        }
    }

    @Transactional
    public Map<String, Object> charge(String idempotencyKey, String correlationId, PaymentRequest req) {
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey);
        String fp = fingerprint(idempotencyKey, req);

        // 1. Handle idempotency reuse
        if (existing.isPresent()) {
            IdempotencyKey k = existing.get();
            if (!k.getRequestFingerprint().equals(fp)) {
                throw new RuntimeException("Idempotency conflict: same key, different payload");
            }
            try {
                Map resp = objectMapper.readValue(k.getResponseBody(), Map.class);
                return resp;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse stored response");
            }
        }

        // 2. Create new idempotency record
        IdempotencyKey key = new IdempotencyKey();
        key.setIdempotencyKey(idempotencyKey);
        key.setRequestFingerprint(fp);
        idempotencyKeyRepository.save(key);

        // 3. Validate request
        if (req.getAmount() == null || req.getAmount() <= 0) {
            Payment p = new Payment();
            p.setOrderId(req.getOrderId());
            p.setAmount(req.getAmount());
            p.setMethod(req.getMethod() == null ? "CARD" : req.getMethod());
            p.setStatus("FAILED");
            paymentRepository.save(p);

            Map<String, Object> resp = new HashMap<>();
            resp.put("paymentId", p.getPaymentId());
            resp.put("status", "FAILED");
            resp.put("orderId", p.getOrderId());
            resp.put("amount", p.getAmount());
            resp.put("error", "Invalid payment amount. Must be greater than zero.");

            try {
                key.setResponseCode(400);
                key.setResponseBody(objectMapper.writeValueAsString(resp));
                idempotencyKeyRepository.save(key);
            } catch (Exception ignored) {}

            return resp;
        }

        // 4. Process valid payment
        Payment p = new Payment();
        p.setOrderId(req.getOrderId());
        p.setAmount(req.getAmount());
        p.setMethod(req.getMethod() == null ? "CARD" : req.getMethod());
        p.setStatus("PENDING");
        p = paymentRepository.save(p);

        // Simulate payment success (always succeed for valid amounts)
        p.setStatus("SUCCESS");
        p.setReference("ETP-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(p);

        // 5. Build success response
        Map<String, Object> resp = new HashMap<>();
        resp.put("paymentId", p.getPaymentId());
        resp.put("status", "SUCCESS");
        resp.put("orderId", p.getOrderId());
        resp.put("amount", p.getAmount());
        resp.put("reference", p.getReference());

        try {
            key.setResponseCode(200);
            key.setResponseBody(objectMapper.writeValueAsString(resp));
            idempotencyKeyRepository.save(key);
        } catch (Exception ignored) {}

        return resp;
    }


    @Transactional
    public Map<String, Object> refund(RefundRequest req) {
        Optional<Payment> pOpt = paymentRepository.findById(req.getPaymentId());
        if (pOpt.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        Payment p = pOpt.get();

        // Validate payment status
        if (!"SUCCESS".equals(p.getStatus())) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        // Validate refund amount
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new RuntimeException("Invalid refund amount. Must be greater than zero");
        }
        if (req.getAmount() > p.getAmount()) {
            throw new RuntimeException("Refund amount cannot exceed original payment amount");
        }

        // Check if payment already refunded
        Optional<Refund> existingRefund = refundRepository.findByPaymentId(p.getPaymentId());
        if (existingRefund.isPresent()) {
            throw new RuntimeException("Payment already refunded");
        }

        // Create new refund record
        Refund r = new Refund();
        r.setPaymentId(p.getPaymentId());
        r.setAmount(req.getAmount());
        r.setStatus("PENDING");
        r = refundRepository.save(r);

        // Simulate refund processing (always success for valid refund)
        r.setStatus("SUCCESS");
        r.setProviderRef("REF-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        refundRepository.save(r);

        // Update payment status
        p.setStatus("REFUNDED");
        paymentRepository.save(p);

        // Build refund response
        Map<String, Object> resp = new HashMap<>();
        resp.put("refundId", r.getId());
        resp.put("status", "SUCCESS");
        resp.put("paymentId", p.getPaymentId());
        resp.put("orderId", p.getOrderId());
        resp.put("refundAmount", r.getAmount());
        resp.put("providerRef", r.getProviderRef());

        return resp;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(Integer id) {
        return paymentRepository.findById(id).orElse(null);
    }
}
