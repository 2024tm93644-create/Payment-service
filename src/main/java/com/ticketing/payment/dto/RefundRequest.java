package com.ticketing.payment.dto;

public class RefundRequest {
    private Integer paymentId;
    private Double amount;
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
