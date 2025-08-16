package com.ecommerce.sagaservice.api.controller;

import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import com.ecommerce.sagaservice.infrastructure.repository.SagaStateRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saga")
public class SagaStatusController {
    private final SagaStateRepository sagaStateRepository;

    public SagaStatusController(SagaStateRepository sagaStateRepository) {
        this.sagaStateRepository = sagaStateRepository;
    }

    @GetMapping("/status/{orderId}")
    public SagaState getSagaStatus(@PathVariable Long orderId) {
        return sagaStateRepository.findByOrderId(orderId);
    }
}