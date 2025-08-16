package com.ecommerce.sagaservice.domain.aggregate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "saga_states")
public class SagaState {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String currentStep;
    private String status; // IN_PROGRESS, SUCCEEDED, FAILED, COMPENSATING
    @Column(columnDefinition = "TEXT")
    private String payload; // Uzun event JSON'u için
    public SagaState() {}

    public SagaState(Long orderId, String currentStep, String status) {
        this.orderId = orderId;
        this.currentStep = currentStep;
        this.status = status;
    }
}
