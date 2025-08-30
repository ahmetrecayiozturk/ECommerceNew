package com.ecommerce.sagaservice.api.controller;

import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import com.ecommerce.sagaservice.infrastructure.repository.SagaStateRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saga")
public class SagaStatusController {
    private final SagaStateRepository sagaStateRepository;
    private final Environment env;

    public SagaStatusController(SagaStateRepository sagaStateRepository, Environment env) {
        this.sagaStateRepository = sagaStateRepository;
        this.env = env;
    }

    @PostMapping("/health-check")
    public ResponseEntity<String> healthCheck(){
        String port = env.getProperty("server.port");
        String msg = "Saga Service is up and running on port: " + port;
        System.out.println(msg);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/status/{orderId}")
    public SagaState getSagaStatus(@PathVariable Long orderId) {
        return sagaStateRepository.findByOrderId(orderId);
    }
}