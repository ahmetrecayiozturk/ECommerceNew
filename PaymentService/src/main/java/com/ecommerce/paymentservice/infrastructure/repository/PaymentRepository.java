package com.ecommerce.paymentservice.infrastructure.repository;

import com.ecommerce.paymentservice.domain.aggregate.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findById(long id);
    Optional<Payment> findByOrderId(Long orderId);
}
