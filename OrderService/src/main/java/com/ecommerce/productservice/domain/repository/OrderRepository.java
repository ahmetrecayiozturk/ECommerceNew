package com.ecommerce.productservice.domain.repository;

import com.ecommerce.productservice.domain.aggregate.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
