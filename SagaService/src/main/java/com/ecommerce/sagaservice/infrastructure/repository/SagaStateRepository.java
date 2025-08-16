package com.ecommerce.sagaservice.infrastructure.repository;

import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaStateRepository extends JpaRepository<SagaState, Integer> {
    SagaState findByOrderId(Long orderId);
}
