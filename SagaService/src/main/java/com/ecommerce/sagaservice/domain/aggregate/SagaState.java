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
    private Long userId;
    private Long productId;
    private int quantity;
    private String currentStep;
    private String status;
    //payload uzun olunca böyle yaptım 255'i geçse de sorun olmuyor
    @Column(columnDefinition = "TEXT")
    private String payload;
    public SagaState() {}

    public SagaState(Long orderId, String currentStep, String status) {
        this.orderId = orderId;
        this.currentStep = currentStep;
        this.status = status;
    }
}
