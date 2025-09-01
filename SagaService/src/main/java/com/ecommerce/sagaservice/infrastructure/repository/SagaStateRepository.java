package com.ecommerce.sagaservice.infrastructure.repository;

import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaStateRepository extends JpaRepository<SagaState, Integer> {
    SagaState findByOrderId(Long orderId);
    List<SagaState> findByStatus(String status);
}
