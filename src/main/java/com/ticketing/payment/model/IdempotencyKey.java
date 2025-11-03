package com.ticketing.payment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String idempotencyKey;
    @Column(columnDefinition = "text")
    private String requestFingerprint;
    private Integer responseCode;
    @Column(columnDefinition = "jsonb")
    private String responseBody;
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getRequestFingerprint() { return requestFingerprint; }
    public void setRequestFingerprint(String requestFingerprint) { this.requestFingerprint = requestFingerprint; }
    public Integer getResponseCode() { return responseCode; }
    public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
}
