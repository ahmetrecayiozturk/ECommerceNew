package com.ecommerce.orderservice.infrastructure.repository;

import com.ecommerce.orderservice.domain.aggregate.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
