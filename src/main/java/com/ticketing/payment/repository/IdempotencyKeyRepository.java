package com.ticketing.payment.repository;

import com.ticketing.payment.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Integer> {
    Optional<IdempotencyKey> findByIdempotencyKey(String key);
}
