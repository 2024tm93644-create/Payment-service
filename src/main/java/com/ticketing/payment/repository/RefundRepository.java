package com.ticketing.payment.repository;

import com.ticketing.payment.model.Refund;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Integer> {
	 Optional<Refund> findByPaymentId(Integer paymentId);
}
