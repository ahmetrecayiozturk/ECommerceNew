package com.ecommerce.deliveryservice.infrastructure.repository;

import com.ecommerce.deliveryservice.domain.aggregate.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findById(long id);
    Optional<Delivery> findByOrderId(Long orderId);
}
