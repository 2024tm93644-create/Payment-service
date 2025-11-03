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
    public Map<String,Object> charge(String idempotencyKey, String correlationId, PaymentRequest req) throws RuntimeException {
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey);
        String fp = fingerprint(idempotencyKey, req);
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

        IdempotencyKey key = new IdempotencyKey();
        key.setIdempotencyKey(idempotencyKey);
        key.setRequestFingerprint(fp);
        idempotencyKeyRepository.save(key);

        Payment p = new Payment();
        p.setOrderId(req.getOrderId());
        p.setAmount(req.getAmount());
        p.setMethod(req.getMethod() == null ? "CARD" : req.getMethod());
        p.setStatus("PENDING");
        p = paymentRepository.save(p);

        boolean success = Math.round(req.getAmount()) % 2 == 0;
        if (success) {
            p.setStatus("SUCCESS");
            p.setReference("ETP-" + java.util.UUID.randomUUID().toString().substring(0,8));
            paymentRepository.save(p);
            Map<String,Object> resp = new HashMap<>();
            resp.put("paymentId", p.getPaymentId());
            resp.put("status","SUCCESS");
            resp.put("orderId", p.getOrderId());
            resp.put("amount", p.getAmount());
            resp.put("reference", p.getReference());
            try {
                key.setResponseCode(200);
                key.setResponseBody(objectMapper.writeValueAsString(resp));
                idempotencyKeyRepository.save(key);
            } catch (Exception e) {}
            return resp;
        } else {
            p.setStatus("FAILED");
            paymentRepository.save(p);
            Map<String,Object> resp = new HashMap<>();
            resp.put("paymentId", p.getPaymentId());
            resp.put("status","FAILED");
            resp.put("orderId", p.getOrderId());
            resp.put("amount", p.getAmount());
            try {
                key.setResponseCode(402);
                key.setResponseBody(objectMapper.writeValueAsString(resp));
                idempotencyKeyRepository.save(key);
            } catch (Exception e) {}
            return resp;
        }
    }

    @Transactional
    public Map<String,Object> refund(RefundRequest req) {
        Optional<Payment> pOpt = paymentRepository.findById(req.getPaymentId());
        if (pOpt.isEmpty()) throw new RuntimeException("payment not found");
        Payment p = pOpt.get();
        if (!"SUCCESS".equals(p.getStatus())) throw new RuntimeException("only successful payments can be refunded");
        Refund r = new Refund();
        r.setPaymentId(p.getPaymentId());
        r.setAmount(req.getAmount());
        r.setStatus("PENDING");
        r = refundRepository.save(r);

        r.setStatus("SUCCESS");
        r.setProviderRef("REF-" + java.util.UUID.randomUUID().toString().substring(0,8));
        refundRepository.save(r);
        p.setStatus("REFUNDED");
        paymentRepository.save(p);

        Map<String,Object> resp = new HashMap<>();
        resp.put("refundId", r.getId());
        resp.put("status","SUCCESS");
        resp.put("paymentId", p.getPaymentId());
        resp.put("amount", r.getAmount());
        return resp;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(Integer id) {
        return paymentRepository.findById(id).orElse(null);
    }
}
